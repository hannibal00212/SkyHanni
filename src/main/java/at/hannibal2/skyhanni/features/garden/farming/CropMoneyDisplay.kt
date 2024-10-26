package at.hannibal2.skyhanni.features.garden.farming

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.features.garden.MoneyPerHourConfig.CustomFormatEntry
import at.hannibal2.skyhanni.events.GardenToolChangeEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.garden.CropType.Companion.getByNameOrNull
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.features.garden.GardenNextJacobContest
import at.hannibal2.skyhanni.features.garden.farming.GardenCropSpeed.getSpeed
import at.hannibal2.skyhanni.features.garden.farming.GardenCropSpeed.isSpeedDataEmpty
import at.hannibal2.skyhanni.features.inventory.bazaar.BazaarApi.getBazaarData
import at.hannibal2.skyhanni.features.inventory.bazaar.BazaarApi.isBazaarItem
import at.hannibal2.skyhanni.features.inventory.bazaar.BazaarData
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.addAsSingletonList
import at.hannibal2.skyhanni.utils.CollectionUtils.moveEntryToTop
import at.hannibal2.skyhanni.utils.CollectionUtils.sortedDesc
import at.hannibal2.skyhanni.utils.ConfigUtils
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getNpcPrice
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.itemNameWithoutColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.asInternalName
import at.hannibal2.skyhanni.utils.NEUItems
import at.hannibal2.skyhanni.utils.NEUItems.getItemStack
import at.hannibal2.skyhanni.utils.NEUItems.getNpcPriceOrNull
import at.hannibal2.skyhanni.utils.NEUItems.getPrice
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RenderUtils.renderStringsAndItems
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getReforgeName
import kotlinx.coroutines.launch
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object CropMoneyDisplay {

    var multipliers = mapOf<NEUInternalName, Int>()
    private var showCalculation = false

    fun toggleShowCalculation() {
        showCalculation = !showCalculation
        ChatUtils.chat("Show crop money calculation: " + if (showCalculation) "enabled" else "disabled")
        update()
    }

    private var display = emptyList<List<Any>>()
    private val config get() = GardenAPI.config.moneyPerHours
    private var loaded = false
    private var ready = false
    private val cropNames = mutableMapOf<NEUInternalName, CropType>()
    private val toolHasBountiful get() = GardenAPI.storage?.toolWithBountiful

    val BOX_OF_SEEDS by lazy { "BOX_OF_SEEDS".asInternalName().getItemStack() }

    @SubscribeEvent
    fun onProfileJoin(event: ProfileJoinEvent) {
        display = emptyList()
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return

        if (!GardenAPI.hideExtraGuis()) {
            config.pos.renderStringsAndItems(display, posLabel = "Garden Money Per Hour")
        }
    }

    @SubscribeEvent
    fun onGardenToolChange(event: GardenToolChangeEvent) {
        update()
    }

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return
        if (!event.repeatSeconds(5)) return

        if (GardenAPI.getCurrentlyFarmedCrop() == null && !config.alwaysOn) return

        update()
    }

    private fun update() {
        init()

        display = drawDisplay()
    }

    private fun drawDisplay(): List<List<Any>> {
        val newDisplay = mutableListOf<List<Any>>()

        val title = if (config.compact) {
            "§7Money/Hour:"
        } else {
            "§7Money per Hour when selling:"
        }

        if (!ready) {
            newDisplay.addAsSingletonList(title)
            newDisplay.addAsSingletonList("§eLoading...")
            return newDisplay
        }

        if (GardenAPI.getCurrentlyFarmedCrop() == null && !config.alwaysOn) return newDisplay

        newDisplay.addAsSingletonList(fullTitle(title))

        if (!GardenAPI.config.cropMilestones.progress) {
            newDisplay.addAsSingletonList("§cCrop Milestone Progress Display is disabled!")
            return newDisplay
        }

        var extraMushroomCowPerkCoins = 0.0
        var extraDicerCoins = 0.0
        var extraArmorCoins = 0.0
        GardenAPI.getCurrentlyFarmedCrop()?.let {
            val reforgeName = InventoryUtils.getItemInHand()?.getReforgeName()
            toolHasBountiful?.put(it, reforgeName == "bountiful")

            if (GardenAPI.mushroomCowPet && it != CropType.MUSHROOM && config.mooshroom) {
                val redMushroom = "ENCHANTED_RED_MUSHROOM".asInternalName()
                val brownMushroom = "ENCHANTED_BROWN_MUSHROOM".asInternalName()
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
                extraMushroomCowPerkCoins = perSecond * 60 * 60
            }

            val itemInHand = InventoryUtils.getItemInHand()?.getInternalName()
            if (itemInHand?.contains("DICER") == true && config.dicer) {
                val (dicerDrops, internalName) = when (it) {
                    CropType.MELON -> GardenCropSpeed.latestMelonDicer to "ENCHANTED_MELON".asInternalName()
                    CropType.PUMPKIN -> GardenCropSpeed.latestPumpkinDicer to "ENCHANTED_PUMPKIN".asInternalName()

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
                extraDicerCoins = 60 * 60 * GardenCropSpeed.getRecentBPS() * dicerDrops * price
            }

            if (config.armor) {
                val amountPerHour =
                    it.multiplier * GardenCropSpeed.getRecentBPS() * ArmorDropTracker.getDropsPerHour(it)
                extraArmorCoins = amountPerHour * it.specialDropType.asInternalName().getNpcPrice()
            }
        }

        val moneyPerHourData = calculateMoneyPerHour(newDisplay)
        if (moneyPerHourData.isEmpty()) {
            if (!isSpeedDataEmpty()) {
                val message = "money/hr empty but speed data not empty, retry"
                ChatUtils.debug(message)
                newDisplay.addAsSingletonList("§eStill Loading...")
                ready = false
                loaded = false
                return newDisplay
            }
            newDisplay.addAsSingletonList("§cFarm crops to add them to this list!")
            return newDisplay
        }

        var number = 0
        val help = moneyPerHourData.mapValues { (_, value) -> value.max() }
        for (internalName in help.sortedDesc().keys) {
            number++
            val crop = cropNames[internalName]!!
            val isCurrent = crop == GardenAPI.getCurrentlyFarmedCrop()
            if (number > config.showOnlyBest && (!config.showCurrent || !isCurrent)) continue
            val debug = isCurrent && showCalculation
            if (debug) {
                newDisplay.addAsSingletonList("final calculation for: $internalName/$crop")
            }

            val list = mutableListOf<Any>()
            if (!config.compact) {
                list.add("§7$number# ")
            }

            try {
                if (isSeeds(internalName)) {
                    list.add(BOX_OF_SEEDS)
                } else {
                    list.add(internalName.getItemStack())
                }

                if (cropNames[internalName] == CropType.WHEAT && config.mergeSeeds) {
                    list.add(BOX_OF_SEEDS)
                }
            } catch (e: NullPointerException) {
                ErrorManager.logErrorWithData(
                    e, "Error calculating seed price for money per hour display",
                    "internalName" to internalName,
                    "cropNames" to cropNames,
                    "list" to list,
                )
            }

            if (!config.compact) {
                val itemName = internalName.itemNameWithoutColor
                val currentColor = if (isCurrent) "§e" else "§7"
                val contestFormat = if (GardenNextJacobContest.isNextCrop(crop)) "§n" else ""
                list.add("$currentColor$contestFormat$itemName§7: ")
            }

            val coinsColor = if (isCurrent && config.compact) "§e" else "§6"
            val moneyArray = moneyPerHourData[internalName].orEmpty()

            for (price in moneyArray) {
                val finalPrice = price + extraMushroomCowPerkCoins + extraDicerCoins + extraArmorCoins
                val format = format(finalPrice)
                if (debug) {
                    newDisplay.addAsSingletonList(" price: ${price.addSeparators()}")
                    newDisplay.addAsSingletonList(" extraMushroomCowPerkCoins: ${extraMushroomCowPerkCoins.addSeparators()}")
                    newDisplay.addAsSingletonList(" extraArmorCoins: ${extraArmorCoins.addSeparators()}")
                    newDisplay.addAsSingletonList(" extraDicerCoins: ${extraDicerCoins.addSeparators()}")
                    newDisplay.addAsSingletonList(" finalPrice: ${finalPrice.addSeparators()}")
                }
                list.add("$coinsColor$format")
                list.add("§7/")
            }
            list.removeLast()

            newDisplay.add(list)
        }
        return if (config.hideTitle) newDisplay.drop(1) else newDisplay
    }

    // TODO : Rewrite to not be index-reliant
    private fun fullTitle(title: String): String {
        val titleText: String
        val nameList = mutableListOf<String>()
        if (config.useCustomFormat) {
            val map = mapOf(
                0 to "Sell Offer",
                1 to "Instant Sell",
                2 to "NPC Price",
            )
            val list = mutableListOf<String>()
            for (index in config.customFormat) {
                // TODO, change functionality to use enum rather than ordinals
                map[index.ordinal]?.let {
                    list.add(it)
                }
            }
            for (line in list) {
                nameList.add("§e$line")
                nameList.add("§7/")
            }
            nameList.removeLast()
            titleText = nameList.joinToString("")
        } else {
            titleText = if (LorenzUtils.noTradeMode) "§eNPC Price" else "§eSell Offer"
        }
        return "$title §7($titleText§7)"
    }

    private fun format(moneyPerHour: Double) = if (config.compactPrice) {
        moneyPerHour.shortFormat()
    } else {
        moneyPerHour.toLong().addSeparators()
    }

    private fun calculateMoneyPerHour(debugList: MutableList<List<Any>>): Map<NEUInternalName, Array<Double>> {
        val moneyPerHours = mutableMapOf<NEUInternalName, Array<Double>>()

        var seedsPrice: BazaarData? = null
        var seedsPerHour = 0.0

        val onlyNpcPrice =
            (!config.useCustomFormat && LorenzUtils.noTradeMode) ||
                (config.useCustomFormat && config.customFormat.singleOrNull() == CustomFormatEntry.NPC_PRICE)

        for ((internalName, amount) in multipliers.moveEntryToTop { isSeeds(it.key) }) {
            if (internalName.equals("BOX_OF_SEEDS")) continue

            val crop = cropNames[internalName]!!
            // When only the NPC price is shown, display the price only for the base item
            if (onlyNpcPrice) {
                if (amount != 1) continue
            } else {
                if (amount < 10) {
                    continue
                }
            }

            var speed = crop.getSpeed()?.toDouble() ?: continue

            val isCurrent = crop == GardenAPI.getCurrentlyFarmedCrop()
            val debug = isCurrent && showCalculation
            if (debug) {
                debugList.addAsSingletonList("calculateMoneyPerHour: $internalName/$crop")
                debugList.addAsSingletonList(" speed: ${speed.addSeparators()}")
            }

            val isSeeds = isSeeds(internalName)
            if (debug) {
                debugList.addAsSingletonList(" isSeeds: $isSeeds")
            }
            if (isSeeds) speed *= 1.36
            if (crop.replenish) {
                val blockPerSecond = crop.multiplier * GardenCropSpeed.getRecentBPS()
                if (debug) {
                    debugList.addAsSingletonList(" replenish blockPerSecond reduction: ${blockPerSecond.addSeparators()}")
                }
                speed -= blockPerSecond
            }

            val speedPerHour = speed * 60 * 60
            if (debug) {
                debugList.addAsSingletonList(" speedPerHour: ${speedPerHour.addSeparators()}")
            }
            val cropsPerHour = speedPerHour / amount.toDouble()
            if (debug) {
                debugList.addAsSingletonList(" cropsPerHour: ${cropsPerHour.addSeparators()}")
            }

            val bazaarData = internalName.getBazaarData() ?: continue

            var npcPrice = internalName.getNpcPrice() * cropsPerHour
            var sellOffer = bazaarData.sellOfferPrice * cropsPerHour
            var instantSell = bazaarData.instantBuyPrice * cropsPerHour
            if (debug) {
                debugList.addAsSingletonList(" npcPrice: ${npcPrice.addSeparators()}")
                debugList.addAsSingletonList(" sellOffer: ${sellOffer.addSeparators()}")
                debugList.addAsSingletonList(" instantSell: ${instantSell.addSeparators()}")
            }

            if (crop == CropType.WHEAT && config.mergeSeeds) {
                if (isSeeds) {
                    seedsPrice = bazaarData
                    seedsPerHour = cropsPerHour
                    continue
                } else {
                    seedsPrice?.let {
                        if (debug) {
                            debugList.addAsSingletonList(" added seedsPerHour: $seedsPerHour")
                        }
                        val factor = NEUItems.getPrimitiveMultiplier(internalName).amount
                        npcPrice += "SEEDS".asInternalName().getNpcPrice() * seedsPerHour / factor
                        sellOffer += it.sellOfferPrice * seedsPerHour
                        instantSell += it.instantBuyPrice * seedsPerHour
                    }
                }
            }

            val bountifulMoney =
                if (toolHasBountiful?.get(crop) == true && config.bountiful) speedPerHour * 0.2 else 0.0
            if (debug && bountifulMoney > 0.0) {
                debugList.addAsSingletonList(" bountifulCoins: ${bountifulMoney.addSeparators()}")
            }
            moneyPerHours[internalName] =
                formatNumbers(sellOffer + bountifulMoney, instantSell + bountifulMoney, npcPrice + bountifulMoney)
        }
        return moneyPerHours
    }

    private fun isSeeds(internalName: NEUInternalName) =
        internalName.equals("ENCHANTED_SEEDS") || internalName.equals("SEEDS")

    // TODO : Rewrite to not be index-reliant
    private fun formatNumbers(sellOffer: Double, instantSell: Double, npcPrice: Double): Array<Double> {
        return if (config.useCustomFormat) {
            val map = mapOf(
                0 to sellOffer,
                1 to instantSell,
                2 to npcPrice,
            )
            val newList = mutableListOf<Double>()
            for (index in config.customFormat) {
                // TODO, change functionality to use enum rather than ordinals
                map[index.ordinal]?.let {
                    newList.add(it)
                }
            }
            newList.toTypedArray()
        } else {
            if (LorenzUtils.noTradeMode) {
                arrayOf(npcPrice)
            } else {
                arrayOf(sellOffer)
            }
        }
    }

    private fun init() {
        if (loaded) return
        loaded = true

        SkyHanniMod.coroutineScope.launch {
            val map = mutableMapOf<NEUInternalName, Int>()
            for ((rawInternalName, _) in NEUItems.allNeuRepoItems()) {
                if (rawInternalName == "ENCHANTED_PAPER") continue
                if (rawInternalName == "ENCHANTED_BREAD") continue
                if (rawInternalName == "SIMPLE_CARROT_CANDY") continue
                val internalName = rawInternalName.asInternalName()
                if (!internalName.isBazaarItem()) continue

                val (newId, amount) = NEUItems.getPrimitiveMultiplier(internalName)
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

    private fun isEnabled() = GardenAPI.inGarden() && config.display

    @SubscribeEvent
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
}
