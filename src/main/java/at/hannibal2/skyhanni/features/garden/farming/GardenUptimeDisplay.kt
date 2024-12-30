package at.hannibal2.skyhanni.features.garden.farming

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.features.garden.GardenUptimeConfig.FarmingUptimeDisplayText
import at.hannibal2.skyhanni.data.ClickType
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.CropClickEvent
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
import at.hannibal2.skyhanni.utils.LorenzUtils.isAnyOf
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.Searchable
import at.hannibal2.skyhanni.utils.renderables.toSearchable
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
        { it.garden.timedTracker },
        { drawDisplay(it)}
    )

    class TimeData: TimedTrackerData<Data>({ Data() }){

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

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
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

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent) {
        if (!isEnabled()) return
        tracker.renderDisplay(config.pos)
    }

    @HandleEvent
    fun onPlayerMove(event: EntityMoveEvent<EntityPlayer>) {
        if (!isEnabled()) return
        secondsLastMove = 0
    }

    var blockBreaksLastSecond = 0
    var storage = GardenAPI.storage
    var justRemovedAFK = false
    var isAFK = false
    var secondsAFK = 0
    var secondsLastMove = 0
    var activityType: ActivityType? = null

    private fun drawDisplay(data: Data): List<Searchable> = buildList {
        val lineMap = mutableMapOf<FarmingUptimeDisplayText, Searchable>()
        lineMap[FarmingUptimeDisplayText.TITLE] = Renderable.string("§6Farming Uptime").toSearchable()

        lineMap[FarmingUptimeDisplayText.DATE] = tracker.buildDate().toSearchable()

        var uptime = data.cropBreakTime
        if (config.includeVisitors) uptime += data.visitorTime
        if (config.includePests) uptime += data.pestTime
        lineMap[FarmingUptimeDisplayText.UPTIME] =
            Renderable.string(
                "§7Uptime: §e${if (uptime > 0) uptime.seconds else "§cnone"}${if (isAFK) " §cPaused!" else ""}").toSearchable()

        var bps = 0.0
        if (uptime > 0) bps =
            (data.blocksBroken.toDouble() - blockBreaksLastSecond) / uptime
        if (bps > 0) {
            lineMap[FarmingUptimeDisplayText.BPS] =
                Renderable.string("§7Blocks/Second: §e${bps.roundTo(2)}").toSearchable()
        }

        lineMap[FarmingUptimeDisplayText.BLOCKS_BROKEN] =
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

    private fun formatDisplay(lineMap: MutableMap<FarmingUptimeDisplayText, Searchable>): List<Searchable> {
        val newList = mutableListOf<Searchable>()
        newList.addAll(config.uptimeDisplayText.mapNotNull { lineMap[it] })
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
