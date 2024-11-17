package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.config.ConfigManager
import at.hannibal2.skyhanni.data.jsonobjects.repo.MultiFilterJson
import at.hannibal2.skyhanni.events.NeuRepositoryReloadEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.ItemBlink.checkBlinkItem
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.PrimitiveIngredient.Companion.toPrimitiveItemStacks
import at.hannibal2.skyhanni.utils.PrimitiveItemStack.Companion.makePrimitiveStack
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getItemId
import at.hannibal2.skyhanni.utils.system.PlatformUtils
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import io.github.moulberry.notenoughupdates.NEUManager
import io.github.moulberry.notenoughupdates.NEUOverlay
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.overlays.AuctionSearchOverlay
import io.github.moulberry.notenoughupdates.overlays.BazaarSearchOverlay
import io.github.moulberry.notenoughupdates.util.ItemResolutionQuery
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GLAllocation
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11
import kotlin.time.Duration.Companion.seconds
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getNpcPrice as getNpcPriceNew
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getNpcPriceOrNull as getNpcPriceOrNullNew
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getPriceOrNull as getPriceOrNullNew
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getRawCraftCostOrNull as getRawCraftCostOrNullNew

@SkyHanniModule
object NEUItems {

    val manager: NEUManager get() = NotEnoughUpdates.INSTANCE.manager
    private val multiplierCache = mutableMapOf<NEUInternalName, PrimitiveItemStack>()
    private val recipesCache = mutableMapOf<NEUInternalName, Set<PrimitiveRecipe>>()
    private val ingredientsCache = mutableMapOf<PrimitiveRecipe, Set<PrimitiveIngredient>>()
    private val itemIdCache = mutableMapOf<Item, List<NEUInternalName>>()

    var allItemsCache = mapOf<String, NEUInternalName>() // item name -> internal name
    val allInternalNames = mutableListOf<NEUInternalName>()
    val ignoreItemsFilter = MultiFilter()

    private val fallbackItem by lazy {
        ItemUtils.createItemStack(
            ItemStack(Blocks.barrier).item,
            "§cMissing Repo Item",
            "§cYour NEU repo seems to be out of date",
        )
    }

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val ignoredItems = event.getConstant<MultiFilterJson>("IgnoredItems")
        ignoreItemsFilter.load(ignoredItems)
    }

    @SubscribeEvent
    fun onNeuRepoReload(event: NeuRepositoryReloadEvent) {
        allItemsCache = readAllNeuItems()
    }

    fun readAllNeuItems(): Map<String, NEUInternalName> {
        allInternalNames.clear()
        val map = mutableMapOf<String, NEUInternalName>()
        for (rawInternalName in allNeuRepoItems().keys) {
            var name = manager.createItem(rawInternalName).displayName.lowercase()

            // we ignore all builder blocks from the item name -> internal name cache
            // because builder blocks can have the same display name as normal items.
            if (rawInternalName.startsWith("BUILDER_")) continue

            val internalName = rawInternalName.toInternalName()

            // TODO remove all except one of them once neu is consistent
            name = name.removePrefix("§f§f§7[lvl 1➡100] ")
            name = name.removePrefix("§f§f§7[lvl {lvl}] ")
            name = name.removePrefix("§7[lvl 1➡100] ")

            if (name.contains("[lvl 1➡100]")) {
                if (PlatformUtils.isDevEnvironment) {
                    error("wrong name: '$name'")
                }
                println("wrong name: '$name'")
            }
            map[name] = internalName
            allInternalNames.add(internalName)
        }
        return map
    }

    fun getInternalName(itemStack: ItemStack): String? = ItemResolutionQuery(manager)
        .withCurrentGuiContext()
        .withItemStack(itemStack)
        .resolveInternalName()

    fun getInternalNameOrNull(nbt: NBTTagCompound): NEUInternalName? =
        ItemResolutionQuery(manager).withItemNBT(nbt).resolveInternalName()?.toInternalName()

    // TODO check if getItemId is necessary here. getItemStackOrNull should already return null if invalid
    fun getInternalNameFromHypixelIdOrNull(hypixelId: String): NEUInternalName? {
        val internalName = hypixelId.replace(':', '-')
        return internalName.toInternalName().takeIf { it.getItemStackOrNull()?.getItemId() == internalName }
    }

    fun getInternalNameFromHypixelId(hypixelId: String): NEUInternalName =
        getInternalNameFromHypixelIdOrNull(hypixelId)
            ?: error("hypixel item id does not match internal name: $hypixelId")

    @Deprecated("Moved to ItemPriceUtils", ReplaceWith(""))
    fun NEUInternalName.getNpcPriceOrNull(): Double? = getNpcPriceOrNullNew()

    fun transHypixelNameToInternalName(hypixelId: String): NEUInternalName =
        manager.auctionManager.transformHypixelBazaarToNEUItemId(hypixelId).toInternalName()

    @Deprecated("Moved to ItemPriceUtils", ReplaceWith(""))
    fun NEUInternalName.getPriceOrNull(
        priceSource: ItemPriceSource = ItemPriceSource.BAZAAR_INSTANT_BUY,
        pastRecipes: List<PrimitiveRecipe> = emptyList(),
    ): Double? = this.getPriceOrNullNew(priceSource, pastRecipes)

    @Deprecated("Moved to ItemPriceUtils", ReplaceWith(""))
    fun NEUInternalName.getRawCraftCostOrNull(pastRecipes: List<PrimitiveRecipe> = emptyList()): Double? =
        getRawCraftCostOrNullNew(ItemPriceSource.BAZAAR_INSTANT_BUY, pastRecipes)

    fun NEUInternalName.getItemStackOrNull(): ItemStack? = ItemResolutionQuery(manager)
        .withKnownInternalName(asString())
        .resolveToItemStack()?.copy()

    fun getItemStackOrNull(internalName: String) = internalName.toInternalName().getItemStackOrNull()

    fun NEUInternalName.getItemStack(): ItemStack =
        getItemStackOrNull() ?: run {
            getPriceOrNullNew() ?: return@run fallbackItem
            if (ignoreItemsFilter.match(this.asString())) return@run fallbackItem

            val name = this.toString()
            ItemUtils.addMissingRepoItem(name, "Could not create item stack for $name")
            fallbackItem
        }

    fun isVanillaItem(item: ItemStack): Boolean = item.getInternalName().isVanillaItem()

    fun NEUInternalName.isVanillaItem(): Boolean = manager.auctionManager.isVanillaItem(this.asString())

    fun NEUInternalName.removePrefix(prefix: String): NEUInternalName {
        if (prefix.isEmpty()) return this
        val string = asString()
        if (!string.startsWith(prefix)) return this
        return string.substring(prefix.length).toInternalName()
    }

    const val itemFontSize = 2.0 / 3.0

    fun ItemStack.renderOnScreen(
        x: Float,
        y: Float,
        scaleMultiplier: Double = itemFontSize,
        rescaleSkulls: Boolean = true,
    ) {
        val item = checkBlinkItem()
        val isSkull = rescaleSkulls && item.item === Items.skull

        val baseScale = (if (isSkull) 4f / 3f else 1f)
        val finalScale = baseScale * scaleMultiplier

        val translateX: Float
        val translateY: Float
        if (isSkull) {
            val skullDiff = ((scaleMultiplier) * 2.5).toFloat()
            translateX = x - skullDiff
            translateY = y - skullDiff
        } else {
            translateX = x
            translateY = y
        }

        GlStateManager.pushMatrix()

        GlStateManager.translate(translateX, translateY, -19f)
        GlStateManager.scale(finalScale, finalScale, 0.2)
        GL11.glNormal3f(0f, 0f, 1f / 0.2f) // Compensate for z scaling

        RenderHelper.enableGUIStandardItemLighting()

        AdjustStandardItemLighting.adjust() // Compensate for z scaling

        try {
            Minecraft.getMinecraft().renderItem.renderItemIntoGUI(item, 0, 0)
        } catch (e: Exception) {
            if (lastWarn.passedSince() > 1.seconds) {
                lastWarn = SimpleTimeMark.now()
                println(" ")
                println("item: $item")
                println("name: ${item.name}")
                println("getInternalNameOrNull: ${item.getInternalNameOrNull()}")
                println(" ")
                ChatUtils.debug("rendering an item has failed.")
            }
        }
        RenderHelper.disableStandardItemLighting()

        GlStateManager.popMatrix()
    }

    private var lastWarn = SimpleTimeMark.farPast()

    private object AdjustStandardItemLighting {

        private const val lightScaling = 2.47f // Adjust as needed
        private const val g = 0.6f // Original Value taken from RenderHelper
        private const val lightIntensity = lightScaling * g
        private val itemLightBuffer = GLAllocation.createDirectFloatBuffer(16)

        init {
            itemLightBuffer.clear()
            itemLightBuffer.put(lightIntensity).put(lightIntensity).put(lightIntensity).put(1.0f)
            itemLightBuffer.flip()
        }

        fun adjust() {
            GL11.glLight(16384, 4609, itemLightBuffer)
            GL11.glLight(16385, 4609, itemLightBuffer)
        }
    }

    fun allNeuRepoItems(): Map<String, JsonObject> = NotEnoughUpdates.INSTANCE.manager.itemInformation

    fun getInternalNamesForItemId(item: Item): List<NEUInternalName> {
        itemIdCache[item]?.let {
            return it
        }
        val result = allNeuRepoItems().filter {
            Item.getByNameOrId(it.value["itemid"].asString) == item
        }.keys.map {
            it.toInternalName()
        }
        itemIdCache[item] = result
        return result
    }

    fun getPrimitiveMultiplier(internalName: NEUInternalName, tryCount: Int = 0): PrimitiveItemStack {
        multiplierCache[internalName]?.let { return it }
        if (tryCount == 10) {
            ErrorManager.logErrorStateWithData(
                "Could not load recipe data.",
                "Failed to find item multiplier",
                "internalName" to internalName,
            )
            return internalName.makePrimitiveStack()
        }
        for (recipe in getRecipes(internalName)) {
            if (!recipe.isCraftingRecipe()) continue

            val map = mutableMapOf<NEUInternalName, Int>()
            for (ingredient in recipe.getCachedIngredients().toPrimitiveItemStacks()) {
                val amount = ingredient.amount
                var internalItemId = ingredient.internalName
                // ignore cactus green
                if (internalName == "ENCHANTED_CACTUS_GREEN".toInternalName() && internalItemId == "INK_SACK-2".toInternalName()) {
                    internalItemId = "CACTUS".toInternalName()
                }

                // ignore wheat in enchanted cookie
                if (internalName == "ENCHANTED_COOKIE".toInternalName() && internalItemId == "WHEAT".toInternalName()) {
                    continue
                }

                // ignore golden carrot in enchanted golden carrot
                if (internalName == "ENCHANTED_GOLDEN_CARROT".toInternalName() && internalItemId == "GOLDEN_CARROT".toInternalName()) {
                    continue
                }

                // ignore rabbit hide in leather
                if (internalName == "LEATHER".toInternalName() && internalItemId == "RABBIT_HIDE".toInternalName()) {
                    continue
                }

                map.addOrPut(internalItemId, amount)
            }
            if (map.size != 1) continue
            val current = map.iterator().next().toPair()
            val id = current.first
            return if (current.second > 1) {
                val child = getPrimitiveMultiplier(id, tryCount + 1)
                val result = child * current.second
                multiplierCache[internalName] = result
                result
            } else {
                internalName.makePrimitiveStack()
            }
        }

        val result = internalName.makePrimitiveStack()
        multiplierCache[internalName] = result
        return result
    }

    fun getRecipes(internalName: NEUInternalName): Set<PrimitiveRecipe> {
        return recipesCache.getOrPut(internalName) {
            PrimitiveRecipe.convertMultiple(manager.getRecipesFor(internalName.asString())).toSet()
        }
    }

    fun PrimitiveRecipe.getCachedIngredients() = ingredientsCache.getOrPut(this) { ingredients }

    fun neuHasFocus(): Boolean {
        if (!PlatformUtils.isNeuLoaded()) return false
        if (AuctionSearchOverlay.shouldReplace()) return true
        if (BazaarSearchOverlay.shouldReplace()) return true
        // TODO add RecipeSearchOverlay via RecalculatingValue and reflection
        // https://github.com/NotEnoughUpdates/NotEnoughUpdates/blob/master/src/main/java/io/github/moulberry/notenoughupdates/overlays/RecipeSearchOverlay.java
        if (InventoryUtils.inStorage() && InventoryUtils.isNeuStorageEnabled) return true
        if (NEUOverlay.searchBarHasFocus) return true

        return false
    }

    // Uses NEU
    fun saveNBTData(item: ItemStack, removeLore: Boolean = true): String {
        val jsonObject = manager.getJsonForItem(item)
        if (!jsonObject.has("internalname")) {
            jsonObject.add("internalname", JsonPrimitive("_"))
        }
        if (removeLore && jsonObject.has("lore")) jsonObject.remove("lore")
        val jsonString = jsonObject.toString()
        return StringUtils.encodeBase64(jsonString)
    }

    fun loadNBTData(encoded: String): ItemStack {
        val jsonString = StringUtils.decodeBase64(encoded)
        val jsonObject = ConfigManager.gson.fromJson(jsonString, JsonObject::class.java)
        return manager.jsonToStack(jsonObject, false)
    }
}
