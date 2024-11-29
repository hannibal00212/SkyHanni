package at.hannibal2.skyhanni.features.garden.contest

import at.hannibal2.skyhanni.data.ClickType
import at.hannibal2.skyhanni.events.CropClickEvent
import at.hannibal2.skyhanni.events.FarmingContestEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.ScoreboardUpdateEvent
import at.hannibal2.skyhanni.events.TabListUpdateEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.formatLong
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.NumberUtil.prettyRound
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.renderStrings
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeUtils
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@SkyHanniModule
object JacobContestStatsSummary {

    private val config get() = GardenAPI.config.jacobContestStats
    private var blocksBroken = 0
    private var startTimeIrl = SimpleTimeMark.farPast()
    private var startTimeRelative: Long? = null
    private var startScore: Long? = null
    private var predictedScore: Long? = null
    private var percent: Double? = null
    private var medalColor = ""
    private var contestStats = mutableListOf<String>()

    private val tabContestPattern by RepoPattern.pattern(
        "garden.jacob.contest.tab.data",
        " §r(§e○|§6☘) §r§f(?<crop>.+) §r§f◆ Top §r(?<color>§.)(?<percent>(\\d|[.])+)%"
    )
    private val scoreboardContestTimeLeftPattern by RepoPattern.pattern(
        "garden.jacob.contest.scoreboard.time.left",
        " (§e○|§6☘) §f(?<crop>.+) §a(?<time>\\d+m\\d+s)"
    )
    private val scoreboardContestScorePattern by RepoPattern.pattern(
        "garden.jacob.contest.scoreboard.score",
        " (Collected|(?<medal>§b§lDIAMOND|§3§lPLATINUM|§6§lGOLD|§f§lSILVER|§c§lBRONZE) §fwith) §e(?<amount>.+)"
    )

    @SubscribeEvent
    fun onCropClick(event: CropClickEvent) {
        if (!isEnabled()) return
        if (!FarmingContestAPI.inContest) return
        if (event.clickType != ClickType.LEFT_CLICK) return

        if (event.crop == FarmingContestAPI.contestCrop) {
            blocksBroken++
        }
    }

    @SubscribeEvent
    fun onTabListUpdate(event: TabListUpdateEvent) {
        if (!isEnabled()) return
        if (!FarmingContestAPI.inContest) return

        for (line in event.tabList) {
            tabContestPattern.matchMatcher(line) {
                if (group("crop") != FarmingContestAPI.contestCrop?.cropName) return
                percent = group("percent").toDouble() / 100
                medalColor = group("color")
                update()
            }
        }
    }

    @SubscribeEvent
    fun onScoreBoardUpdate(event: ScoreboardUpdateEvent) {
        if (!isEnabled()) return
        if (!FarmingContestAPI.inContest) return

        var timeLeft = 0L
        var amount = 0L

        for (line in event.scoreboard) {
            scoreboardContestTimeLeftPattern.matchMatcher(line) {
                timeLeft = TimeUtils.getDuration(group("time")).inWholeSeconds
            }
            scoreboardContestScorePattern.matchMatcher(line) {
                amount = group("amount").fixScoreAmount()
            }
        }

        val fixedStartTime = startTimeRelative
        val fixedStartScore = startScore
        if (fixedStartTime == null || fixedStartScore == null) {
            startTimeRelative = 1200 - timeLeft
            startScore = amount
            predictedScore = null
        } else {
            val amountGained = amount - fixedStartScore
            val timeParticipated = 1200 - timeLeft - fixedStartTime
            predictedScore = amount + amountGained / timeParticipated * (1200 - fixedStartTime)
        }
        update()
    }

    private fun String.fixScoreAmount(): Long =
        this.formatLong() * when (this.split(',').last().length) {
            2 -> 10
            1 -> 100
            else -> 1
        }

    fun update() {
        val formattedStartTime = ((startTimeRelative ?: return) * 1000 - 999).seconds.format()
        contestStats.clear()
        val cropName = FarmingContestAPI.contestCrop?.cropName
        val duration = startTimeIrl.passedSince()
        val durationInSeconds = duration.toDouble(DurationUnit.SECONDS)
        val timeParticipated = duration.format()
        val blocksPerSecond = (blocksBroken.toDouble() / durationInSeconds).roundTo(2)
        val formattedPercent = (percent?.times(100))?.prettyRound(1)
        val position = if (percent == null) "§eNo data yet" else "Top $medalColor$formattedPercent%"

        val unsortedList = mutableListOf<String>()
        unsortedList.add("§e§l$cropName Contest Stats")
        unsortedList.add("§7Started §b$formattedStartTime §7into contest")
        unsortedList.add("§7Participating for §b$timeParticipated")
        unsortedList.add("§7Blocks Broken: §e${blocksBroken.addSeparators()}")
        unsortedList.add("§7Blocks per Second: §c$blocksPerSecond")
        unsortedList.add("§7Position: $position")
        unsortedList.add("§7Predicted Score: §e${predictedScore?.addSeparators() ?: "Calculating..."}")


        for (index in config.text) {
            contestStats.add(unsortedList[index.ordinal])
        }
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (!FarmingContestAPI.inContest) return

        config.pos.renderStrings(contestStats, 2, "Jacob Contest Stats")
    }

    @SubscribeEvent
    fun onFarmingContest(event: FarmingContestEvent) {
        if (!isEnabled()) return

        when (event.phase) {
            FarmingContestPhase.START -> {
                ChatUtils.chat("Started tracking your Jacob Contest Blocks Per Second!")
                startTimeIrl = SimpleTimeMark.now()
            }

            FarmingContestPhase.STOP -> {
                val cropName = event.crop.cropName
                val duration = startTimeIrl.passedSince()
                val durationInSeconds = duration.toDouble(DurationUnit.SECONDS)
                val blocksPerSecond = (blocksBroken.toDouble() / durationInSeconds).roundTo(2)
                val time = startTimeIrl.passedSince().format()
                val formattedPercent = (percent?.times(100))?.prettyRound(1)
                val position = if (percent == null) "§eNo data yet" else "Top $medalColor$formattedPercent%"

                ChatUtils.chat("§l$cropName Contest Stats")
                ChatUtils.chat("§7Participated for §b$time")
                ChatUtils.chat("§7Total Blocks Broken: §e${blocksBroken.addSeparators()}")
                ChatUtils.chat("§7Average Blocks per Second: §c$blocksPerSecond")
                ChatUtils.chat("§7Position: $position")
            }

            FarmingContestPhase.CHANGE -> {
                ChatUtils.chat("You changed the crop during the contest, resetting the Blocks Per Second calculation..")
                startTimeIrl = SimpleTimeMark.now()
            }
        }
        startTimeRelative = null
        percent = null
        blocksBroken = 0
    }

    fun isEnabled() = GardenAPI.inGarden() && config.jacobContestSummary
}
