package at.hannibal2.skyhanni.features.inventory.chocolatefactory

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryUpdatedEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.NumberUtil.formatDouble
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.NumberUtil.formatLong
import at.hannibal2.skyhanni.utils.NumberUtil.romanToDecimal
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.TimeUtils
import net.minecraft.item.ItemStack

@SkyHanniModule
object ChocolateFactoryDataLoader {

    private val config get() = ChocolateFactoryApi.config
    private val profileStorage get() = ChocolateFactoryApi.profileStorage

    // <editor-fold desc="Patterns">
    /**
     * REGEX-TEST: §674.15 §8per second
     */
    private val chocolatePerSecondPattern by ChocolateFactoryApi.patternGroup.pattern(
        "chocolate.persecond",
        "§6(?<amount>[\\d.,]+) §8per second",
    )

    /**
     * REGEX-TEST: §7All-time Chocolate: §67,645,879,859
     */
    private val chocolateAllTimePattern by ChocolateFactoryApi.patternGroup.pattern(
        "chocolate.alltime",
        "§7All-time Chocolate: §6(?<amount>[\\d,]+)",
    )

    /**
     * REGEX-TEST: §6Chocolate Factory III
     */
    private val prestigeLevelPattern by ChocolateFactoryApi.patternGroup.pattern(
        "prestige.level",
        "§6Chocolate Factory (?<prestige>[IVX]+)",
    )

    /**
     * REGEX-TEST: §7Chocolate this Prestige: §6330,382,389
     */
    private val chocolateThisPrestigePattern by ChocolateFactoryApi.patternGroup.pattern(
        "chocolate.thisprestige",
        "§7Chocolate this Prestige: §6(?<amount>[\\d,]+)",
    )

    /**
     * REGEX-TEST: §7Max Chocolate: §660B
     */
    private val maxChocolatePattern by ChocolateFactoryApi.patternGroup.pattern(
        "chocolate.max",
        "§7Max Chocolate: §6(?<max>.*)",
    )

    /**
     * REGEX-TEST: §7§cRequires 4B Chocolate this Prestige!
     */
    private val chocolateForPrestigePattern by ChocolateFactoryApi.patternGroup.pattern(
        "chocolate.forprestige",
        "§7§cRequires (?<amount>\\w+) Chocolate this.*",
    )

    /**
     * REGEX-TEST: §7Total Multiplier: §61.399x
     */
    private val chocolateMultiplierPattern by ChocolateFactoryApi.patternGroup.pattern(
        "chocolate.multiplier",
        "§7Total Multiplier: §6(?<amount>[\\d.]+)x",
    )

    /**
     * REGEX-TEST: §7You are §8#§b114
     * REGEX-TEST: §7§7You are §8#§b5,139 §7in all-time Chocolate.
     * REGEX-TEST: §7§7You are §8#§b5,139 §7in all-time
     */
    private val leaderboardPlacePattern by ChocolateFactoryApi.patternGroup.pattern(
        "leaderboard.place",
        "(?:§.)+You are §8#§b(?<position>[\\d,]+)(?: §7in all-time)?(?: Chocolate\\.)?",
    )

    /**
     * REGEX-TEST: §7§8You are in the top §65.06%§8 of players!
     */
    private val leaderboardPercentilePattern by ChocolateFactoryApi.patternGroup.pattern(
        "leaderboard.percentile",
        "§7§8You are in the top §.(?<percent>[\\d.]+)%§8 of players!",
    )

    /**
     * REGEX-TEST: §7Your Barn: §a16§7/§a450 Rabbits
     */
    private val barnAmountPattern by ChocolateFactoryApi.patternGroup.pattern(
        "barn.amount",
        "§7Your Barn: §.(?<rabbits>\\d+)§7/§.(?<max>\\d+) Rabbits",
    )

    /**
     * REGEX-TEST: §7Charges: §e2§7/§a3
     */
    private val timeTowerAmountPattern by ChocolateFactoryApi.patternGroup.pattern(
        "timetower.amount",
        "§7Charges: §.(?<uses>\\d+)§7/§a(?<max>\\d+)",
    )

    /**
     * REGEX-TEST: §7What does it do? Nobody knows...
     */
    private val timeTowerAmountEmptyPattern by ChocolateFactoryApi.patternGroup.pattern(
        "timetower.amount.empty",
        "§7What does it do\\? Nobody knows\\.\\.\\.",
    )

    /**
     * REGEX-TEST: §7Status: §a§lACTIVE §f59m58s
     * REGEX-TEST: §7Status: §c§lINACTIVE
     */
    private val timeTowerStatusPattern by ChocolateFactoryApi.patternGroup.pattern(
        "timetower.status",
        "§7Status: §.§l(?<status>INACTIVE|ACTIVE)(?: §f)?(?<acitveTime>\\w*)",
    )

    /**
     * REGEX-TEST: §7Next Charge: §a7h59m58s
     */
    private val timeTowerRechargePattern by ChocolateFactoryApi.patternGroup.pattern(
        "timetower.recharge",
        "§7Next Charge: §a(?<duration>\\w+)",
    )
    val clickMeRabbitPattern by ChocolateFactoryApi.patternGroup.pattern(
        "rabbit.clickme",
        "§e§lCLICK ME!",
    )

    /**
     * REGEX-TEST: §6§lGolden Rabbit §8- §aStampede
     */
    val clickMeGoldenRabbitPattern by ChocolateFactoryApi.patternGroup.pattern(
        "rabbit.clickme.golden",
        "§6§lGolden Rabbit §8- §a(?<name>.*)",
    )

    /**
     * REGEX-TEST: Rabbit Bro - [14] Employee
     */
    private val rabbitAmountPattern by ChocolateFactoryApi.patternGroup.pattern(
        "rabbit.amount",
        "Rabbit \\S+ - \\[(?<amount>\\d+)].*",
    )

    /**
     * REGEX-TEST: Time Tower I
     */
    private val upgradeTierPattern by ChocolateFactoryApi.patternGroup.pattern(
        "upgradetier",
        ".*\\s(?<tier>[IVXLC]+)",
    )

    /**
     * REGEX-TEST: Rabbit Bro - Unemployed
     */
    private val unemployedRabbitPattern by ChocolateFactoryApi.patternGroup.pattern(
        "rabbit.unemployed",
        "Rabbit \\w+ - Unemployed",
    )

    /**
     * REGEX-TEST: Rabbit Shrine
     * REGEX-TEST: Coach Jackrabbit
     */
    private val otherUpgradePattern by ChocolateFactoryApi.patternGroup.pattern(
        "other.upgrade",
        "Rabbit Shrine|Coach Jackrabbit",
    )

    /**
     * REGEX-TEST: §7Available eggs: §a0
     */
    private val hitmanAvailableEggsPattern by ChocolateFactoryApi.patternGroup.pattern(
        "hitman.availableeggs",
        "§7Available eggs: §a(?<amount>\\d+)",
    )

    /**
     * REGEX-TEST: §7Purchased slots: §a28§7/§a28
     * REGEX-TEST: §7Purchased slots: §e0§7/§a22
     */
    private val hitmanPurchasedSlotsPattern by ChocolateFactoryApi.patternGroup.pattern(
        "hitman.purchasedslots",
        "§7Purchased slots: §.(?<amount>\\d+)§7\\/§a\\d+",
    )

    /**
     * REGEX-TEST: §7Slot cooldown: §a8m 6s
     */
    private val hitmanSingleSlotCooldownPattern by ChocolateFactoryApi.patternGroup.pattern(
        "hitman.slotcooldown",
        "§7Slot cooldown: §a(?<duration>[\\w ]+)",
    )

    /**
     * REGEX-TEST: §7All slots in: §a8h 8m 6s
     */
    private val hitmanAllSlotsCooldownPattern by ChocolateFactoryApi.patternGroup.pattern(
        "hitman.allslotscooldown",
        "§7All slots in: §a(?<duration>[\\w ]+)",
    )

    // </editor-fold>

    @HandleEvent
    fun onInventoryUpdated(event: InventoryUpdatedEvent) {
        if (!ChocolateFactoryApi.inChocolateFactory) return

        updateInventoryItems(event.inventoryItems)
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        clearData()
    }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        clearData()
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(
            47,
            "inventory.chocolateFactory.rabbitWarning",
            "inventory.chocolateFactory.rabbitWarning.rabbitWarning",
        )
    }

    @HandleEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        val soundProperty = config.rabbitWarning.specialRabbitSound
        ConditionalUtils.onToggle(soundProperty) {
            ChocolateFactoryApi.warningSound = SoundUtils.createSound(soundProperty.get(), 1f)
        }

        config.chocolateUpgradeWarnings.upgradeWarningTimeTower.whenChanged { _, _ ->
            ChocolateFactoryApi.factoryUpgrades.takeIf { it.isNotEmpty() }?.let {
                findBestUpgrades(it)
            } ?: run {
                ChatUtils.clickableChat(
                    "Could not determine your current statistics to get next upgrade. Open CF to fix this!",
                    onClick = { HypixelCommands.chocolateFactory() },
                    "§eClick to run /cf!",
                )
            }
        }
    }

    private fun clearData() {
        ChocolateFactoryApi.inChocolateFactory = false
        ChocolateFactoryApi.chocolateFactoryPaused = false
        ChocolateFactoryApi.factoryUpgrades = emptyList()
        ChocolateFactoryApi.bestAffordableSlot = -1
        ChocolateFactoryApi.bestPossibleSlot = -1
    }

    fun updateInventoryItems(inventory: Map<Int, ItemStack>) {
        val profileStorage = profileStorage ?: return

        val chocolateItem = InventoryUtils.getItemAtSlotIndex(ChocolateFactoryApi.infoIndex) ?: return
        val prestigeItem = InventoryUtils.getItemAtSlotIndex(ChocolateFactoryApi.prestigeIndex) ?: return
        val timeTowerItem = InventoryUtils.getItemAtSlotIndex(ChocolateFactoryApi.timeTowerIndex) ?: return
        val productionInfoItem = InventoryUtils.getItemAtSlotIndex(ChocolateFactoryApi.productionInfoIndex) ?: return
        val leaderboardItem = InventoryUtils.getItemAtSlotIndex(ChocolateFactoryApi.leaderboardIndex) ?: return
        val barnItem = InventoryUtils.getItemAtSlotIndex(ChocolateFactoryApi.barnIndex) ?: return
        val hitmanItem = InventoryUtils.getItemAtSlotIndex(ChocolateFactoryApi.rabbitHitmanIndex) ?: return

        ChocolateFactoryApi.factoryUpgrades = emptyList()

        processChocolateItem(chocolateItem)
        val list = mutableListOf<ChocolateFactoryUpgrade>()
        processPrestigeItem(list, prestigeItem)
        processTimeTowerItem(timeTowerItem)
        processProductionItem(productionInfoItem)
        processLeaderboardItem(leaderboardItem)
        processBarnItem(barnItem)
        processHitmanItem(hitmanItem)

        profileStorage.rawChocPerSecond = (ChocolateFactoryApi.chocolatePerSecond / profileStorage.chocolateMultiplier + .01).toInt()
        profileStorage.lastDataSave = SimpleTimeMark.now()

        ChocolateFactoryStats.updateDisplay()

        processInventory(list, inventory)

        findBestUpgrades(list)
        ChocolateFactoryApi.factoryUpgrades = list
    }

    private fun processChocolateItem(item: ItemStack) {
        val profileStorage = profileStorage ?: return

        ChocolateFactoryApi.chocolateAmountPattern.matchMatcher(item.name.removeColor()) {
            profileStorage.currentChocolate = group("amount").formatLong()
        }
        for (line in item.getLore()) {
            chocolatePerSecondPattern.matchMatcher(line) {
                ChocolateFactoryApi.chocolatePerSecond = group("amount").formatDouble()
            }
            chocolateAllTimePattern.matchMatcher(line) {
                profileStorage.chocolateAllTime = group("amount").formatLong()
            }
        }
    }

    private fun processPrestigeItem(list: MutableList<ChocolateFactoryUpgrade>, item: ItemStack) {
        val profileStorage = profileStorage ?: return

        prestigeLevelPattern.matchMatcher(item.name) {
            ChocolateFactoryApi.currentPrestige = group("prestige").romanToDecimal()
        }
        var prestigeCost: Long? = null
        for (line in item.getLore()) {
            chocolateThisPrestigePattern.matchMatcher(line) {
                profileStorage.chocolateThisPrestige = group("amount").formatLong()
            }
            maxChocolatePattern.matchMatcher(line) {
                profileStorage.maxChocolate = group("max").formatLong()
            }
            chocolateForPrestigePattern.matchMatcher(line) {
                ChocolateFactoryApi.chocolateForPrestige = group("amount").formatLong()
                prestigeCost = ChocolateFactoryApi.chocolateForPrestige
            }
        }
        val prestigeUpgrade = ChocolateFactoryUpgrade(
            ChocolateFactoryApi.prestigeIndex,
            ChocolateFactoryApi.currentPrestige,
            prestigeCost,
            isPrestige = true,
        )
        list.add(prestigeUpgrade)
    }

    private fun processProductionItem(item: ItemStack) {
        val profileStorage = profileStorage ?: return

        chocolateMultiplierPattern.firstMatcher(item.getLore()) {
            val currentMultiplier = group("amount").formatDouble()
            profileStorage.chocolateMultiplier = currentMultiplier

            if (ChocolateFactoryTimeTowerManager.timeTowerActive()) {
                profileStorage.rawChocolateMultiplier = currentMultiplier - profileStorage.timeTowerLevel * 0.1
            } else {
                profileStorage.rawChocolateMultiplier = currentMultiplier
            }
        }
    }

    private fun processLeaderboardItem(item: ItemStack) {
        ChocolateFactoryApi.leaderboardPosition = null
        ChocolateFactoryApi.leaderboardPercentile = null

        for (line in item.getLore()) {
            leaderboardPlacePattern.matchMatcher(line) {
                ChocolateFactoryApi.leaderboardPosition = group("position").formatInt()
            }
            leaderboardPercentilePattern.matchMatcher(line) {
                ChocolateFactoryApi.leaderboardPercentile = group("percent").formatDouble()
            }
        }
    }

    private fun processBarnItem(item: ItemStack) {
        val profileStorage = profileStorage ?: return

        barnAmountPattern.firstMatcher(item.getLore()) {
            profileStorage.currentRabbits = group("rabbits").formatInt()
            profileStorage.maxRabbits = group("max").formatInt()
            ChocolateFactoryBarnManager.trySendBarnFullMessage(inventory = true)
        }
    }

    private fun processTimeTowerItem(item: ItemStack) {
        val profileStorage = profileStorage ?: return

        for (line in item.getLore()) {
            timeTowerAmountPattern.matchMatcher(line) {
                profileStorage.currentTimeTowerUses = group("uses").formatInt()
                profileStorage.maxTimeTowerUses = group("max").formatInt()
                ChocolateFactoryTimeTowerManager.checkTimeTowerWarning(true)
            }
            if (timeTowerAmountEmptyPattern.matches(line)) {
                profileStorage.currentTimeTowerUses = 0
                profileStorage.maxTimeTowerUses = 0
                profileStorage.currentTimeTowerUses = 0
            }
            timeTowerStatusPattern.matchMatcher(line) {
                val activeTime = group("acitveTime")
                if (activeTime.isNotEmpty()) {
                    val activeUntil = SimpleTimeMark.now() + TimeUtils.getDuration(activeTime)
                    profileStorage.currentTimeTowerEnds = activeUntil
                } else {
                    profileStorage.currentTimeTowerEnds = SimpleTimeMark.farPast()
                }
            }
            timeTowerRechargePattern.matchMatcher(line) {
                val timeUntilTower = TimeUtils.getDuration(group("duration"))
                val nextTimeTower = SimpleTimeMark.now() + timeUntilTower
                profileStorage.nextTimeTower = nextTimeTower
            }
        }
    }

    private fun processHitmanItem(item: ItemStack) {
        val profileStorage = profileStorage ?: return

        for (line in item.getLore()) {
            hitmanAvailableEggsPattern.matchMatcher(line) {
                profileStorage.hitmanStats.availableHitmanEggs = group("amount").formatInt()
            }
            hitmanSingleSlotCooldownPattern.matchMatcher(line) {
                val timeUntilSlot = TimeUtils.getDuration(group("duration"))
                val nextSlot = (SimpleTimeMark.now() + timeUntilSlot)
                profileStorage.hitmanStats.singleSlotCooldownMark = nextSlot
            }
            hitmanAllSlotsCooldownPattern.matchMatcher(line) {
                val timeUntilAllSlots = TimeUtils.getDuration(group("duration"))
                val nextAllSlots = (SimpleTimeMark.now() + timeUntilAllSlots)
                profileStorage.hitmanStats.allSlotsCooldownMark = nextAllSlots
            }
            hitmanPurchasedSlotsPattern.matchMatcher(line) {
                profileStorage.hitmanStats.purchasedHitmanSlots = group("amount").formatInt()
            }
        }
    }

    private fun processInventory(list: MutableList<ChocolateFactoryUpgrade>, inventory: Map<Int, ItemStack>) {
        for ((slotIndex, item) in inventory) {
            processItem(list, item, slotIndex)
        }
    }

    private fun processItem(list: MutableList<ChocolateFactoryUpgrade>, item: ItemStack, slotIndex: Int) {
        if (slotIndex == ChocolateFactoryApi.prestigeIndex) return

        if (slotIndex !in ChocolateFactoryApi.otherUpgradeSlots && slotIndex !in ChocolateFactoryApi.rabbitSlots) return

        val itemName = item.name.removeColor()
        val lore = item.getLore()
        val upgradeCost = ChocolateFactoryApi.getChocolateBuyCost(lore)
        val averageChocolate = ChocolateAmount.averageChocPerSecond().roundTo(2)
        val isMaxed = upgradeCost == null

        if (slotIndex in ChocolateFactoryApi.rabbitSlots) {
            handleRabbitSlot(list, itemName, slotIndex, isMaxed, upgradeCost, averageChocolate)
        } else if (slotIndex in ChocolateFactoryApi.otherUpgradeSlots) {
            handleOtherUpgradeSlot(list, itemName, slotIndex, isMaxed, upgradeCost, averageChocolate)
        }
    }

    private fun handleRabbitSlot(
        list: MutableList<ChocolateFactoryUpgrade>,
        itemName: String,
        slotIndex: Int,
        isMaxed: Boolean,
        upgradeCost: Long?,
        averageChocolate: Double,
    ) {
        val level = rabbitAmountPattern.matchMatcher(itemName) {
            group("amount").formatInt()
        } ?: run {
            if (unemployedRabbitPattern.matches(itemName)) 0 else null
        } ?: return

        if (isMaxed) {
            val rabbitUpgradeItem = ChocolateFactoryUpgrade(slotIndex, level, null, isRabbit = true)
            list.add(rabbitUpgradeItem)
            return
        }

        val chocolateIncrease = ChocolateFactoryApi.rabbitSlots[slotIndex] ?: 0
        val newAverageChocolate = ChocolateAmount.averageChocPerSecond(rawPerSecondIncrease = chocolateIncrease)
        addUpgradeToList(list, slotIndex, level, upgradeCost, averageChocolate, newAverageChocolate, isRabbit = true)
    }

    private fun handleOtherUpgradeSlot(
        list: MutableList<ChocolateFactoryUpgrade>,
        itemName: String,
        slotIndex: Int,
        isMaxed: Boolean,
        upgradeCost: Long?,
        averageChocolate: Double,
    ) {
        val level = upgradeTierPattern.matchMatcher(itemName) {
            group("tier").romanToDecimal()
        } ?: run {
            if (otherUpgradePattern.matches(itemName)) 0 else null
        } ?: return

        if (slotIndex == ChocolateFactoryApi.timeTowerIndex) {
            this.profileStorage?.timeTowerLevel = level
        }

        if (isMaxed) {
            val otherUpgrade = ChocolateFactoryUpgrade(slotIndex, level, null)
            list.add(otherUpgrade)
            return
        }

        val newAverageChocolate = when (slotIndex) {
            ChocolateFactoryApi.timeTowerIndex -> ChocolateAmount.averageChocPerSecond(includeTower = true)
            ChocolateFactoryApi.coachRabbitIndex -> ChocolateAmount.averageChocPerSecond(baseMultiplierIncrease = 0.01)
            else -> {
                val otherUpgrade = ChocolateFactoryUpgrade(slotIndex, level, upgradeCost)
                list.add(otherUpgrade)
                return
            }
        }

        addUpgradeToList(list, slotIndex, level, upgradeCost, averageChocolate, newAverageChocolate, isRabbit = false)
    }

    private fun addUpgradeToList(
        list: MutableList<ChocolateFactoryUpgrade>,
        slotIndex: Int,
        level: Int,
        upgradeCost: Long?,
        averageChocolate: Double,
        newAverageChocolate: Double,
        isRabbit: Boolean,
    ) {
        val extra = (newAverageChocolate - averageChocolate).roundTo(2)
        val effectiveCost = (upgradeCost!! / extra).roundTo(2)
        val upgrade = ChocolateFactoryUpgrade(slotIndex, level, upgradeCost, extra, effectiveCost, isRabbit = isRabbit)
        list.add(upgrade)
    }

    private fun findBestUpgrades(list: List<ChocolateFactoryUpgrade>) {
        val profileStorage = profileStorage ?: return

        val ttFiltered = list.filter {
            config.chocolateUpgradeWarnings.upgradeWarningTimeTower.get() || it.slotIndex != ChocolateFactoryApi.timeTowerIndex
        }

        val notMaxed = ttFiltered.filter {
            !it.isMaxed && it.effectiveCost != null
        }

        val bestUpgrade = notMaxed.minByOrNull { it.effectiveCost ?: Double.MAX_VALUE }
        profileStorage.bestUpgradeAvailableAt = bestUpgrade?.canAffordAt ?: SimpleTimeMark.farPast()
        profileStorage.bestUpgradeCost = bestUpgrade?.price ?: 0
        ChocolateFactoryApi.bestPossibleSlot = bestUpgrade?.getValidUpgradeIndex() ?: -1

        val bestUpgradeLevel = bestUpgrade?.level ?: 0
        ChocolateFactoryUpgradeWarning.checkUpgradeChange(ChocolateFactoryApi.bestPossibleSlot, bestUpgradeLevel)

        val affordAbleUpgrade = notMaxed.filter { it.canAfford() }.minByOrNull { it.effectiveCost ?: Double.MAX_VALUE }
        ChocolateFactoryApi.bestAffordableSlot = affordAbleUpgrade?.getValidUpgradeIndex() ?: -1
    }
}
