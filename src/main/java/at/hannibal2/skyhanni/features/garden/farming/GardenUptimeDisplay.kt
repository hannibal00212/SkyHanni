package at.hannibal2.skyhanni.features.garden.farming

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.features.garden.GardenUptimeConfig.GardenUptimeDisplayText
import at.hannibal2.skyhanni.data.ClickType
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.CropClickEvent
import at.hannibal2.skyhanni.events.DateChangeEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.entity.EntityMoveEvent
import at.hannibal2.skyhanni.events.garden.pests.PestKillEvent
import at.hannibal2.skyhanni.events.garden.visitor.VisitorAcceptedEvent
import at.hannibal2.skyhanni.events.garden.visitor.VisitorOpenEvent
import at.hannibal2.skyhanni.events.garden.visitor.VisitorRefusedEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ConditionalUtils.afterChange
import at.hannibal2.skyhanni.utils.ConditionalUtils.onToggle
import at.hannibal2.skyhanni.utils.LorenzUtils.isAnyOf
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.Searchable
import at.hannibal2.skyhanni.utils.renderables.toSearchable
import at.hannibal2.skyhanni.utils.tracker.SkyHanniTracker
import at.hannibal2.skyhanni.utils.tracker.SkyhanniTimedTracker
import at.hannibal2.skyhanni.utils.tracker.TimedTrackerData
import at.hannibal2.skyhanni.utils.tracker.TrackerData
import com.google.gson.annotations.Expose
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object GardenUptimeDisplay {
    private val config get() = GardenAPI.config.gardenUptime

    private val tracker = SkyhanniTimedTracker<Data>(
        "Garden Uptime Tracker",
        { Data() },
        { it.garden.uptimeTracker },
        { drawDisplay(it) }
    )

    class TimeData : TimedTrackerData<Data>({ Data() }) {
        init {
            if (config.resetSession) {
                getOrPutEntry(SkyHanniTracker.DisplayMode.SESSION).reset()
            }
        }
    }

    class Data : TrackerData() {
        override fun reset() {
            cropBreakTime = 0
            visitorTime = 0
            pestTime = 0
            blocksBroken = 0
        }
        @Expose
        var cropBreakTime: Int = 0

        @Expose
        var visitorTime: Int = 0

        @Expose
        var pestTime: Int = 0

        @Expose
        var blocksBroken: Int = 0

    }

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (isAFK || !isEnabled() || activityType == null) return
        secondsLastMove++
        secondsAFK++

        if (checkAFKTimeout()) {
            markAFK()
        }

        tracker.modify {
            when (activityType) {
                ActivityType.VISITOR -> it.visitorTime++
                ActivityType.PEST -> it.pestTime++
                ActivityType.CROP_BREAK -> if (!justRemovedAFK) it.cropBreakTime++
                null -> {}
            }
        }
        blockBreaksLastSecond = 0
        justRemovedAFK = false
    }

    @HandleEvent
    fun onIslandChange(event: IslandChangeEvent) {
        if (!config.showDisplay) return
        if (event.oldIsland.isAnyOf(IslandType.GARDEN)) markAFK()
        if (event.newIsland.isAnyOf(IslandType.GARDEN)) tracker.update()
    }

    @HandleEvent
    fun onCropBreak(event: CropClickEvent) {
        if (event.clickType != ClickType.LEFT_CLICK || !isEnabled()) return
        if (isAFK) {
            justRemovedAFK = true
        }
        if (!justRemovedAFK) {
            tracker.modify { it.blocksBroken++ }
            blockBreaksLastSecond++
        }
        activityType = ActivityType.CROP_BREAK
        resetAFK()
    }

    @HandleEvent
    fun onPestKill(event: PestKillEvent) {
        if (!isEnabled()) return
        activityType = ActivityType.PEST
        resetAFK()
    }

    @HandleEvent
    fun onVistorOpen(event: VisitorOpenEvent) {
        if (!isEnabled()) return
        activityType = ActivityType.VISITOR
        resetAFK()
    }

    @HandleEvent
    fun onVistorAccepted(event: VisitorAcceptedEvent) {
        if (!isEnabled()) return
        activityType = ActivityType.VISITOR
        resetAFK()
    }

    @HandleEvent
    fun onVistorRefused(event: VisitorRefusedEvent) {
        if (!isEnabled()) return
        tracker.modify { it.visitorTime += secondsAFK }
        activityType = ActivityType.VISITOR
        resetAFK()
    }

    @HandleEvent
    fun onPlayerMove(event: EntityMoveEvent<EntityPlayer>) {
        if (!isEnabled() && event.isLocalPlayer) return
        secondsLastMove = 0
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent) {
        if (!isEnabled()) return
        tracker.renderDisplay(config.pos)
    }

    @HandleEvent
    fun onDateChange(event: DateChangeEvent) {
        tracker.changeDate(event.oldDate, event.newDate)
    }

    @HandleEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        onToggle(
            config.includeVisitors,
            config.includePests
        ) {
            tracker.update()
        }
        config.uptimeDisplayText.afterChange {
            tracker.update()
        }
    }

    private var blockBreaksLastSecond = 0
    var storage = GardenAPI.storage
    private var justRemovedAFK = false
    private var isAFK = false
    private var secondsAFK = 0
    private var secondsLastMove = 0
    private var activityType: ActivityType? = null

    private fun drawDisplay(data: Data): List<Searchable> = buildList {
        val lineMap = mutableMapOf<GardenUptimeDisplayText, Searchable>()
        lineMap[GardenUptimeDisplayText.TITLE] = Renderable.string("§6Garden Uptime").toSearchable()

        lineMap[GardenUptimeDisplayText.DATE] = tracker.buildDate().toSearchable()

        var uptime = data.cropBreakTime
        if (config.includeVisitors.get()) uptime += data.visitorTime
        if (config.includePests.get()) uptime += data.pestTime
        lineMap[GardenUptimeDisplayText.UPTIME] =
            Renderable.string(
                "§7Uptime: §e${if (uptime > 0) uptime.seconds else "§cnone"}${if (isAFK) " §cPaused!" else ""}"
            ).toSearchable()

        var bps = 0.0
        if (uptime > 0) bps =
            (data.blocksBroken.toDouble() - blockBreaksLastSecond) / uptime
        if (bps > 0) {
            lineMap[GardenUptimeDisplayText.BPS] =
                Renderable.string("§7Blocks/Second: §e${bps.roundTo(2)}").toSearchable()
        }

        lineMap[GardenUptimeDisplayText.BLOCKS_BROKEN] =
            Renderable.string("§7Blocks Broken: §e${data.blocksBroken.addSeparators()}").toSearchable()

        return formatDisplay(lineMap)
    }

    private fun markAFK() {
        isAFK = true
        tracker.modify {
            when (activityType) {
                ActivityType.VISITOR -> it.visitorTime -= secondsAFK
                ActivityType.PEST -> it.pestTime -= secondsAFK
                ActivityType.CROP_BREAK -> it.cropBreakTime -= secondsAFK - 1
                null -> {}
            }
        }
        secondsAFK = 0
        secondsLastMove = 0
    }

    private fun formatDisplay(lineMap: MutableMap<GardenUptimeDisplayText, Searchable>): List<Searchable> {
        val newList = mutableListOf<Searchable>()
        newList.addAll(config.uptimeDisplayText.get().mapNotNull { lineMap[it] })
        return newList
    }

    private fun resetAFK() {
        isAFK = false
        secondsAFK = 0
    }

    private fun checkAFKTimeout(): Boolean {
        if (secondsAFK < config.timeout) return false
        if (config.movementTimeout) {
            if (secondsLastMove < config.timeout && secondsAFK < config.movementTimeoutDuration) {
                return false
            }
        }
        return true
    }

    private fun isEnabled() = GardenAPI.inGarden() && config.showDisplay

    enum class ActivityType(val displayName: String) {
        CROP_BREAK("Crop Break"),
        VISITOR("Visitor"),
        PEST("Pest"),
    }


}
