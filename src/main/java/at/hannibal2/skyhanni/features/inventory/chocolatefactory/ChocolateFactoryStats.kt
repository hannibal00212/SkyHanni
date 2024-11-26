package at.hannibal2.skyhanni.features.inventory.chocolatefactory

import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.features.event.hoppity.HoppityAPI.isAlternateDay
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEventSummary
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.hitman.HitmanAPI.extraSlotsInDuration
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.hitman.HitmanAPI.getOpenSlots
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.hitman.HitmanAPI.getTimeToHuntCount
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.hitman.HitmanAPI.getTimeToNumSlots
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ClipboardUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.toRoman
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.asTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.renderables.Renderable
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration

@SkyHanniModule
object ChocolateFactoryStats {

    private val config get() = ChocolateFactoryAPI.config
    private val profileStorage get() = ChocolateFactoryAPI.profileStorage

    private var display = listOf<Renderable>()

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!ChocolateFactoryAPI.chocolateFactoryPaused) return
        updateDisplay()
    }

    @SubscribeEvent
    fun onBackgroundDraw(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        if (!ChocolateFactoryAPI.inChocolateFactory && !ChocolateFactoryAPI.chocolateFactoryPaused) return
        if (!config.statsDisplay) return

        config.position.renderRenderables(display, posLabel = "Chocolate Factory Stats")
    }

    @Suppress("LongMethod")
    fun updateDisplay() {
        val profileStorage = profileStorage ?: return

        val perSecond = ChocolateFactoryAPI.chocolatePerSecond
        val perMinute = perSecond * 60
        val perHour = perMinute * 60
        val perDay = perHour * 24

        val position = ChocolateFactoryAPI.leaderboardPosition
        val positionText = position?.addSeparators() ?: "???"
        val percentile = ChocolateFactoryAPI.leaderboardPercentile
        val percentileText = percentile?.let { "§7Top §a$it%" }.orEmpty()
        val leaderboard = "#$positionText $percentileText"
        ChocolatePositionChange.update(position, leaderboard)
        HoppityEventSummary.updateCfPosition(position, percentile)

        val timeTowerInfo = if (ChocolateFactoryTimeTowerManager.timeTowerActive()) {
            "§d§lActive"
        } else {
            "§6${ChocolateFactoryTimeTowerManager.timeTowerCharges()}"
        }

        val timeTowerFull = ChocolateFactoryTimeTowerManager.timeTowerFullTimeMark()

        val prestigeEstimate = ChocolateAmount.PRESTIGE.formattedTimeUntilGoal(ChocolateFactoryAPI.chocolateForPrestige)
        val chocolateUntilPrestigeCalculation =
            ChocolateFactoryAPI.chocolateForPrestige - ChocolateAmount.PRESTIGE.chocolate()

        var chocolateUntilPrestige = "§6${chocolateUntilPrestigeCalculation.addSeparators()}"

        if (chocolateUntilPrestigeCalculation <= 0) {
            chocolateUntilPrestige = "§aPrestige Available"
        }

        val upgradeAvailableAt = ChocolateAmount.CURRENT.formattedTimeUntilGoal(profileStorage.bestUpgradeCost)

        val hitmanStats = profileStorage.hitmanStats
        val availableHitmanEggs = hitmanStats.availableEggs?.takeIf { it > 0 }?.toString() ?: "§7None"
        val hitmanSingleSlotCd = hitmanStats.slotCooldown?.takeIf { it.isInFuture() }?.timeUntil()?.format() ?: "§aAll Ready"
        val hitmanAllSlotsCd = hitmanStats.allSlotsCooldown?.takeIf { it.isInFuture() }?.timeUntil()?.format() ?: "§aAll Ready"

        val hitman28TimeToSlots = hitmanStats.getTimeToNumSlots(28)
        val hitman28TimeToHunts = hitmanStats.getTimeToHuntCount(28)
        val hitman28InhibitedBySpawns = hitman28TimeToHunts > hitman28TimeToSlots

        val hitman28ClaimsReadyTime = when {
            hitman28InhibitedBySpawns -> hitman28TimeToHunts
            else -> {
                // We're inhibited by when slots are available
                val timeMarkThen = SimpleTimeMark.now() + hitman28TimeToSlots
                // Figure out when the next egg spawn after that time is
                val sbTimeThen = timeMarkThen.toSkyBlockTime()
                val sortedEntries = HoppityEggType.resettingEntries.sortedBy { it.resetsAt }
                val nextMeal = sortedEntries.firstOrNull {
                    it.resetsAt > sbTimeThen.hour && it.altDay == sbTimeThen.isAlternateDay()
                } ?: sortedEntries.firstOrNull {
                    it.resetsAt < sbTimeThen.hour && it.altDay != sbTimeThen.isAlternateDay()
                }
                sbTimeThen.copy(hour = nextMeal?.resetsAt ?: 0, minute = 0, second = 0).asTimeMark().timeUntil()
            }
        }

        val hitman28ClaimsReady = hitman28ClaimsReadyTime.takeIf { it > Duration.ZERO }?.format() ?: "§aReady Now"

        var hitmanSlotsFullTime = Duration.ZERO
        val openSlotsNow = hitmanStats.getOpenSlots()
        var runningOpenSlots = openSlotsNow
        while (runningOpenSlots > 0) {
            // See how long it will take to fill those slots
            val timeToFill = hitmanStats.getTimeToHuntCount(runningOpenSlots)
            // Determine how many extra slots will be available after that time
            runningOpenSlots = hitmanStats.extraSlotsInDuration(timeToFill, runningOpenSlots)
            hitmanSlotsFullTime += timeToFill
        }
        val hitmanSlotsFull =
            if (openSlotsNow == 0) "§7Cooldown..."
            else hitmanSlotsFullTime.takeIf { it > Duration.ZERO }?.format() ?: "§cFull Now"

        val map = buildMap {
            put(ChocolateFactoryStat.HEADER, "§6§lChocolate Factory ${ChocolateFactoryAPI.currentPrestige.toRoman()}")

            val maxSuffix = if (ChocolateFactoryAPI.isMax()) {
                " §cMax!"
            } else ""
            put(ChocolateFactoryStat.CURRENT, "§eCurrent Chocolate: §6${ChocolateAmount.CURRENT.formatted}$maxSuffix")
            put(ChocolateFactoryStat.THIS_PRESTIGE, "§eThis Prestige: §6${ChocolateAmount.PRESTIGE.formatted}")
            put(ChocolateFactoryStat.ALL_TIME, "§eAll-time: §6${ChocolateAmount.ALL_TIME.formatted}")

            put(ChocolateFactoryStat.PER_SECOND, "§ePer Second: §6${perSecond.addSeparators()}")
            put(ChocolateFactoryStat.PER_MINUTE, "§ePer Minute: §6${perMinute.addSeparators()}")
            put(ChocolateFactoryStat.PER_HOUR, "§ePer Hour: §6${perHour.addSeparators()}")
            put(ChocolateFactoryStat.PER_DAY, "§ePer Day: §6${perDay.addSeparators()}")

            put(ChocolateFactoryStat.MULTIPLIER, "§eChocolate Multiplier: §6${profileStorage.chocolateMultiplier}")
            put(ChocolateFactoryStat.BARN, "§eBarn: §6${ChocolateFactoryBarnManager.barnStatus()}")

            put(ChocolateFactoryStat.LEADERBOARD_POS, "§ePosition: §b$leaderboard")

            put(ChocolateFactoryStat.EMPTY, "")
            put(ChocolateFactoryStat.EMPTY_2, "")
            put(ChocolateFactoryStat.EMPTY_3, "")
            put(ChocolateFactoryStat.EMPTY_4, "")
            put(ChocolateFactoryStat.EMPTY_5, "")

            put(ChocolateFactoryStat.TIME_TOWER, "§eTime Tower: §6$timeTowerInfo")
            put(
                ChocolateFactoryStat.TIME_TOWER_FULL,
                if (ChocolateFactoryTimeTowerManager.timeTowerFull()) {
                    "§eFull Tower Charges: §a§lNow\n" +
                        "§eHappens at: §a§lNow"
                } else {
                    "§eFull Tower Charges: §b${timeTowerFull.timeUntil().format()}\n" +
                        "§eHappens at: §b${timeTowerFull.formattedDate("EEEE, MMM d h:mm a")}"
                },
            )
            put(
                ChocolateFactoryStat.RAW_PER_SECOND,
                "§eRaw Per Second: §6${profileStorage.rawChocPerSecond.addSeparators()}",
            )

            val allTime = ChocolateAmount.ALL_TIME.chocolate()
            val nextChocolateMilestone = ChocolateFactoryAPI.getNextMilestoneChocolate(allTime)
            val amountUntilNextMilestone = nextChocolateMilestone - allTime
            val amountFormat = amountUntilNextMilestone.addSeparators()
            val maxMilestoneEstimate = ChocolateAmount.ALL_TIME.formattedTimeUntilGoal(nextChocolateMilestone)
            val prestigeData = when {
                !ChocolateFactoryAPI.isMaxPrestige() -> mapOf(
                    ChocolateFactoryStat.TIME_TO_PRESTIGE to "§eTime To Prestige: $prestigeEstimate",
                    ChocolateFactoryStat.CHOCOLATE_UNTIL_PRESTIGE to "§eChocolate To Prestige: §6$chocolateUntilPrestige"
                )
                amountUntilNextMilestone >= 0 -> mapOf(
                    ChocolateFactoryStat.TIME_TO_PRESTIGE to "§eTime To Next Milestone: $maxMilestoneEstimate",
                    ChocolateFactoryStat.CHOCOLATE_UNTIL_PRESTIGE to "§eChocolate To Next Milestone: §6$amountFormat"
                )
                else -> emptyMap()
            }
            putAll(prestigeData)

            put(ChocolateFactoryStat.TIME_TO_BEST_UPGRADE, "§eBest Upgrade: $upgradeAvailableAt")

            put(ChocolateFactoryStat.HITMAN_HEADER, "§c§lRabbit Hitman")
            put(ChocolateFactoryStat.AVAILABLE_HITMAN_EGGS, "§eAvailable Hitman Eggs: §b$availableHitmanEggs")
            put(ChocolateFactoryStat.HITMAN_SLOT_COOLDOWN, "§eHitman Slot Cooldown: §b$hitmanSingleSlotCd")
            put(ChocolateFactoryStat.HITMAN_ALL_SLOTS, "§eAll Hitman Slots Cooldown: §b$hitmanAllSlotsCd")
            put(ChocolateFactoryStat.HITMAN_FULL_SLOTS, "§eFull Hitman Slots: §b$hitmanSlotsFull")
            put(ChocolateFactoryStat.HITMAN_28_SLOTS, "§e28 Hitman Claims: §b$hitman28ClaimsReady")
        }
        val text = config.statsDisplayList.filter { it.shouldDisplay() }.flatMap { map[it]?.split("\n").orEmpty() }

        display = listOf(
            Renderable.clickAndHover(
                Renderable.verticalContainer(text.map(Renderable::string)),
                tips = listOf("§bCopy to Clipboard!"),
                onClick = {
                    val list = text.toMutableList()
                    list.add(0, "${LorenzUtils.getPlayerName()}'s Chocolate Factory Stats")

                    ClipboardUtils.copyToClipboard(list.joinToString("\n") { it.removeColor() })
                },
            ),
        )
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.transform(42, "event.chocolateFactory.statsDisplayList") { element ->
            addToDisplayList(element, "TIME_TOWER", "TIME_TO_PRESTIGE")
        }
        event.transform(45, "inventory.chocolateFactory.statsDisplayList") { element ->
            addToDisplayList(element, "TIME_TO_BEST_UPGRADE")
        }
    }

    private fun addToDisplayList(element: JsonElement, vararg toAdd: String): JsonElement {
        val jsonArray = element.asJsonArray
        toAdd.forEach { jsonArray.add(JsonPrimitive(it)) }
        return jsonArray
    }

    enum class ChocolateFactoryStat(private val display: String, val shouldDisplay: () -> Boolean = { true }) {
        HEADER("§6§lChocolate Factory Stats"),
        CURRENT("§eCurrent Chocolate: §65,272,230"),
        THIS_PRESTIGE("§eThis Prestige: §6483,023,853", { ChocolateFactoryAPI.currentPrestige != 1 }),
        ALL_TIME("§eAll-time: §6641,119,115"),
        PER_SECOND("§ePer Second: §63,780.72"),
        PER_MINUTE("§ePer Minute: §6226,843.2"),
        PER_HOUR("§ePer Hour: §613,610,592"),
        PER_DAY("§ePer Day: §6326,654,208"),
        MULTIPLIER("§eChocolate Multiplier: §61.77"),
        BARN("§eBarn: §6171/190 Rabbits"),
        LEADERBOARD_POS("§ePosition: §b#103 §7Top §a0.87%"),
        EMPTY(""),
        EMPTY_2(""),
        EMPTY_3(""),
        EMPTY_4(""),
        EMPTY_5(""),
        TIME_TOWER("§eTime Tower: §62/3 Charges", { ChocolateFactoryTimeTowerManager.currentCharges() != -1 }),
        TIME_TOWER_FULL(
            "§eTime Tower Full Charges: §b5h 13m 59s\n§bHappens at: Monday, May 13 5:32 AM",
            { ChocolateFactoryTimeTowerManager.currentCharges() != -1 || ChocolateFactoryTimeTowerManager.timeTowerFull() },
        ),
        TIME_TO_PRESTIGE("§eTime To Prestige: §b1d 13h 59m 4s"),
        RAW_PER_SECOND("§eRaw Per Second: §62,136"),
        CHOCOLATE_UNTIL_PRESTIGE("§eChocolate To Prestige: §65,851"),
        TIME_TO_BEST_UPGRADE(
            "§eBest Upgrade: §b 59m 4s",
            { ChocolateFactoryAPI.profileStorage?.bestUpgradeCost != 0L },
        ),
        HITMAN_HEADER("§c§lRabbit Hitman"),
        AVAILABLE_HITMAN_EGGS("§eAvailable Hitman Eggs: §b3"),
        HITMAN_SLOT_COOLDOWN("§eHitman Slot Cooldown: §b8m 6s"),
        HITMAN_ALL_SLOTS("§eAll Hitman Slots Cooldown: §b8h 8m 6s"),
        HITMAN_FULL_SLOTS("§eFull Hitman Slots: §b2h 10m"),
        HITMAN_28_SLOTS("§e28 Hitman Claims: §b3h 20m"),
        ;

        override fun toString(): String {
            return display
        }
    }
}
