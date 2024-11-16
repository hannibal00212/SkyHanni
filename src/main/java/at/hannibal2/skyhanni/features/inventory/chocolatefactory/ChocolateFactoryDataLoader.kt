package at.hannibal2.skyhanni.features.inventory.chocolatefactory

import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryUpdatedEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryAPI.specialRabbitTextures
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryUtils.getNextUnlockedRabbitWorker
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryUtils.getNextUpgrade
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.getSkullTexture
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
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.Deque
import java.util.LinkedList

@SkyHanniModule
object ChocolateFactoryDataLoader {

    private val config get() = ChocolateFactoryAPI.config
    private val profileStorage get() = ChocolateFactoryAPI.profileStorage

    private val chocolatePerSecondPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "chocolate.persecond",
        "§6(?<amount>[\\d.,]+) §8per second",
    )
    private val chocolateAllTimePattern by ChocolateFactoryAPI.patternGroup.pattern(
        "chocolate.alltime",
        "§7All-time Chocolate: §6(?<amount>[\\d,]+)",
    )
    private val prestigeLevelPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "prestige.level",
        "§6Chocolate Factory (?<prestige>[IVX]+)",
    )
    private val chocolateThisPrestigePattern by ChocolateFactoryAPI.patternGroup.pattern(
        "chocolate.thisprestige",
        "§7Chocolate this Prestige: §6(?<amount>[\\d,]+)",
    )

    /**
     * REGEX-TEST: §7Max Chocolate: §660B
     */
    private val maxChocolatePattern by ChocolateFactoryAPI.patternGroup.pattern(
        "chocolate.max",
        "§7Max Chocolate: §6(?<max>.*)",
    )
    private val chocolateForPrestigePattern by ChocolateFactoryAPI.patternGroup.pattern(
        "chocolate.forprestige",
        "§7§cRequires (?<amount>\\w+) Chocolate this.*",
    )
    private val chocolateMultiplierPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "chocolate.multiplier",
        "§7Total Multiplier: §6(?<amount>[\\d.]+)x",
    )

    /**
     * REGEX-TEST: §7You are §8#§b114
     * REGEX-TEST: §7§7You are §8#§b5,139 §7in all-time Chocolate.
     * REGEX-TEST: §7§7You are §8#§b5,139 §7in all-time
     */
    private val leaderboardPlacePattern by ChocolateFactoryAPI.patternGroup.pattern(
        "leaderboard.place",
        "(?:§.)+You are §8#§b(?<position>[\\d,]+)(?: §7in all-time)?(?: Chocolate\\.)?",
    )
    private val leaderboardPercentilePattern by ChocolateFactoryAPI.patternGroup.pattern(
        "leaderboard.percentile",
        "§7§8You are in the top §.(?<percent>[\\d.]+)%§8 of players!",
    )
    private val barnAmountPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "barn.amount",
        "§7Your Barn: §.(?<rabbits>\\d+)§7/§.(?<max>\\d+) Rabbits",
    )
    private val timeTowerAmountPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "timetower.amount",
        "§7Charges: §.(?<uses>\\d+)§7/§a(?<max>\\d+)",
    )
    private val timeTowerStatusPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "timetower.status",
        "§7Status: §.§l(?<status>INACTIVE|ACTIVE)(?: §f)?(?<acitveTime>\\w*)",
    )
    private val timeTowerRechargePattern by ChocolateFactoryAPI.patternGroup.pattern(
        "timetower.recharge",
        "§7Next Charge: §a(?<duration>\\w+)",
    )
    val clickMeRabbitPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "rabbit.clickme",
        "§e§lCLICK ME!",
    )

    /**
     * REGEX-TEST: §6§lGolden Rabbit §8- §aStampede
     */
    val clickMeGoldenRabbitPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "rabbit.clickme.golden",
        "§6§lGolden Rabbit §8- §a(?<name>.*)",
    )
    private val rabbitAmountPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "rabbit.amount",
        "Rabbit \\S+ - \\[(?<amount>\\d+)].*",
    )
    private val upgradeTierPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "upgradetier",
        ".*\\s(?<tier>[IVXLC]+)",
    )
    private val unemployedRabbitPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "rabbit.unemployed",
        "Rabbit \\w+ - Unemployed",
    )
    private val otherUpgradePattern by ChocolateFactoryAPI.patternGroup.pattern(
        "other.upgrade",
        "Rabbit Shrine|Coach Jackrabbit",
    )

    @SubscribeEvent
    fun onInventoryUpdated(event: InventoryUpdatedEvent) {
        if (!ChocolateFactoryAPI.inChocolateFactory) return

        updateInventoryItems(event.inventoryItems)
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        clearData()
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        clearData()
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(
            47,
            "inventory.chocolateFactory.rabbitWarning",
            "inventory.chocolateFactory.rabbitWarning.rabbitWarning",
        )
    }

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        val soundProperty = config.rabbitWarning.specialRabbitSound
        ConditionalUtils.onToggle(soundProperty) {
            ChocolateFactoryAPI.warningSound = SoundUtils.createSound(soundProperty.get(), 1f)
        }

        config.chocolateUpgradeWarnings.upgradeWarningTimeTower.whenChanged { _, _ ->
            ChocolateFactoryAPI.factoryUpgrades.takeIf { it.isNotEmpty() }?.let {
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
        ChocolateFactoryAPI.inChocolateFactory = false
        ChocolateFactoryAPI.chocolateFactoryPaused = false
        ChocolateFactoryAPI.factoryUpgrades = emptyList()
        ChocolateFactoryAPI.bestAffordableSlot = -1
        ChocolateFactoryAPI.bestPossibleSlot = -1

        ChocolateFactoryAPI.allBestPossibleUpgrades = emptyMap()
        ChocolateFactoryAPI.lastUpgradesWhenChecking = emptyMap()
        ChocolateFactoryAPI.lastBestNotAffordableUpgrade = null
        ChocolateFactoryAPI.totalUpgradeCost = 0
    }

    fun updateInventoryItems(inventory: Map<Int, ItemStack>) {
        val profileStorage = profileStorage ?: return

        val chocolateItem = InventoryUtils.getItemAtSlotIndex(ChocolateFactoryAPI.infoIndex) ?: return
        val prestigeItem = InventoryUtils.getItemAtSlotIndex(ChocolateFactoryAPI.prestigeIndex) ?: return
        val timeTowerItem = InventoryUtils.getItemAtSlotIndex(ChocolateFactoryAPI.timeTowerIndex) ?: return
        val productionInfoItem = InventoryUtils.getItemAtSlotIndex(ChocolateFactoryAPI.productionInfoIndex) ?: return
        val leaderboardItem = InventoryUtils.getItemAtSlotIndex(ChocolateFactoryAPI.leaderboardIndex) ?: return
        val barnItem = InventoryUtils.getItemAtSlotIndex(ChocolateFactoryAPI.barnIndex) ?: return

        ChocolateFactoryAPI.factoryUpgrades = emptyList()

        processChocolateItem(chocolateItem)
        val list = mutableListOf<ChocolateFactoryUpgrade>()
        processPrestigeItem(list, prestigeItem)
        processTimeTowerItem(timeTowerItem)
        processProductionItem(productionInfoItem)
        processLeaderboardItem(leaderboardItem)
        processBarnItem(barnItem)

        profileStorage.rawChocPerSecond = (ChocolateFactoryAPI.chocolatePerSecond / profileStorage.chocolateMultiplier + .01).toInt()
        profileStorage.lastDataSave = SimpleTimeMark.now()

        ChocolateFactoryStats.updateDisplay()

        processInventory(list, inventory)

        findBestUpgrades(list)
        if (config.showAllBestUpgrades) findAllBestUpgrades(list)

        ChocolateFactoryAPI.factoryUpgrades = list
    }

    private fun processChocolateItem(item: ItemStack) {
        val profileStorage = profileStorage ?: return

        ChocolateFactoryAPI.chocolateAmountPattern.matchMatcher(item.name.removeColor()) {
            profileStorage.currentChocolate = group("amount").formatLong()
        }
        for (line in item.getLore()) {
            chocolatePerSecondPattern.matchMatcher(line) {
                ChocolateFactoryAPI.chocolatePerSecond = group("amount").formatDouble()
            }
            chocolateAllTimePattern.matchMatcher(line) {
                profileStorage.chocolateAllTime = group("amount").formatLong()
            }
        }
    }

    private fun processPrestigeItem(list: MutableList<ChocolateFactoryUpgrade>, item: ItemStack) {
        val profileStorage = profileStorage ?: return

        prestigeLevelPattern.matchMatcher(item.name) {
            ChocolateFactoryAPI.currentPrestige = group("prestige").romanToDecimal()
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
                ChocolateFactoryAPI.chocolateForPrestige = group("amount").formatLong()
                prestigeCost = ChocolateFactoryAPI.chocolateForPrestige
            }
        }
        val prestigeUpgrade = ChocolateFactoryUpgrade(
            ChocolateFactoryAPI.prestigeIndex,
            ChocolateFactoryAPI.currentPrestige,
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
        ChocolateFactoryAPI.leaderboardPosition = null
        ChocolateFactoryAPI.leaderboardPercentile = null

        for (line in item.getLore()) {
            leaderboardPlacePattern.matchMatcher(line) {
                ChocolateFactoryAPI.leaderboardPosition = group("position").formatInt()
            }
            leaderboardPercentilePattern.matchMatcher(line) {
                ChocolateFactoryAPI.leaderboardPercentile = group("percent").formatDouble()
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
            timeTowerStatusPattern.matchMatcher(line) {
                val activeTime = group("acitveTime")
                if (activeTime.isNotEmpty()) {
                    // todo in future fix this issue with TimeUtils.getDuration
                    val formattedGroup = activeTime.replace("h", "h ").replace("m", "m ")

                    val activeDuration = TimeUtils.getDuration(formattedGroup)
                    val activeUntil = SimpleTimeMark.now() + activeDuration
                    profileStorage.currentTimeTowerEnds = activeUntil
                } else {
                    profileStorage.currentTimeTowerEnds = SimpleTimeMark.farPast()
                }
            }
            timeTowerRechargePattern.matchMatcher(line) {
                // todo in future fix this issue with TimeUtils.getDuration
                val formattedGroup = group("duration").replace("h", "h ").replace("m", "m ")

                val timeUntilTower = TimeUtils.getDuration(formattedGroup)
                val nextTimeTower = SimpleTimeMark.now() + timeUntilTower
                profileStorage.nextTimeTower = nextTimeTower
            }
        }
    }

    private fun processInventory(list: MutableList<ChocolateFactoryUpgrade>, inventory: Map<Int, ItemStack>) {
        ChocolateFactoryAPI.clickRabbitSlot = null

        for ((slotIndex, item) in inventory) {
            processItem(list, item, slotIndex)
        }
    }

    private fun processItem(list: MutableList<ChocolateFactoryUpgrade>, item: ItemStack, slotIndex: Int) {
        if (slotIndex == ChocolateFactoryAPI.prestigeIndex) return

        handleRabbitWarnings(item, slotIndex)

        if (slotIndex !in ChocolateFactoryAPI.otherUpgradeSlots && slotIndex !in ChocolateFactoryAPI.rabbitSlots) return

        val itemName = item.name.removeColor()
        val lore = item.getLore()
        val upgradeCost = ChocolateFactoryAPI.getChocolateBuyCost(lore)
        val averageChocolate = ChocolateAmount.averageChocPerSecond().roundTo(2)
        val isMaxed = upgradeCost == null

        if (slotIndex in ChocolateFactoryAPI.rabbitSlots) {
            handleRabbitSlot(list, itemName, slotIndex, isMaxed, upgradeCost, averageChocolate)
        } else if (slotIndex in ChocolateFactoryAPI.otherUpgradeSlots) {
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

        val chocolateIncrease = ChocolateFactoryAPI.rabbitSlots[slotIndex] ?: 0
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

        if (slotIndex == ChocolateFactoryAPI.timeTowerIndex) {
            this.profileStorage?.timeTowerLevel = level
        }

        if (isMaxed) {
            val otherUpgrade = ChocolateFactoryUpgrade(slotIndex, level, null)
            list.add(otherUpgrade)
            return
        }

        val newAverageChocolate = when (slotIndex) {
            ChocolateFactoryAPI.timeTowerIndex -> ChocolateAmount.averageChocPerSecond(includeTower = true)
            ChocolateFactoryAPI.coachRabbitIndex -> ChocolateAmount.averageChocPerSecond(baseMultiplierIncrease = 0.01)
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

    private fun shouldSearchForBestUpgrades(curChocolate: Long, currentUpgrades: List<ChocolateFactoryUpgrade>): Boolean {
        // check if any of the current upgrades is of higher level then previous calculated best upgrades
        val isCurrentHigher = currentUpgrades.any { current ->
            val upgrade = ChocolateFactoryAPI.allBestPossibleUpgrades[current.slotIndex]?.lastOrNull()
            val lvlOnLastRun = ChocolateFactoryAPI.lastUpgradesWhenChecking[current.slotIndex]
            return@any lvlOnLastRun == null ||
                (upgrade == null && lvlOnLastRun.level < current.level) ||
                (upgrade != null && current.level > upgrade.level)
        }

        if (!isCurrentHigher) {
            // remove all upgrades that have been done
            for (current in currentUpgrades) {
                while ((ChocolateFactoryAPI.allBestPossibleUpgrades[current.slotIndex]?.firstOrNull()?.level ?: current.level)
                    < current.level
                ) {
                    ChocolateFactoryAPI.totalUpgradeCost -=
                        ChocolateFactoryAPI.allBestPossibleUpgrades[current.slotIndex]?.firstOrNull()?.price ?: 0L

                    ChocolateFactoryAPI.allBestPossibleUpgrades[current.slotIndex]?.removeFirst()
                }
            }

            // Check weather the last best upgrade that couldn't be afforded can now be afforded
            // If not just return.
            if (ChocolateFactoryAPI.totalUpgradeCost < curChocolate &&
                ChocolateFactoryAPI.totalUpgradeCost +
                (ChocolateFactoryAPI.lastBestNotAffordableUpgrade?.price ?: 0L) > curChocolate
            ) return false
        }
        return true
    }

    private fun findAllBestUpgrades(list: List<ChocolateFactoryUpgrade>) {

        val curChocolate = ChocolateAmount.CURRENT.chocolate() + ChocolateAmount.chocolateSinceUpdate()

        val currentUpgrades = ArrayList(
            list.filter { it.extraPerSecond != null && it.extraPerSecond > 0 }
                .filter { it.slotIndex != ChocolateFactoryAPI.timeTowerIndex },
        )

        if (!shouldSearchForBestUpgrades(curChocolate, currentUpgrades)) return
        ChocolateFactoryAPI.allBestPossibleUpgrades = emptyMap()

        ChocolateFactoryAPI.lastUpgradesWhenChecking = currentUpgrades.associateBy { it.slotIndex }

        val bestUpgrades = hashMapOf<Int, Deque<ChocolateFactoryUpgrade>>()
        for (upgrade in currentUpgrades) {
            bestUpgrades[upgrade.slotIndex] = LinkedList()
        }
        val remainingChocolate = findAllBestUpgradesImpl(
            currentUpgrades,
            bestUpgrades,
            curChocolate
        )
        val totalCost = curChocolate - remainingChocolate

        ChocolateFactoryAPI.allBestPossibleUpgrades = bestUpgrades
        ChocolateFactoryAPI.totalUpgradeCost = totalCost
    }

    /** Find the best possible upgrades for the current chocolate amount.
     * Should only ever get called by [findAllBestUpgrades].
     *
     * @param list list of current upgrades
     * @param allUpgrades map of all upgrades
     * @param remainingChocolate remaining chocolate to spend
     * @param baseIncreaseAfterUpgrades total base increase after upgrades
     * @param multiplierIncreaseAfterUpgrades total multiplier increase after upgrades
     */
    private fun findAllBestUpgradesImpl(
        list: ArrayList<ChocolateFactoryUpgrade>,
        allUpgrades: MutableMap<Int, Deque<ChocolateFactoryUpgrade>>,
        remainingChocolate: Long = profileStorage?.currentChocolate ?: 0,
        baseIncreaseAfterUpgrades: Int = 0,
        multiplierIncreaseAfterUpgrades: Double = 0.0
    ): Long {

        // ---------------- Find best possible upgrade ----------------

        val notMaxed = list.filter { !it.isMaxed }

        // find the best current upgrade out of the current possible upgrades
        val bestUpgrade = notMaxed.minByOrNull { it.effectiveCost ?: Double.MAX_VALUE }


        //  No best upgrade (all upgrades are maxed -> bestUpgrade = null) or cant afford best upgrade
        if (bestUpgrade == null || (bestUpgrade.price ?: Long.MAX_VALUE) > remainingChocolate) {
            // best upgrades found

            ChocolateFactoryAPI.lastBestNotAffordableUpgrade = bestUpgrade
            return remainingChocolate
        }

        // ---------------- simulate making the upgrade ----------------

        // Keep track of total base increase and multiplier increase after upgrades.
        val nextBaseIncrease = baseIncreaseAfterUpgrades + (ChocolateFactoryAPI.rabbitSlots[bestUpgrade.slotIndex] ?: 0)
        val nextMultiplierIncrease = multiplierIncreaseAfterUpgrades +
            if (bestUpgrade.slotIndex == ChocolateFactoryAPI.coachRabbitIndex) 0.01 else 0.0

        // Should never throw since empty lists are added in caller method.
        allUpgrades[bestUpgrade.slotIndex]?.add(bestUpgrade) ?: throw IllegalStateException("Best upgrade not found in list")

        // Replace bestUpdate with it's next level
        list[list.indexOf(bestUpgrade)] = getNextUpgrade(bestUpgrade)

        // Add new rabbit if unlocked via reaching level 20 on current rabbit.
        if (bestUpgrade.isRabbit && bestUpgrade.level == 19) {
            val nextRabbit = getNextUnlockedRabbitWorker(bestUpgrade)
            if (nextRabbit != null) {
                list.add(nextRabbit)
                allUpgrades[nextRabbit.slotIndex] = LinkedList()
            }
        }

        // Update extra per second and effective costs for all current upgrades.
        updateEffectiveCost(list, nextBaseIncrease, nextMultiplierIncrease)

        // recursive call to find the next best upgrade after spending the cost of the best upgrade.
        return findAllBestUpgradesImpl(
            list,
            allUpgrades,
            remainingChocolate - (bestUpgrade.price ?: Long.MAX_VALUE),
            nextBaseIncrease,
            nextMultiplierIncrease,
        )
    }

    private fun updateEffectiveCost(upgrades: MutableList<ChocolateFactoryUpgrade>, baseIncrease: Int, multiplierIncrease: Double) {
        val beforeChocPerSec = ChocolateAmount.averageChocPerSecond(
            rawPerSecondIncrease = baseIncrease,
            baseMultiplierIncrease = multiplierIncrease,
        )
        for (i in upgrades.indices) {
            val afterChocPerSec = ChocolateAmount.averageChocPerSecond(
                rawPerSecondIncrease = baseIncrease + (ChocolateFactoryAPI.rabbitSlots[upgrades[i].slotIndex] ?: 0),
                baseMultiplierIncrease = multiplierIncrease +
                    if (upgrades[i].slotIndex == ChocolateFactoryAPI.coachRabbitIndex) 0.01 else 0.0,
            )

            val extra: Double = (afterChocPerSec - beforeChocPerSec).roundTo(2)
            val effectiveCost: Double? = upgrades[i].price?.div(extra)?.roundTo(2)
            upgrades[i] = upgrades[i].copy(extraPerSecond = extra, effectiveCost = effectiveCost)
        }
    }

    private fun handleRabbitWarnings(item: ItemStack, slotIndex: Int) {
        val isGoldenRabbit = clickMeGoldenRabbitPattern.matches(item.name)
        val warningConfig = config.rabbitWarning

        if (clickMeRabbitPattern.matches(item.name) || isGoldenRabbit) {
            if (config.rabbitWarning.rabbitWarning) {
                SoundUtils.playBeepSound()
            }

            if (warningConfig.specialRabbitWarning && (isGoldenRabbit || item.getSkullTexture() in specialRabbitTextures)) {
                SoundUtils.repeatSound(100, warningConfig.repeatSound, ChocolateFactoryAPI.warningSound)
            }

            ChocolateFactoryAPI.clickRabbitSlot = slotIndex
        }
    }

    private fun findBestUpgrades(list: List<ChocolateFactoryUpgrade>) {
        val profileStorage = profileStorage ?: return

        val ttFiltered = list.filter {
            config.chocolateUpgradeWarnings.upgradeWarningTimeTower.get() || it.slotIndex != ChocolateFactoryAPI.timeTowerIndex
        }

        val notMaxed = ttFiltered.filter {
            !it.isMaxed && it.effectiveCost != null
        }

        val bestUpgrade = notMaxed.minByOrNull { it.effectiveCost ?: Double.MAX_VALUE }
        profileStorage.bestUpgradeAvailableAt = bestUpgrade?.canAffordAt ?: SimpleTimeMark.farPast()
        profileStorage.bestUpgradeCost = bestUpgrade?.price ?: 0
        ChocolateFactoryAPI.bestPossibleSlot = bestUpgrade?.getValidUpgradeIndex() ?: -1

        val bestUpgradeLevel = bestUpgrade?.level ?: 0
        ChocolateFactoryUpgradeWarning.checkUpgradeChange(ChocolateFactoryAPI.bestPossibleSlot, bestUpgradeLevel)

        val affordAbleUpgrade = notMaxed.filter { it.canAfford() }.minByOrNull { it.effectiveCost ?: Double.MAX_VALUE }
        ChocolateFactoryAPI.bestAffordableSlot = affordAbleUpgrade?.getValidUpgradeIndex() ?: -1
    }
}

