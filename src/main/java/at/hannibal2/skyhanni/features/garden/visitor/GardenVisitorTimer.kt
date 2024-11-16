package at.hannibal2.skyhanni.features.garden.visitor

import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.CropClickEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.garden.visitor.VisitorArrivalEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.features.garden.farming.GardenCropSpeed
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderable
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.TabListData
import at.hannibal2.skyhanni.utils.TimeUtils
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@SkyHanniModule
object GardenVisitorTimer {

    private val config get() = GardenAPI.config.visitors.timer

    private val timePattern by RepoPattern.pattern(
        "garden.visitor.timer.time.new",
        " Next Visitor: §r(?<info>.*)",
    )

    private var display: Renderable? = null
    private var lastMillis = 0.seconds
    private var sixthVisitorArrivalTime = SimpleTimeMark.farPast()
    private var visitorJustArrived = false
    private var sixthVisitorReady = false
    private var lastTimerValue = ""
    private var lastTimerUpdate = SimpleTimeMark.farPast()
    private var lastVisitors: Int = -1

    // TODO nea?
//    private val visitorInterval by dynamic(GardenAPI::config, Storage.ProfileSpecific.GardenStorage::visitorInterval)
    private var visitorInterval: Duration?
        get() = GardenAPI.storage?.visitorInterval?.toDuration(DurationUnit.MILLISECONDS)
        set(value) {
            value?.let {
                GardenAPI.storage?.visitorInterval = it.inWholeMilliseconds
            }
        }

    @SubscribeEvent
    fun onVisitorArrival(event: VisitorArrivalEvent) {
        visitorJustArrived = true
    }

    @SubscribeEvent
    fun onProfileJoin(event: ProfileJoinEvent) {
        display = null
        lastMillis = 0.seconds
        sixthVisitorArrivalTime = SimpleTimeMark.farPast()
        visitorJustArrived = false
        sixthVisitorReady = false
    }

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return

        var visitorsAmount = VisitorAPI.visitorsInTabList(TabListData.getTabList()).size
        var visitorInterval = visitorInterval ?: return
        var millis = visitorInterval
        var queueFull = false

        timePattern.firstMatcher(TabListData.getTabList()) {
            val timeInfo = group("info").removeColor()
            if (timeInfo == "Not Unlocked!") {
                display = Renderable.string("§cVisitors not unlocked!")
                return
            }
            if (timeInfo == "Queue Full!") {
                queueFull = true
            } else {
                if (lastTimerValue != timeInfo) {
                    lastTimerUpdate = SimpleTimeMark.now()
                    lastTimerValue = timeInfo
                }
                millis = TimeUtils.getDuration(timeInfo)
            }
        } ?: run {
            display = createDisplayText("§cVisitor time info not in tab list")
            return
        }

        if (lastVisitors != -1 && visitorsAmount - lastVisitors == 1) {
            if (!queueFull) {
                visitorInterval = millis
                this.visitorInterval = visitorInterval
            } else {
                updateSixthVisitorArrivalTime()
            }
        }

        if (queueFull) {
            if (visitorJustArrived && visitorsAmount - lastVisitors == 1) {
                updateSixthVisitorArrivalTime()
                visitorJustArrived = false
                sixthVisitorReady = false
            }
            millis = sixthVisitorArrivalTime.timeUntil()

            val nextSixthVisitorArrival = SimpleTimeMark.now() + millis + (visitorInterval * (5 - visitorsAmount))
            GardenAPI.storage?.nextSixthVisitorArrival = nextSixthVisitorArrival
            if (isSixthVisitorEnabled() && millis.isNegative()) {
                visitorsAmount++
                if (!sixthVisitorReady) {
                    sixthVisitorReady = true
                    if (isSixthVisitorWarningEnabled()) {
                        LorenzUtils.sendTitle("§a6th Visitor Ready", 5.seconds)
                        SoundUtils.playBeepSound()
                    }
                }
            }
        }
        val sinceLastTimerUpdate = lastTimerUpdate.passedSince() - 100.milliseconds
        val guessTime = visitorsAmount < 5 && sinceLastTimerUpdate in 500.milliseconds..60.seconds
        if (guessTime) {
            millis -= sinceLastTimerUpdate
        }

        if (lastMillis == Duration.INFINITE) {
            ErrorManager.logErrorStateWithData(
                "Found Visitor Timer bug, reset value", "lastMillis was infinite",
                "lastMillis" to lastMillis,
            )
            lastMillis = 0.seconds
        }

        val diff = lastMillis - millis
        if (diff == 0.seconds && visitorsAmount == lastVisitors) return
        lastMillis = millis
        lastVisitors = visitorsAmount

        val formatColor = when {
            queueFull -> "6"
            else -> "e"
        }

        val adjustedMillis = if (GardenAPI.isCurrentlyFarming()) millis / 3 else millis
        val extraSpeed = if (GardenAPI.isCurrentlyFarming()) {
            val duration = adjustedMillis * (GardenCropSpeed.getRecentBPS() / 20)
            "§7/§$formatColor" + duration.format()
        } else ""
        if (config.newVisitorPing && adjustedMillis < 10.seconds) {
            SoundUtils.playBeepSound()
        }

        val formatDuration = millis.format()
        val next = if (queueFull && (!isSixthVisitorEnabled() || millis.isNegative())) "§cQueue Full!" else {
            "Next in §$formatColor$formatDuration$extraSpeed"
        }
        val visitorLabel = if (visitorsAmount == 1) "visitor" else "visitors"
        display = createDisplayText("§b$visitorsAmount $visitorLabel §7($next§7)")
    }

    private fun createDisplayText(text: String) = Renderable.clickAndHover(
        text,
        listOf("§eClick to teleport to the barn!"),
        onClick = { HypixelCommands.teleportToPlot("barn") },
    )

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return

        config.pos.renderRenderable(display, posLabel = "Garden Visitor Timer")
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        lastVisitors = -1
        GardenAPI.storage?.nextSixthVisitorArrival?.let {
            if (it.isFarFuture() && it.toMillis() != -9223370336633802065) {
                sixthVisitorArrivalTime = it
            }
        }
        sixthVisitorReady = false
        lastMillis = sixthVisitorArrivalTime.timeUntil()
    }

    @SubscribeEvent
    fun onCropClick(event: CropClickEvent) {
        if (!isEnabled()) return
        sixthVisitorArrivalTime -= 100.milliseconds

        // We only need manually retracting the time when hypixel shows 6 minutes or above
        if (lastMillis > 5.minutes) {
            lastTimerUpdate -= 100.milliseconds
        }
    }

    private fun updateSixthVisitorArrivalTime() {
        visitorInterval?.let {
            sixthVisitorArrivalTime = SimpleTimeMark.now() + it
        }
    }

    private fun isSixthVisitorEnabled() = config.sixthVisitorEnabled
    private fun isSixthVisitorWarningEnabled() = config.sixthVisitorWarning
    private fun isEnabled() = GardenAPI.inGarden() && config.enabled

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "garden.visitorTimerEnabled", "garden.visitors.timer.enabled")
        event.move(3, "garden.visitorTimerSixthVisitorEnabled", "garden.visitors.timer.sixthVisitorEnabled")
        event.move(3, "garden.visitorTimerSixthVisitorWarning", "garden.visitors.timer.sixthVisitorWarning")
        event.move(3, "garden.visitorTimerPos", "garden.visitors.timer.pos")
    }
}
