package at.hannibal2.skyhanni.features.garden.farming

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.features.garden.MoneyPerHourConfig.CustomFormatEntry
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.GardenToolChangeEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.garden.CropType.Companion.getByNameOrNull
import at.hannibal2.skyhanni.features.garden.GardenApi
import at.hannibal2.skyhanni.features.garden.GardenNextJacobContest
import at.hannibal2.skyhanni.features.garden.farming.GardenCropSpeed.getSpeed
import at.hannibal2.skyhanni.features.garden.farming.GardenCropSpeed.isSpeedDataEmpty
import at.hannibal2.skyhanni.features.inventory.bazaar.BazaarApi.getBazaarData
import at.hannibal2.skyhanni.features.inventory.bazaar.BazaarApi.isBazaarItem
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.moveEntryToTop
import at.hannibal2.skyhanni.utils.ConfigUtils
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getNpcPrice
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getNpcPriceOrNull
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getPrice
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.itemNameWithoutColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NeuItems
import at.hannibal2.skyhanni.utils.NeuItems.getItemStack
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderable
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getReforgeName
import at.hannibal2.skyhanni.utils.renderables.Container
import at.hannibal2.skyhanni.utils.renderables.Renderable
import kotlinx.coroutines.launch

@SkyHanniModule
object CropMoneyDisplay {

    var multipliers = mapOf<NeuInternalName, Int>()

    private var display: Renderable? = null
    private val config get() = GardenApi.config.moneyPerHours
    private var loaded = false
    private var ready = false
    private val cropNames = mutableMapOf<NeuInternalName, CropType>()
    private val toolHasBountiful get() = GardenApi.storage?.toolWithBountiful

    private var moneyPerHour: Map<NeuInternalName, CropMoneyData> = mutableMapOf()
    private val extraMoneyPerHour: ExtraMoneyData = ExtraMoneyData(0.0, 0.0, 0.0)

    private val BOX_OF_SEEDS by lazy { "BOX_OF_SEEDS".toInternalName().getItemStack() }
    private val SEEDS = "SEEDS".toInternalName()
    private val ENCHANTED_SEEDS = "ENCHANTED_SEEDS".toInternalName()

    @HandleEvent
    fun onProfileJoin(event: ProfileJoinEvent) {
        display = null
    }

    @HandleEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return

        if (!GardenApi.hideExtraGuis()) {
            config.pos.renderRenderable(display, posLabel = "Garden Money Per Hour")
        }
    }

    @HandleEvent
    fun onGardenToolChange(event: GardenToolChangeEvent) {
        update()
    }

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return
        if (!event.repeatSeconds(5)) return

        if (GardenApi.getCurrentlyFarmedCrop() == null && !config.alwaysOn) return

        update()
    }

    @HandleEvent
    fun onDebug(event: DebugDataCollectEvent) {
        event.title("Crop Money - Extra")
        event.addIrrelevant(extraMoneyPerHour.toString())
        event.title("Crop Money - Crop")
        event.addIrrelevant {
            val currentCrop = GardenApi.getCurrentlyFarmedCrop()
            for (data in moneyPerHour.values) {
                if (data.crop == currentCrop) {
                    add(data.toString())
                    add(" ")
                }
            }
        }
    }

    private fun update() {
        init()
        display = buildDisplay()
    }

    private fun buildDisplay(): Renderable? {
        val title = if (config.compact) {
            "§7Money/Hour:"
        } else {
            "§7Money per Hour when selling:"
        }

        if (!ready) {
            return Container.vertical {
                string(title)
                string("§eLoading...")
            }
        }

        if (GardenApi.getCurrentlyFarmedCrop() == null && !config.alwaysOn) return null

        return Container.vertical {
            if (!config.hideTitle) string(fullTitle(title))

            if (!GardenApi.config.cropMilestones.progress) {
                string("§cCrop Milestone Progress Display is disabled!")
            } else {
                renderable(buildDisplayBody())
            }
        }
    }

    private fun buildDisplayBody(): Renderable {
        GardenApi.getCurrentlyFarmedCrop()?.let {
            val reforgeName = InventoryUtils.getItemInHand()?.getReforgeName()
            toolHasBountiful?.put(it, reforgeName == "bountiful")

            if (GardenApi.mushroomCowPet && it != CropType.MUSHROOM && config.mooshroom) {
                val redMushroom = "ENCHANTED_RED_MUSHROOM".toInternalName()
                val brownMushroom = "ENCHANTED_BROWN_MUSHROOM".toInternalName()
                val (redPrice, brownPrice) = if (LorenzUtils.noTradeMode) {
                    val redPrice = (redMushroom.getNpcPriceOrNull() ?: 160.0) / 160
                    val brownPrice = (brownMushroom.getNpcPriceOrNull() ?: 160.0) / 160
                    redPrice to brownPrice
                } else {
                    val redPrice = redMushroom.getPrice() / 160
                    val brownPrice = brownMushroom.getPrice() / 160
                    redPrice to brownPrice
                }

                val mushroomPrice = (redPrice + brownPrice) / 2
                val perSecond = GardenCropSpeed.getRecentBPS() * it.multiplier * mushroomPrice
                extraMoneyPerHour.mushroomCowCoins = perSecond * 60 * 60
            }

            val itemInHand = InventoryUtils.getItemInHand()?.getInternalName()
            if (itemInHand?.contains("DICER") == true && config.dicer) {
                val (dicerDrops, internalName) = when (it) {
                    CropType.MELON -> GardenCropSpeed.latestMelonDicer to "ENCHANTED_MELON".toInternalName()
                    CropType.PUMPKIN -> GardenCropSpeed.latestPumpkinDicer to "ENCHANTED_PUMPKIN".toInternalName()

                    else -> ErrorManager.skyHanniError(
                        "Unknown dicer detected.",
                        "crop" to it,
                        "item in hand" to itemInHand,
                    )
                }
                val bazaarData = internalName.getBazaarData()
                val price =
                    if (LorenzUtils.noTradeMode || bazaarData == null) internalName.getNpcPrice() / 160
                    else (bazaarData.instantBuyPrice + bazaarData.sellOfferPrice) / 320
                extraMoneyPerHour.dicerCoins = 60 * 60 * GardenCropSpeed.getRecentBPS() * dicerDrops * price
            }

            if (config.armor) {
                val amountPerHour = GardenCropSpeed.getRecentBPS() * ArmorDropTracker.getDropsPerHour(it)
                extraMoneyPerHour.armorCoins = amountPerHour * it.specialDropType.toInternalName().getNpcPrice()
            }
        }

        moneyPerHour = calculateMoneyPerHour()
        if (moneyPerHour.isEmpty()) {
            if (!isSpeedDataEmpty()) {
                val message = "money/hr empty but speed data not empty, retry"
                ChatUtils.debug(message)
                ready = false
                loaded = false
                return Renderable.string("§eStill Loading...")
            }
            return Renderable.string("§cFarm crops to add them to this list!")
        }
        val cropList = createDescendingCropList(moneyPerHour)
        return Container.vertical {
            for ((index, pair) in cropList.withIndex()) {
                renderable(buildCropMoneyLine(index + 1, pair.first, pair.second, extraMoneyPerHour.total))
            }
        }
    }

    private fun createDescendingCropList(
        moneyPerHour: Map<NeuInternalName, CropMoneyData>
    ): List<Pair<NeuInternalName, List<Double>>> {
        val defaultPrices = listOf(0.0, 0.0, 0.0)
        val enchantedSeedPrices = moneyPerHour[ENCHANTED_SEEDS]?.toPrices() ?: defaultPrices
        val seedPrices = moneyPerHour[SEEDS]?.toPrices() ?: defaultPrices
        val bestSeedPrices = enchantedSeedPrices.zip(seedPrices).map { (a, b) -> a.coerceAtLeast(b) }

        val priceList: MutableList<Pair<NeuInternalName, List<Double>>> = mutableListOf()
        for ((internalName, cropMoneyData) in moneyPerHour) {
            if (config.mergeSeeds && isSeeds(internalName)) continue
            var prices = cropMoneyData.toPrices()
            if (config.mergeSeeds && cropMoneyData.crop == CropType.WHEAT) {
                prices = prices.zip(bestSeedPrices).map { (a, b) -> a + b }
            }
            priceList.add(internalName to prices)
        }
        priceList.sortByDescending { it.second.max() }
        return priceList
    }

    private fun buildCropMoneyLine(
        number: Int,
        internalName: NeuInternalName,
        prices: List<Double>,
        extraMoneyPerHour: Double
    ): Renderable? {
        val crop = cropNames[internalName]!!
        val isCurrent = crop == GardenApi.getCurrentlyFarmedCrop()
        if (number > config.showOnlyBest && (!config.showCurrent || !isCurrent)) return null

        return Container.horizontal {
            if (!config.compact) string("§7$number# ")

            if (isSeeds(internalName)) {
                item(BOX_OF_SEEDS)
            } else {
                item(internalName)
            }

            if (cropNames[internalName] == CropType.WHEAT && config.mergeSeeds) {
                item(BOX_OF_SEEDS)
            }

            if (!config.compact) {
                val itemName = internalName.itemNameWithoutColor
                val currentColor = if (isCurrent) "§e" else "§7"
                val contestFormat = if (GardenNextJacobContest.isNextCrop(crop)) "§n" else ""
                string("$currentColor$contestFormat$itemName§7: ")
            }

            val coinsColor = if (config.compact && GardenApi.getCurrentlyFarmedCrop() == crop) "§e" else "§6"
            val coins = prices.joinToString("§7/") {
                val finalPrice = it + extraMoneyPerHour
                val formattedPrice = if (config.compactPrice) {
                    finalPrice.shortFormat()
                } else {
                    finalPrice.toLong().addSeparators()
                }
                "$coinsColor$formattedPrice"
            }
            string(coins)
        }
    }

    private fun fullTitle(title: String): String {
        return currentFormats().joinToString("§7/", "$title §7(", "§7)")
    }

    private fun calculateMoneyPerHour(): Map<NeuInternalName, CropMoneyData> {
        val moneyPerHours = mutableMapOf<NeuInternalName, CropMoneyData>()

        val onlyNpcPrice =
            (!config.useCustomFormat && LorenzUtils.noTradeMode) ||
                (config.useCustomFormat && config.customFormat.singleOrNull() == CustomFormatEntry.NPC_PRICE)

        for ((internalName, amount) in multipliers.moveEntryToTop { isSeeds(it.key) }) {
            if (internalName == BOX_OF_SEEDS.getInternalName()) continue
            calculateCropMoney(internalName, onlyNpcPrice, amount)?.let { moneyPerHours[internalName] = it }
        }

        return moneyPerHours
    }

    private fun calculateCropMoney(internalName: NeuInternalName, onlyNpcPrice: Boolean, amount: Int): CropMoneyData? {
        if (internalName == BOX_OF_SEEDS.getInternalName()) return null

        val crop = cropNames[internalName]!!

        // When only the NPC price is shown, display the price only for the base item
        if (onlyNpcPrice) {
            if (amount != 1) return null
        } else {
            if (amount < 10) {
                return null
            }
        }

        var speed = crop.getSpeed()?.toDouble() ?: return null

        val isSeeds = isSeeds(internalName)
        if (isSeeds) speed *= 1.36
        val replenishReduction = if (crop.replenish) (crop.multiplier * GardenCropSpeed.getRecentBPS()) else 0.0
        speed -= replenishReduction

        val speedPerHour = speed * 60 * 60
        val cropsPerHour = speedPerHour / amount.toDouble()
        val bazaarData = internalName.getBazaarData() ?: return null

        val npcCoins = internalName.getNpcPrice() * cropsPerHour
        val sellOfferCoins = bazaarData.sellOfferPrice * cropsPerHour
        val instantSellCoins = bazaarData.instantBuyPrice * cropsPerHour
        val bountifulCoins = if (toolHasBountiful?.get(crop) == true && config.bountiful) speedPerHour * 0.2 else 0.0

        return CropMoneyData(
            crop,
            internalName,
            speed,
            replenishReduction,
            speedPerHour,
            cropsPerHour,
            npcCoins,
            sellOfferCoins,
            instantSellCoins,
            bountifulCoins,
        )
    }

    private fun currentFormats() =
        if (config.useCustomFormat) {
            config.customFormat
        } else if (LorenzUtils.noTradeMode) {
            listOf(CustomFormatEntry.NPC_PRICE)
        } else {
            listOf(CustomFormatEntry.SELL_OFFER)
        }

    private fun isSeeds(internalName: NeuInternalName) = internalName == ENCHANTED_SEEDS || internalName == SEEDS

    private fun init() {
        if (loaded) return
        loaded = true

        SkyHanniMod.coroutineScope.launch {
            val map = mutableMapOf<NeuInternalName, Int>()
            for ((rawInternalName, _) in NeuItems.allNeuRepoItems()) {
                if (rawInternalName == "ENCHANTED_PAPER") continue
                if (rawInternalName == "ENCHANTED_BREAD") continue
                if (rawInternalName == "SIMPLE_CARROT_CANDY") continue
                val internalName = rawInternalName.toInternalName()
                if (!internalName.isBazaarItem()) continue

                val (newId, amount) = NeuItems.getPrimitiveMultiplier(internalName)
                val itemName = newId.itemNameWithoutColor
                val crop = getByNameOrNull(itemName)
                crop?.let {
                    map[internalName] = amount
                    cropNames[internalName] = it
                }
            }

            multipliers = map

            ready = true
            update()
        }
    }

    private fun isEnabled() = GardenApi.inGarden() && config.display

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "garden.moneyPerHourDisplay", "garden.moneyPerHours.display")
        event.move(3, "garden.moneyPerHourShowOnlyBest", "garden.moneyPerHours.showOnlyBest")
        event.move(3, "garden.moneyPerHourShowCurrent", "garden.moneyPerHours.showCurrent")
        event.move(3, "garden.moneyPerHourAlwaysOn", "garden.moneyPerHours.alwaysOn")
        event.move(3, "garden.moneyPerHourCompact", "garden.moneyPerHours.compact")
        event.move(3, "garden.moneyPerHourCompactPrice", "garden.moneyPerHours.compactPrice")
        event.move(3, "garden.moneyPerHourUseCustomFormat", "garden.moneyPerHours.useCustomFormat")
        event.move(3, "garden.moneyPerHourCustomFormat", "garden.moneyPerHours.customFormat")
        event.move(3, "garden.moneyPerHourMergeSeeds", "garden.moneyPerHours.mergeSeeds")
        event.move(3, "garden.moneyPerHourBountiful", "garden.moneyPerHours.bountiful")
        event.move(3, "garden.moneyPerHourMooshroom", "garden.moneyPerHours.mooshroom")
        event.move(3, "garden.moneyPerHourArmor", "garden.moneyPerHours.armor")
        event.move(3, "garden.moneyPerHourDicer", "garden.moneyPerHours.dicer")
        event.move(3, "garden.moneyPerHourHideTitle", "garden.moneyPerHours.hideTitle")
        event.move(3, "garden.moneyPerHourPos", "garden.moneyPerHours.pos")
        event.transform(11, "garden.moneyPerHours.customFormat") { element ->
            ConfigUtils.migrateIntArrayListToEnumArrayList(element, CustomFormatEntry::class.java)
        }
    }

    private fun CropMoneyData.toPrices(): List<Double> {
        val formats = currentFormats()
        return formats.map { getCoins(it, config.bountiful) }
    }

    data class ExtraMoneyData(
        var mushroomCowCoins: Double,
        var armorCoins: Double,
        var dicerCoins: Double
    ) {
        override fun toString(): String = """
            extraMushroomCowPerkCoins: ${mushroomCowCoins.addSeparators()}
            extraArmorCoins: ${armorCoins.addSeparators()}
            extraDicerCoins: ${dicerCoins.addSeparators()}
        """.trimIndent()

        val total get() = armorCoins + dicerCoins + mushroomCowCoins
    }

    data class CropMoneyData(
        val crop: CropType,
        val exactCrop: NeuInternalName,
        val speed: Double,
        val replenishReduction: Double,
        val speedPerHour: Double,
        val cropsPerHour: Double,
        val npcCoins: Double,
        val sellOfferCoins: Double,
        val instantSellCoins: Double,
        val bountifulCoins: Double,
    ) {
        override fun toString(): String = """
            speed: ${speed.addSeparators()}
            ${if (replenishReduction != 0.0) "replenish reduction: ${replenishReduction.addSeparators()}" else ""}
            crop: $crop ($exactCrop)
            speedPerHour: ${speedPerHour.addSeparators()}
            cropsPerHour: ${cropsPerHour.addSeparators()}
            npcCoins: ${npcCoins.addSeparators()}
            sellOfferCoins: ${sellOfferCoins.addSeparators()}
            instantSellCoins: ${instantSellCoins.addSeparators()}
            ${if (bountifulCoins > 0.0) "bountifulCoins: ${bountifulCoins.addSeparators()}" else ""}
        """.trimIndent()

        fun getCoins(sellType: CustomFormatEntry, bountiful: Boolean): Double {
            val coins = when (sellType) {
                CustomFormatEntry.SELL_OFFER -> sellOfferCoins
                CustomFormatEntry.INSTANT_SELL -> instantSellCoins
                CustomFormatEntry.NPC_PRICE -> npcCoins
            }
            return coins + if (bountiful) bountifulCoins else 0.0
        }
    }
}
