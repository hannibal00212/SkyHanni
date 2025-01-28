package at.hannibal2.skyhanni.features.garden.composter

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.features.garden.composter.ComposterConfig
import at.hannibal2.skyhanni.config.features.garden.composter.ComposterConfig.RetrieveFromEntry
import at.hannibal2.skyhanni.data.SackApi.getAmountInSacksOrNull
import at.hannibal2.skyhanni.data.jsonobjects.repo.GardenJson
import at.hannibal2.skyhanni.data.model.ComposterUpgrade
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.NeuRepositoryReloadEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.TabListUpdateEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.events.minecraft.ToolTipEvent
import at.hannibal2.skyhanni.features.garden.GardenApi
import at.hannibal2.skyhanni.features.garden.composter.ComposterApi.getLevel
import at.hannibal2.skyhanni.features.inventory.bazaar.BazaarApi
import at.hannibal2.skyhanni.features.misc.items.EstimatedItemValue
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.addAsSingletonList
import at.hannibal2.skyhanni.utils.CollectionUtils.sortedDesc
import at.hannibal2.skyhanni.utils.ConfigUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.InventoryUtils.getAmountInInventory
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getPrice
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.KeyboardManager
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.addSelector
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.NONE
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NeuItems
import at.hannibal2.skyhanni.utils.NeuItems.getItemStack
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.romanToDecimalIfNecessary
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.renderStringsAndItems
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.renderables.Renderable
import java.util.Collections
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@SkyHanniModule
object ComposterOverlay {

    private var organicMatterFactors: Map<NeuInternalName, Double> = emptyMap()
    private var fuelFactors: Map<NeuInternalName, Double> = emptyMap()
    private var organicMatter: Map<NeuInternalName, Double> = emptyMap()

    private val config get() = GardenApi.config.composters
    private var organicMatterDisplay = emptyList<List<Any>>()
    private var fuelExtraDisplay = emptyList<List<Any>>()

    private var currentTimeType = TimeType.HOUR
    private var inComposter = false
    private var inComposterUpgrades = false
    private var extraComposterUpgrade: ComposterUpgrade? = null
        set(value) {
            field = value
            lastHovered = System.currentTimeMillis()
        }

    private var maxLevel = false
    private var lastHovered = 0L
    private var lastAttemptTime = SimpleTimeMark.farPast()

    var inInventory = false

    private var testOffset = 0

    var currentOrganicMatterItem: NeuInternalName?
        get() = GardenApi.storage?.composterCurrentOrganicMatterItem
        private set(value) {
            GardenApi.storage?.composterCurrentOrganicMatterItem = value
        }

    var currentFuelItem: NeuInternalName?
        get() = GardenApi.storage?.composterCurrentFuelItem
        private set(value) {
            GardenApi.storage?.composterCurrentFuelItem = value
        }

    fun onCommand(args: Array<String>) {
        if (args.size != 1) {
            ChatUtils.userError("Usage: /shtestcomposter <offset>")
            return
        }
        testOffset = args[0].toInt()
        ChatUtils.chat("Composter test offset set to $testOffset.")
    }

    private val COMPOST = "COMPOST".toInternalName()
    private val BIOFUEL = "BIOFUEL".toInternalName()
    private val VOLTA = "VOLTA".toInternalName()
    private val OIL_BARREL = "OIL_BARREL".toInternalName()

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inInventory = false
    }

    @HandleEvent(priority = HandleEvent.LOW)
    fun onTabListUpdate(event: TabListUpdateEvent) {
        if (!inInventory) return

        update()
    }

    @HandleEvent
    fun onTick(event: SkyHanniTickEvent) {
        if (!GardenApi.inGarden()) return
        if (inComposterUpgrades && extraComposterUpgrade != null && System.currentTimeMillis() > lastHovered + 200) {
            extraComposterUpgrade = null
            update()
        }
    }

    @HandleEvent
    fun onInventoryFullyOpened(event: InventoryFullyOpenedEvent) {
        if (!GardenApi.inGarden()) return
        if (!config.overlay) return
        inComposter = event.inventoryName == "Composter"
        inComposterUpgrades = event.inventoryName == "Composter Upgrades"
        if (!inComposter && !inComposterUpgrades) return

        inInventory = true
        update()
    }

    @HandleEvent
    fun onToolTip(event: ToolTipEvent) {
        if (!inComposterUpgrades) return
        update()
        for (upgrade in ComposterUpgrade.entries) {
            val name = event.itemStack.name
            if (name.contains(upgrade.displayName)) {
                maxLevel = ComposterUpgrade.regex.matchMatcher(name) {
                    group("level")?.romanToDecimalIfNecessary() ?: 0
                } == 25
                extraComposterUpgrade = upgrade
                update()
                return
            }
        }
        if (extraComposterUpgrade != null) {
            extraComposterUpgrade = null
            maxLevel = false
            update()
        }
    }

    private fun update() {
        val composterUpgrades = ComposterApi.composterUpgrades ?: return
        if (composterUpgrades.isEmpty()) {
            val list = Collections.singletonList(listOf("§cOpen Composter Upgrades!"))
            organicMatterDisplay = list
            fuelExtraDisplay = list
            return
        }
        if (organicMatterFactors.isEmpty()) {
            organicMatterDisplay = listOf(
                Collections.singletonList("§cSkyHanni composter error:"),
                Collections.singletonList("§cRepo data not loaded!"),
                Collections.singletonList("§7(organicMatterFactors is empty)"),
            )
            return
        }
        if (fuelFactors.isEmpty()) {
            organicMatterDisplay = listOf(
                Collections.singletonList("§cSkyHanni composter error:"),
                Collections.singletonList("§cRepo data not loaded!"),
                Collections.singletonList("§7(fuelFactors is empty)"),
            )
            return
        }
        if (currentOrganicMatterItem.let { it !in organicMatterFactors.keys && it != NONE }) {
            currentOrganicMatterItem = NONE
        }
        if (currentFuelItem.let { it !in fuelFactors.keys && it != NONE }) currentFuelItem = NONE

        if (inComposter) {
            organicMatterDisplay = drawOrganicMatterDisplay()
            fuelExtraDisplay = drawFuelExtraDisplay()
        } else if (inComposterUpgrades) {
            organicMatterDisplay = drawUpgradeStats()
            fuelExtraDisplay = emptyList()
        }
    }

    private fun drawUpgradeStats(): List<List<Any>> {
        val newList = mutableListOf<List<Any>>()

        var upgrade = extraComposterUpgrade
        if (upgrade == null) {
            newList.addAsSingletonList("§7Preview: Nothing")
        } else {
            val level = upgrade.getLevel(null)
            val nextLevel = if (maxLevel) "§6§lMAX" else "§c➜ §a" + (level + 1)
            val displayName = upgrade.displayName
            newList.addAsSingletonList("§7Preview §a$displayName§7: §a$level $nextLevel")
        }
        newList.addAsSingletonList("")
        if (maxLevel) {
            upgrade = null
        }

        addExtraData(newList)

        val maxOrganicMatter = ComposterApi.maxOrganicMatter(null)
        val maxOrganicMatterPreview = ComposterApi.maxOrganicMatter(upgrade)

        val matterPer = ComposterApi.organicMatterRequiredPer(null)
        val matterPerPreview = ComposterApi.organicMatterRequiredPer(upgrade)

        val matterMaxDuration = ComposterApi.timePerCompost(null) * floor(maxOrganicMatter / matterPer)
        val matterMaxDurationPreview =
            ComposterApi.timePerCompost(upgrade) * floor(maxOrganicMatterPreview / matterPerPreview)

        var format = formatTime(matterMaxDuration)
        var formatPreview =
            if (matterMaxDuration != matterMaxDurationPreview) " §c➜ §b" + formatTime(matterMaxDurationPreview) else ""

        newList.addAsSingletonList("§7Full §eOrganic Matter §7empty time: §b$format$formatPreview")

        val maxFuel = ComposterApi.maxFuel(null)
        val maxFuelPreview = ComposterApi.maxFuel(upgrade)

        val fuelRequiredPer = ComposterApi.fuelRequiredPer(null)
        val fuelRequiredPerPreview = ComposterApi.fuelRequiredPer(upgrade)

        val fuelMaxDuration = ComposterApi.timePerCompost(null) * floor(maxFuel / fuelRequiredPer)
        val fuelMaxDurationPreview =
            ComposterApi.timePerCompost(upgrade) * floor(maxFuelPreview / fuelRequiredPerPreview)

        format = formatTime(fuelMaxDuration)
        formatPreview =
            if (fuelMaxDuration != fuelMaxDurationPreview) " §c➜ §b" + formatTime(fuelMaxDurationPreview) else ""
        newList.addAsSingletonList("§7Full §2Fuel §7empty time: §b$format$formatPreview")

        return newList
    }

    private fun formatTime(duration: Duration) = duration.format(maxUnits = 2)

    private fun drawOrganicMatterDisplay(): MutableList<List<Any>> {
        val maxOrganicMatter = ComposterApi.maxOrganicMatter(if (maxLevel) null else extraComposterUpgrade)
        val currentOrganicMatter = ComposterApi.getOrganicMatter()
        val missingOrganicMatter = (maxOrganicMatter - currentOrganicMatter).toDouble()

        val newList = mutableListOf<List<Any>>()
        newList.addAsSingletonList("§7Items needed to fill §eOrganic Matter")
        val fillList = fillList(newList, organicMatterFactors, missingOrganicMatter, testOffset) {
            currentOrganicMatterItem = it
            update()
        }
        if (currentOrganicMatterItem == NONE) {
            currentOrganicMatterItem = fillList
            update()
        }
        return newList
    }

    private fun drawFuelExtraDisplay(): List<List<Any>> {
        val newList = mutableListOf<List<Any>>()

        addExtraData(newList)

        if (inComposter) {
            newList.addAsSingletonList("§7Items needed to fill §2Fuel")
            val maxFuel = ComposterApi.maxFuel(null)
            val currentFuel = ComposterApi.getFuel()
            val missingFuel = (maxFuel - currentFuel).toDouble()
            val fillList = fillList(newList, fuelFactors, missingFuel) {
                currentFuelItem = it
                update()
            }
            if (currentFuelItem == NONE) {
                currentFuelItem = fillList
                update()
            }
        }
        return newList
    }

    private fun addExtraData(newList: MutableList<List<Any>>) {
        val organicMatterItem = currentOrganicMatterItem ?: return
        val fuelItem = currentFuelItem ?: return
        if (organicMatterItem == NONE || fuelItem == NONE) return

        newList.addSelector<TimeType>(
            "§7Per ",
            getName = { type -> type.display },
            isCurrent = { it == currentTimeType },
            onChange = {
                currentTimeType = it
                update()
            },
        )

        val list = mutableListOf<Any>()
        list.add("§7Using: ")
        list.add(organicMatterItem.getItemStack())
        list.add("§7and ")
        list.add(fuelItem.getItemStack())
        newList.add(list)

        val timePerCompost = ComposterApi.timePerCompost(null)
        val upgrade = if (maxLevel) null else extraComposterUpgrade
        val timePerCompostPreview = ComposterApi.timePerCompost(upgrade)
        val format = timePerCompost.format()
        val formatPreview =
            if (timePerCompostPreview != timePerCompost) " §c➜ §b" + timePerCompostPreview.format() else ""
        newList.addAsSingletonList(" §7Time per Compost: §b$format$formatPreview")

        val timeText = currentTimeType.display.lowercase()
        val timeMultiplier = if (currentTimeType != TimeType.COMPOST) {
            (currentTimeType.multiplier * 1000.0 / (timePerCompost.inWholeMilliseconds))
        } else 1.0
        val timeMultiplierPreview = if (currentTimeType != TimeType.COMPOST) {
            (currentTimeType.multiplier * 1000.0 / (timePerCompostPreview.inWholeMilliseconds))
        } else 1.0

        val multiDropFactor = ComposterApi.multiDropChance(null) + 1
        val multiDropFactorPreview = ComposterApi.multiDropChance(upgrade) + 1
        val multiplier = multiDropFactor * timeMultiplier
        val multiplierPreview = multiDropFactorPreview * timeMultiplierPreview
        val compostPerTitlePreview =
            if (multiplier != multiplierPreview) " §c➜ §e" + multiplierPreview.roundTo(2) else ""
        val compostPerTitle =
            if (currentTimeType == TimeType.COMPOST) "Compost multiplier" else "Composts per $timeText"
        newList.addAsSingletonList(" §7$compostPerTitle: §e${multiplier.roundTo(2)}$compostPerTitlePreview")

        val organicMatterPrice = getPrice(organicMatterItem)
        val organicMatterFactor = organicMatterFactors[organicMatterItem] ?: 1.0

        val organicMatterRequired = ComposterApi.organicMatterRequiredPer(null)
        val organicMatterRequiredPreview = ComposterApi.organicMatterRequiredPer(upgrade)

        val organicMatterPricePer = organicMatterPrice * (organicMatterRequired / organicMatterFactor)
        val organicMatterPricePerPreview = organicMatterPrice * (organicMatterRequiredPreview / organicMatterFactor)

        val fuelPrice = getPrice(fuelItem)
        val fuelFactor = fuelFactors[fuelItem] ?: 1.0

        val fuelRequired = ComposterApi.fuelRequiredPer(null)
        val fuelRequiredPreview = ComposterApi.fuelRequiredPer(upgrade)

        val fuelPricePer = fuelPrice * (fuelRequired / fuelFactor)
        val fuelPricePerPreview = fuelPrice * (fuelRequiredPreview / fuelFactor)

        val totalCost = (fuelPricePer + organicMatterPricePer) * timeMultiplier
        val totalCostPreview = (fuelPricePerPreview + organicMatterPricePerPreview) * timeMultiplierPreview

        val materialCostFormatPreview =
            if (totalCost != totalCostPreview) " §c➜ §6" + totalCostPreview.shortFormat() else ""
        val materialCostFormat =
            " §7Material costs per $timeText: §6${totalCost.shortFormat()}$materialCostFormatPreview"
        newList.addAsSingletonList(materialCostFormat)

        val priceCompost = COMPOST.getPrice()
        val profit = ((priceCompost * multiDropFactor) - (fuelPricePer + organicMatterPricePer)) * timeMultiplier
        val profitPreview =
            ((priceCompost * multiDropFactorPreview) - (fuelPricePerPreview + organicMatterPricePerPreview)) * timeMultiplierPreview

        val profitFormatPreview = if (profit != profitPreview) " §c➜ §6" + profitPreview.shortFormat() else ""
        val profitFormat = " §7Profit per $timeText: §6${profit.shortFormat()}$profitFormatPreview"
        newList.addAsSingletonList(profitFormat)

        newList.addAsSingletonList("")
    }

    private fun fillList(
        bigList: MutableList<List<Any>>,
        factors: Map<NeuInternalName, Double>,
        missing: Double,
        testOffsetRec: Int = 0,
        onClick: (NeuInternalName) -> Unit,
    ): NeuInternalName {
        val map = mutableMapOf<NeuInternalName, Double>()
        for ((internalName, factor) in factors) {
            map[internalName] = factor / getPrice(internalName)
        }

        val testOffset = if (testOffsetRec > map.size) {
            ChatUtils.userError("Invalid Composter Overlay Offset! $testOffset cannot be greater than ${map.size}!")
            ComposterOverlay.testOffset = 0
            0
        } else testOffsetRec

        val first: NeuInternalName? = calculateFirst(map, testOffset, factors, missing, onClick, bigList)
        if (testOffset != 0) {
            bigList.addAsSingletonList(
                Renderable.link("testOffset = $testOffset") {
                    ComposterOverlay.testOffset = 0
                    update()
                },
            )
        }

        return first ?: error("First is empty!")
    }

    private fun calculateFirst(
        map: MutableMap<NeuInternalName, Double>,
        testOffset: Int,
        factors: Map<NeuInternalName, Double>,
        missing: Double,
        onClick: (NeuInternalName) -> Unit,
        bigList: MutableList<List<Any>>,
    ): NeuInternalName? {
        var i = 0
        var first: NeuInternalName? = null
        for (internalName in map.sortedDesc().keys) {
            i++
            if (i < testOffset) continue
            if (first == null) first = internalName
            val factor = factors[internalName] ?: 1.0

            val item = internalName.getItemStack()
            val price = getPrice(internalName)
            val itemsNeeded = if (config.roundDown) {
                val amount = missing / factor
                if (amount > .75 && amount < 1.0) {
                    1.0
                } else {
                    floor(amount)
                }
            } else {
                ceil(missing / factor)
            }
            val totalPrice = itemsNeeded * price

            val list = mutableListOf<Any>()
            if (testOffset != 0) {
                list.add("#$i ")
            }
            list.add(item)
            formatPrice(totalPrice, internalName, item.name, list, itemsNeeded, onClick)
            bigList.add(list)
            if (i == 10 + testOffset) break
        }
        return first
    }

    private fun formatPrice(
        totalPrice: Double,
        internalName: NeuInternalName,
        itemName: String,
        list: MutableList<Any>,
        itemsNeeded: Double,
        onClick: (NeuInternalName) -> Unit,
    ) {
        val format = totalPrice.shortFormat()
        val selected = if (internalName == currentOrganicMatterItem || internalName == currentFuelItem) "§n" else ""
        val rawItemName = itemName.removeColor()
        val name = itemName.substring(0, 2) + selected + rawItemName
        list.add(
            Renderable.link("$name §8x${itemsNeeded.addSeparators()} §7(§6$format§7)") {
                onClick(internalName)
                if (KeyboardManager.isModifierKeyDown() && lastAttemptTime.passedSince() > 500.milliseconds) {
                    lastAttemptTime = SimpleTimeMark.now()
                    retrieveMaterials(internalName, itemName, itemsNeeded.toInt())
                }
            },
        )
    }

    private fun retrieveMaterials(internalName: NeuInternalName, itemName: String, itemsNeeded: Int) {
        if (itemsNeeded == 0) return
        if (config.retrieveFrom == ComposterConfig.RetrieveFromEntry.BAZAAR &&
            !LorenzUtils.noTradeMode && internalName != BIOFUEL
        ) {
            BazaarApi.searchForBazaarItem(itemName, itemsNeeded)
            return
        }
        val havingInInventory = internalName.getAmountInInventory()
        if (havingInInventory >= itemsNeeded) {
            ChatUtils.chat("$itemName §8x$itemsNeeded §ealready found in inventory!")
            return
        }

        val havingInSacks = internalName.getAmountInSacksOrNull() ?: run {
            HypixelCommands.getFromSacks(internalName.asString(), itemsNeeded - havingInInventory)
            // TODO Add sack type repo data

            val isDwarvenMineable = internalName.let { it == VOLTA || it == OIL_BARREL || it == BIOFUEL }
            val sackType = if (isDwarvenMineable) "Mining §eor §9Dwarven" else "Enchanted Agronomy"
            ChatUtils.clickableChat(
                "Sacks could not be loaded. Click here and open your §9$sackType Sack §eto update the data!",
                onClick = { HypixelCommands.sacks() },
                "§eClick to run /sax!",
                replaceSameMessage = true,
            )
            return
        }
        if (havingInSacks == 0) {
            SoundUtils.playErrorSound()
            if (LorenzUtils.noTradeMode) {
                ChatUtils.chat("No $itemName §efound in sacks.")
            } else {
                ChatUtils.chat("No $itemName §efound in sacks. Opening Bazaar.")
                BazaarApi.searchForBazaarItem(itemName, itemsNeeded)
            }
            return
        }

        HypixelCommands.getFromSacks(internalName.asString(), itemsNeeded - havingInInventory)
        val havingInTotal = havingInInventory + havingInSacks
        if (itemsNeeded >= havingInTotal) {
            if (LorenzUtils.noTradeMode) {
                ChatUtils.chat("You're out of $itemName §ein your sacks!")
            } else {
                ChatUtils.clickableChat(
                    // TODO Add this as a separate feature, and then don't send any msg if the feature is disabled
                    "You're out of $itemName §ein your sacks! Click here to buy more on the Bazaar!",
                    onClick = { HypixelCommands.bazaar(itemName.removeColor()) },
                    "§eClick find on the bazaar!",
                )
            }
        }
    }

    private fun getPrice(internalName: NeuInternalName): Double {
        val price = internalName.getPrice(config.priceSource)
        if (internalName == BIOFUEL && price > 20_000) return 20_000.0

        return price
    }

    @HandleEvent
    fun onNeuRepoReload(event: NeuRepositoryReloadEvent) {
        updateOrganicMatterFactors()
    }

    @HandleEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<GardenJson>("Garden")
        organicMatter = data.organicMatter
        fuelFactors = data.fuel
        updateOrganicMatterFactors()
    }

    private fun updateOrganicMatterFactors() {
        try {
            organicMatterFactors = updateOrganicMatterFactors(organicMatter)
        } catch (e: Exception) {
            ErrorManager.logErrorWithData(
                e, "Failed to calculate composter overlay data",
                "organicMatter" to organicMatter,
            )
        }
    }

    private val blockedItems = listOf(
        "POTION_AFFINITY_TALISMAN",
        "CROPIE_TALISMAN",
        "SPEED_TALISMAN",
        "SIMPLE_CARROT_CANDY",
    )

    private fun isBlockedArmor(internalName: String): Boolean {
        return internalName.endsWith("_BOOTS") ||
            internalName.endsWith("_HELMET") ||
            internalName.endsWith("_CHESTPLATE") ||
            internalName.endsWith("_LEGGINGS")
    }

    private fun updateOrganicMatterFactors(baseValues: Map<NeuInternalName, Double>): Map<NeuInternalName, Double> {
        val map = mutableMapOf<NeuInternalName, Double>()
        for ((internalName, _) in NeuItems.allNeuRepoItems()) {
            if (blockedItems.contains(internalName) || isBlockedArmor(internalName)) continue

            var (newId, amount) = NeuItems.getPrimitiveMultiplier(internalName.toInternalName())
            if (amount <= 9) continue
            if (internalName == "ENCHANTED_HUGE_MUSHROOM_1" || internalName == "ENCHANTED_HUGE_MUSHROOM_2") {
                //  160 * 8 * 4 is 5120 and not 5184, but hypixel made an error, so we have to copy the error
                amount = 5184
            }
            baseValues[newId]?.let {
                map[internalName.toInternalName()] = it * amount
            }
        }
        return map
    }

    @HandleEvent
    fun onBackgroundDraw(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        if (EstimatedItemValue.isCurrentlyShowing()) return

        if (inInventory) {
            config.overlayOrganicMatterPos.renderStringsAndItems(
                organicMatterDisplay,
                posLabel = "Composter Overlay Organic Matter",
            )
            config.overlayFuelExtrasPos.renderStringsAndItems(
                fuelExtraDisplay,
                posLabel = "Composter Overlay Fuel Extras",
            )
        }
    }

    enum class TimeType(val display: String, val multiplier: Int) {
        COMPOST("Compost", 1),
        HOUR("Hour", 60 * 60),
        DAY("Day", 60 * 60 * 24),
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "garden.composterOverlay", "garden.composters.overlay")
        event.move(3, "garden.composterOverlayPriceType", "garden.composters.overlayPriceType")
        event.move(3, "garden.composterOverlayRetrieveFrom", "garden.composters.retrieveFrom")
        event.move(3, "garden.composterOverlayOrganicMatterPos", "garden.composters.overlayOrganicMatterPos")
        event.move(3, "garden.composterOverlayFuelExtrasPos", "garden.composters.overlayFuelExtrasPos")
        event.move(3, "garden.composterRoundDown", "garden.composters.roundDown")
        event.transform(15, "garden.composters.retrieveFrom") { element ->
            ConfigUtils.migrateIntToEnum(element, RetrieveFromEntry::class.java)
        }
    }

    @HandleEvent
    fun onDebug(event: DebugDataCollectEvent) {
        event.title("Garden Composter")

        event.addIrrelevant {
            add("currentOrganicMatterItem: $currentOrganicMatterItem")
            add("currentFuelItem: $currentFuelItem")

            add(" ")
            val composterUpgrades = ComposterApi.composterUpgrades
            if (composterUpgrades == null) {
                add("composterUpgrades is null")
            } else {
                for ((a, b) in composterUpgrades) {
                    add("upgrade $a: $b")
                }
            }

            add(" ")
            val tabListData = ComposterApi.tabListData
            for ((a, b) in tabListData) {
                add("tabListData $a: $b")
            }
        }
    }
}
