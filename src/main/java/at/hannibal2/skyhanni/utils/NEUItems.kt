package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.config.ConfigManager
import at.hannibal2.skyhanni.data.jsonobjects.other.HypixelApiTrophyFish
import at.hannibal2.skyhanni.data.jsonobjects.other.HypixelPlayerApiJson
import at.hannibal2.skyhanni.data.jsonobjects.repo.MultiFilterJson
import at.hannibal2.skyhanni.events.NeuProfileDataLoadedEvent
import at.hannibal2.skyhanni.events.NeuRepositoryReloadEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.ItemBlink.checkBlinkItem
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.asInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.isInt
import at.hannibal2.skyhanni.utils.PrimitiveIngredient.Companion.toPrimitiveItemStacks
import at.hannibal2.skyhanni.utils.PrimitiveItemStack.Companion.makePrimitiveStack
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getItemId
import at.hannibal2.skyhanni.utils.json.BaseGsonBuilder
import at.hannibal2.skyhanni.utils.json.fromJson
import at.hannibal2.skyhanni.utils.system.PlatformUtils
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import io.github.moulberry.notenoughupdates.NEUManager
import io.github.moulberry.notenoughupdates.NEUOverlay
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.events.ProfileDataLoadedEvent
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
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getNpcPrice as getNpcPriceNew
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getNpcPriceOrNull as getNpcPriceOrNullNew
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getPrice as getPriceNew
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getPriceOrNull as getPriceOrNullNew
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getRawCraftCostOrNull as getRawCraftCostOrNullNew

@SkyHanniModule
object NEUItems {

    val manager: NEUManager get() = NotEnoughUpdates.INSTANCE.manager
    private val multiplierCache = mutableMapOf<NEUInternalName, PrimitiveItemStack>()
    private val recipesCache = mutableMapOf<NEUInternalName, Set<PrimitiveRecipe>>()
    private val ingredientsCache = mutableMapOf<PrimitiveRecipe, Set<PrimitiveIngredient>>()
    private val itemIdCache = mutableMapOf<Item, List<NEUInternalName>>()

    private val hypixelApiGson by lazy {
        BaseGsonBuilder.gson()
            .registerTypeAdapter(
                HypixelApiTrophyFish::class.java,
                object : TypeAdapter<HypixelApiTrophyFish>() {
                    @Suppress("EmptyFunctionBlock")
                    override fun write(out: JsonWriter, value: HypixelApiTrophyFish) {}

                    override fun read(reader: JsonReader): HypixelApiTrophyFish {
                        val trophyFish = mutableMapOf<String, Int>()
                        var totalCaught = 0
                        reader.beginObject()
                        while (reader.hasNext()) {
                            val key = reader.nextName()
                            if (key == "total_caught") {
                                totalCaught = reader.nextInt()
                                continue
                            }
                            if (reader.peek() == JsonToken.NUMBER) {
                                val valueAsString = reader.nextString()
                                if (valueAsString.isInt()) {
                                    trophyFish[key] = valueAsString.toInt()
                                    continue
                                }
                            }
                            reader.skipValue()
                        }
                        reader.endObject()
                        return HypixelApiTrophyFish(totalCaught, trophyFish)
                    }
                }.nullSafe(),
            )
            .create()
    }

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

    @SubscribeEvent
    fun onProfileDataLoaded(event: ProfileDataLoadedEvent) {
        val apiData = event.data ?: return
        try {
            val playerData = hypixelApiGson.fromJson<HypixelPlayerApiJson>(apiData)
            NeuProfileDataLoadedEvent(playerData).postAndCatch()

        } catch (e: Exception) {
            ErrorManager.logErrorWithData(
                e, "Error reading hypixel player api data",
                "data" to apiData,
            )
        }
    }

    fun readAllNeuItems(): Map<String, NEUInternalName> {
        allInternalNames.clear()
        val map = mutableMapOf<String, NEUInternalName>()
        for (rawInternalName in allNeuRepoItems().keys) {
            var name = manager.createItem(rawInternalName).displayName.lowercase()

            // we ignore all builder blocks from the item name -> internal name cache
            // because builder blocks can have the same display name as normal items.
            if (rawInternalName.startsWith("BUILDER_")) continue

            val internalName = rawInternalName.asInternalName()

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
        ItemResolutionQuery(manager).withItemNBT(nbt).resolveInternalName()?.asInternalName()

    fun getInternalNameFromHypixelIdOrNull(hypixelId: String): NEUInternalName? {
        val internalName = hypixelId.replace(':', '-')
        return internalName.asInternalName().takeIf { it.getItemStackOrNull()?.getItemId() == internalName }
    }

    fun getInternalNameFromHypixelId(hypixelId: String): NEUInternalName =
        getInternalNameFromHypixelIdOrNull(hypixelId)
            ?: error("hypixel item id does not match internal name: $hypixelId")


    @Deprecated("Moved to ItemPriceUtils", ReplaceWith(""))
    fun NEUInternalName.getPrice(
        priceSource: ItemPriceSource = ItemPriceSource.BAZAAR_INSTANT_BUY,
        pastRecipes: List<PrimitiveRecipe> = emptyList(),
    ): Double = getPriceNew(priceSource, pastRecipes)

    @Deprecated("Moved to ItemPriceUtils", ReplaceWith(""))
    fun NEUInternalName.getNpcPrice(): Double = getNpcPriceNew()

    @Deprecated("Moved to ItemPriceUtils", ReplaceWith(""))
    fun NEUInternalName.getNpcPriceOrNull(): Double? = getNpcPriceOrNullNew()

    fun transHypixelNameToInternalName(hypixelId: String): NEUInternalName =
        manager.auctionManager.transformHypixelBazaarToNEUItemId(hypixelId).asInternalName()

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

    fun getItemStackOrNull(internalName: String) = internalName.asInternalName().getItemStackOrNull()

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

        Minecraft.getMinecraft().renderItem.renderItemIntoGUI(item, 0, 0)
        RenderHelper.disableStandardItemLighting()

        GlStateManager.popMatrix()
    }

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
        val result = allNeuRepoItems()
            .filter { Item.getByNameOrId(it.value.get("itemid").asString) == item }
            .keys.map { it.asInternalName() }
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
                if (internalName == "ENCHANTED_CACTUS_GREEN".asInternalName() && internalItemId == "INK_SACK-2".asInternalName()) {
                    internalItemId = "CACTUS".asInternalName()
                }

                // ignore wheat in enchanted cookie
                if (internalName == "ENCHANTED_COOKIE".asInternalName() && internalItemId == "WHEAT".asInternalName()) {
                    continue
                }

                // ignore golden carrot in enchanted golden carrot
                if (internalName == "ENCHANTED_GOLDEN_CARROT".asInternalName() && internalItemId == "GOLDEN_CARROT".asInternalName()) {
                    continue
                }

                // ignore rabbit hide in leather
                if (internalName == "LEATHER".asInternalName() && internalItemId == "RABBIT_HIDE".asInternalName()) {
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
