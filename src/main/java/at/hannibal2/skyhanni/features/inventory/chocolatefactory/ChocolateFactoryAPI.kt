package at.hannibal2.skyhanni.features.inventory.chocolatefactory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.features.inventory.chocolatefactory.ChocolateFactoryConfig
import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage.ChocolateFactoryStorage
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.data.jsonobjects.repo.HoppityEggLocationsJson
import at.hannibal2.skyhanni.data.jsonobjects.repo.MilestoneJson
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.features.event.hoppity.HoppityCollectionStats
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.nextAfter
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.formatLong
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.groupOrNull
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.UtilsPatterns
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.TreeSet
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object ChocolateFactoryAPI {

    val config: ChocolateFactoryConfig get() = SkyHanniMod.feature.inventory.chocolateFactory
    val profileStorage: ChocolateFactoryStorage? get() = ProfileStorageData.profileSpecific?.chocolateFactory

    val patternGroup = RepoPattern.group("misc.chocolatefactory")
    val chocolateAmountPattern by patternGroup.pattern(
        "chocolate.amount",
        "(?<amount>[\\d,]+) Chocolate",
    )
    private val chocolateFactoryInventoryNamePattern by patternGroup.pattern(
        "inventory.name",
        "Hoppity|Chocolate Factory Milestones",
    )

    /**
     * REGEX-TEST: §a§lPROMOTE §8➜ §7[208§7] §dExecutive
     * REGEX-TEST: §a§lUPGRADE §8➜ §aRabbit Barn CCXXI
     */
    private val upgradeLorePattern by patternGroup.pattern(
        "item.lore.upgrade",
        "§a§l(?:UPGRADE|PROMOTE) §8➜ (?:§7\\[(?<nextlevel>\\d+)§7] )?(?<upgradename>.*?) ?(?<nextlevelalt>[IVXLCDM]*)\$",
    )

    /**
     * REGEX-TEST: §bRabbit Bro§8 - §7[220§7] §bBoard Member
     * REGEX-TEST: §6Rabbit Dog§8 - §7[190§7] §6Director
     * REGEX-TEST: §dRabbit Daddy§8 - §7[201§7] §dExecutive
     */
    private val employeeNamePattern by patternGroup.pattern(
        "item.name.employee",
        "(?<employee>(?:§.+)+Rabbit .*)§8 - §7\\[\\d*§7] .*",
    )

    var rabbitSlots = mapOf<Int, Int>()
    var otherUpgradeSlots = setOf<Int>()
    var noPickblockSlots = setOf<Int>()
    var barnIndex = 34
    var infoIndex = 13
    var productionInfoIndex = 45
    var prestigeIndex = 28
    var milestoneIndex = 53
    var leaderboardIndex = 51
    var handCookieIndex = 38
    var timeTowerIndex = 39
    var shrineIndex = 41
    var coachRabbitIndex = 42
    var maxRabbits = 395
    private var chocolateMilestones = TreeSet<Long>()
    private var chocolateFactoryMilestones: MutableList<MilestoneJson> = mutableListOf()
    private var chocolateShopMilestones: MutableList<MilestoneJson> = mutableListOf()
    private var maxPrestige = 5

    var inChocolateFactory = false
    var chocolateFactoryPaused = false

    var currentPrestige = 1
    var chocolatePerSecond = 0.0
    var leaderboardPosition: Int? = null
    var leaderboardPercentile: Double? = null
    var chocolateForPrestige = 150_000_000L

    var clickRabbitSlot: Int? = null

    var factoryUpgrades = listOf<ChocolateFactoryUpgrade>()
    var bestAffordableSlot = -1
    var bestPossibleSlot = -1

    var specialRabbitTextures = listOf<String>()
    var warningSound = SoundUtils.createSound("note.pling", 1f)

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!LorenzUtils.inSkyBlock) return

        if (chocolateFactoryInventoryNamePattern.matches(event.inventoryName)) {
            if (config.enabled) {
                chocolateFactoryPaused = true
                ChocolateFactoryStats.updateDisplay()
            }
            return
        }
        if (event.inventoryName != "Chocolate Factory") return
        inChocolateFactory = true

        if (config.enabled) {
            factoryUpgrades = emptyList()
            DelayedRun.runNextTick {
                ChocolateFactoryDataLoader.updateInventoryItems(event.inventoryItems)
            }
        }
    }

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<HoppityEggLocationsJson>("HoppityEggLocations")

        rabbitSlots = data.rabbitSlots
        otherUpgradeSlots = data.otherUpgradeSlots
        noPickblockSlots = data.noPickblockSlots
        barnIndex = data.barnIndex
        infoIndex = data.infoIndex
        productionInfoIndex = data.productionInfoIndex
        prestigeIndex = data.prestigeIndex
        milestoneIndex = data.milestoneIndex
        leaderboardIndex = data.leaderboardIndex
        handCookieIndex = data.handCookieIndex
        timeTowerIndex = data.timeTowerIndex
        shrineIndex = data.shrineIndex
        coachRabbitIndex = data.coachRabbitIndex
        maxRabbits = data.maxRabbits
        maxPrestige = data.maxPrestige
        chocolateMilestones = data.chocolateMilestones
        chocolateFactoryMilestones = data.chocolateFactoryMilestones.toMutableList()
        chocolateShopMilestones = data.chocolateShopMilestones.toMutableList()
        specialRabbitTextures = data.specialRabbits

        ChocolateFactoryUpgrade.updateIgnoredSlots()
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        val old = "event.chocolateFactory"
        val new = "inventory.chocolateFactory"
        event.move(44, "$old.enabled", "$new.enabled")
        event.move(44, "$old.statsDisplay", "$new.statsDisplay")
        event.move(44, "$old.statsDisplayList", "$new.statsDisplayList")
        event.move(44, "$old.showStackSizes", "$new.showStackSizes")
        event.move(44, "$old.highlightUpgrades", "$new.highlightUpgrades")
        event.move(44, "$old.useMiddleClick", "$new.useMiddleClick")
        event.move(44, "$old.rabbitWarning", "$new.rabbitWarning")
        event.move(44, "$old.barnCapacityThreshold", "$new.barnCapacityThreshold")
        event.move(44, "$old.extraTooltipStats", "$new.extraTooltipStats")
        event.move(44, "$old.timeTowerWarning", "$new.timeTowerWarning")
        event.move(44, "$old.position", "$new.position")
        event.move(44, "$old.compactOnClick", "$new.compactOnClick")
        event.move(44, "$old.compactOnClickAlways", "$new.compactOnClickAlways")
        event.move(44, "$old.tooltipMove", "$new.tooltipMove")
        event.move(44, "$old.tooltipMovePosition", "$new.tooltipMovePosition")
        event.move(44, "$old.hoppityMenuShortcut", "$new.hoppityMenuShortcut")
        event.move(44, "$old.hoppityCollectionStats", "$new.hoppityCollectionStats")
        event.move(44, "$old.hoppityStatsPosition", "$new.hoppityStatsPosition")
    }

    fun getChocolateBuyCost(lore: List<String>): Long? {
        val nextLine = lore.nextAfter({ UtilsPatterns.costLinePattern.matches(it) }) ?: return null
        return chocolateAmountPattern.matchMatcher(nextLine.removeColor()) {
            group("amount").formatLong()
        }
    }

    fun getNextLevelName(stack: ItemStack): String? = upgradeLorePattern.firstMatcher(stack.getLore()) {
        val upgradeName = if (stack.getLore().any { it == "§8Employee" }) employeeNamePattern.matchMatcher(stack.name) {
            groupOrNull("employee")
        } else groupOrNull("upgradename")
        val nextLevel = groupOrNull("nextlevel") ?: groupOrNull("nextlevelalt")
        if (upgradeName == null || nextLevel == null) null
        else "$upgradeName $nextLevel"
    }

    fun getNextMilestoneChocolate(amount: Long): Long {
        return chocolateMilestones.higher(amount) ?: 0
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled

    fun isMaxPrestige() = currentPrestige >= maxPrestige

    fun timeTowerChargeDuration() = if (HoppityCollectionStats.hasFoundRabbit("Einstein")) 7.hours else 8.hours

    fun timeTowerMultiplier(): Double {
        var multiplier = (profileStorage?.timeTowerLevel ?: 0) * 0.1
        if (HoppityCollectionStats.hasFoundRabbit("Mu")) multiplier += 0.7
        return multiplier
    }

    fun timeUntilNeed(goal: Long): Duration {
        var needed = goal
        val profileStorage = profileStorage ?: return Duration.ZERO

        val baseMultiplier = profileStorage.rawChocolateMultiplier
        val rawChocolatePerSecond = profileStorage.rawChocPerSecond

        if (rawChocolatePerSecond == 0) return Duration.INFINITE

        val secondsUntilTowerExpires = ChocolateFactoryTimeTowerManager.timeTowerActiveDuration().inWholeSeconds

        val timeTowerChocPerSecond = rawChocolatePerSecond * (baseMultiplier + timeTowerMultiplier())

        val secondsAtRate = needed / timeTowerChocPerSecond
        if (secondsAtRate < secondsUntilTowerExpires) {
            return secondsAtRate.seconds
        }

        needed -= (secondsUntilTowerExpires * timeTowerChocPerSecond).toLong()
        val basePerSecond = rawChocolatePerSecond * baseMultiplier
        return (needed / basePerSecond + secondsUntilTowerExpires).seconds
    }

    fun milestoneByRabbit(rabbitName: String): MilestoneJson? {
        return chocolateFactoryMilestones.firstOrNull {
            it.rabbit.removeColor() == rabbitName.removeColor()
        } ?: chocolateShopMilestones.firstOrNull {
            it.rabbit.removeColor() == rabbitName.removeColor()
        }
    }
}
