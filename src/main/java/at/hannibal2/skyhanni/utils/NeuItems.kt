package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.api.enoughupdates.EnoughUpdatesManager
import at.hannibal2.skyhanni.api.enoughupdates.ItemResolutionQuery
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigManager
import at.hannibal2.skyhanni.data.jsonobjects.repo.MultiFilterJson
import at.hannibal2.skyhanni.events.NeuRepositoryReloadEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.ItemBlink.checkBlinkItem
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getPriceOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.PrimitiveIngredient.Companion.toPrimitiveItemStacks
import at.hannibal2.skyhanni.utils.PrimitiveItemStack.Companion.makePrimitiveStack
import at.hannibal2.skyhanni.utils.system.PlatformUtils
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import io.github.moulberry.notenoughupdates.NEUOverlay
import io.github.moulberry.notenoughupdates.overlays.AuctionSearchOverlay
import io.github.moulberry.notenoughupdates.overlays.BazaarSearchOverlay
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GLAllocation
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object NeuItems {
    private val multiplierCache = mutableMapOf<NeuInternalName, PrimitiveItemStack>()
    private val itemIdCache = mutableMapOf<Item, List<NeuInternalName>>()

    var allItemsCache = mapOf<String, NeuInternalName>() // item name -> internal name
    val allInternalNames = mutableListOf<NeuInternalName>()
    val ignoreItemsFilter = MultiFilter()

    private val fallbackItem by lazy {
        ItemUtils.createItemStack(
            ItemStack(Blocks.barrier).item,
            "§cMissing Repo Item",
            "§cYour NEU repo seems to be out of date",
        )
    }

    @HandleEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val ignoredItems = event.getConstant<MultiFilterJson>("IgnoredItems")
        ignoreItemsFilter.load(ignoredItems)
    }

    @HandleEvent
    fun onNeuRepoReload(event: NeuRepositoryReloadEvent) {
        allItemsCache = readAllNeuItems()
    }

    fun readAllNeuItems(): Map<String, NeuInternalName> {
        allInternalNames.clear()
        val map = mutableMapOf<String, NeuInternalName>()
        for (rawInternalName in allNeuRepoItems().keys) {
            var name = getItemStackOrNull(rawInternalName)?.displayName?.lowercase() ?: continue

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

    fun getInternalName(itemStack: ItemStack): String? = ItemResolutionQuery()
        .withCurrentGuiContext()
        .withItemStack(itemStack)
        .resolveInternalName()

    fun getInternalNameOrNull(nbt: NBTTagCompound): NeuInternalName? =
        ItemResolutionQuery().withItemNbt(nbt).resolveInternalName()?.toInternalName()

    fun getInternalNameFromHypixelIdOrNull(hypixelId: String): NeuInternalName? {
        val internalName = hypixelId.replace(':', '-')
        return internalName.toInternalName().takeIf { it.getItemStackOrNull() != null }
    }

    fun getInternalNameFromHypixelId(hypixelId: String): NeuInternalName =
        getInternalNameFromHypixelIdOrNull(hypixelId)
            ?: error("hypixel item id does not match internal name: $hypixelId")

    fun transHypixelNameToInternalName(hypixelId: String): NeuInternalName =
        ItemResolutionQuery.transformHypixelBazaarToNeuItemId(hypixelId).toInternalName()

    fun NeuInternalName.getItemStackOrNull(): ItemStack? = ItemResolutionQuery()
        .withKnownInternalName(asString())
        .resolveToItemStack()?.copy()

    fun getItemStackOrNull(internalName: String) = internalName.toInternalName().getItemStackOrNull()

    fun NeuInternalName.getItemStack(): ItemStack =
        getItemStackOrNull() ?: run {
            getPriceOrNull() ?: return@run fallbackItem
            if (ignoreItemsFilter.match(this.asString())) return@run fallbackItem

            val name = this.toString()
            ItemUtils.addMissingRepoItem(name, "Could not create item stack for $name")
            fallbackItem
        }

    fun isVanillaItem(item: ItemStack): Boolean = item.getInternalName().isVanillaItem()

    private val hardcodedVanillaItems = listOf(
        "WOOD_AXE", "WOOD_HOE", "WOOD_PICKAXE", "WOOD_SPADE", "WOOD_SWORD",
        "GOLD_AXE", "GOLD_HOE", "GOLD_PICKAXE", "GOLD_SPADE", "GOLD_SWORD",
    )

    fun NeuInternalName.isVanillaItem(): Boolean {
        val asString = this.asString()
        if (hardcodedVanillaItems.contains(asString)) return true

        val vanillaName = asString.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        if (allNeuRepoItems().containsKey(vanillaName)) {
            val json = allNeuRepoItems()[vanillaName]
            if (json != null && json.has("vanilla") && json["vanilla"].asBoolean) return true
        }
        return Item.itemRegistry.getObject(ResourceLocation(vanillaName)) != null
    }

    fun NeuInternalName.removePrefix(prefix: String): NeuInternalName {
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

    fun allNeuRepoItems(): Map<String, JsonObject> = EnoughUpdatesManager.getItemInformation()

    fun getInternalNamesForItemId(item: Item): List<NeuInternalName> {
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

    fun getPrimitiveMultiplier(internalName: NeuInternalName, tryCount: Int = 0): PrimitiveItemStack {
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

            val map = mutableMapOf<NeuInternalName, Int>()
            for (ingredient in recipe.ingredients.toPrimitiveItemStacks()) {
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

    fun getRecipes(internalName: NeuInternalName): Set<PrimitiveRecipe> = EnoughUpdatesManager.getRecipesFor(internalName)

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
        val jsonObject = EnoughUpdatesManager.stackToJson(item)
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
        return EnoughUpdatesManager.jsonToStack(jsonObject, false)
    }
}
