package at.hannibal2.skyhanni.features.garden.farming.lane

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.garden.farming.CropClickEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.garden.GardenApi
import at.hannibal2.skyhanni.features.garden.farming.lane.FarmingLaneApi.getValue
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.SkyHanniVec3d
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

@SkyHanniModule
object FarmingLaneCreator {
    val config get() = FarmingLaneApi.config

    var detection = false
    private var start: SkyHanniVec3d? = null
    private var lastLocation: SkyHanniVec3d? = null
    private var potentialEnd: SkyHanniVec3d? = null
    private var crop: CropType? = null
    private var maxDistance = 0.0

    fun commandLaneDetection() {
        detection = !detection
        if (detection) {
            ChatUtils.chat("Enabled lane detection. Farm two layers to detect the lane border position.")
        } else {
            ChatUtils.chat("Stopped lane detection.")
        }
    }

    @HandleEvent
    fun onCropClick(event: CropClickEvent) {
        if (!isEnabled()) return

        val location = LocationUtils.playerLocation()

        val lastLocation = lastLocation ?: run {
            start = location
            maxDistance = 0.0
            lastLocation = location
            crop = event.crop
            return
        }

        if (crop != event.crop) {
            ChatUtils.chat("Different crop broken, stopping lane detection")
            reset()
            return
        }
        if (lastLocation.distance(location) < 0.5) return

        this.lastLocation = location
        val start = start ?: error("start can not be null")
        val distance = start.distance(location)
        if (distance > maxDistance) {
            maxDistance = distance
            potentialEnd = null
        } else {
            val potentialEnd = potentialEnd ?: run {
                potentialEnd = location
                return
            }
            if (potentialEnd.distance(location) > 2) {
                val crop = crop ?: error("crop can not be null")
                saveLane(start, potentialEnd, crop)
            }
        }
    }

    private fun saveLane(a: SkyHanniVec3d, b: SkyHanniVec3d, crop: CropType) {
        val lane = createLane(a, b)
        val lanes = FarmingLaneApi.lanes ?: return
        lanes[crop] = lane
        FarmingLaneApi.currentLane = lane
        ChatUtils.chat("${crop.cropName} lane saved! Farming Lane features are now working.")
        reset()
    }

    private fun createLane(a: SkyHanniVec3d, b: SkyHanniVec3d): FarmingLane {
        val diffX = a.x - b.x
        val diffZ = a.z - b.z
        val direction =
            if (diffZ.absoluteValue > diffX.absoluteValue) FarmingDirection.NORTH_SOUTH else FarmingDirection.EAST_WEST

        val min = min(direction.getValue(a), direction.getValue(b))
        val max = max(direction.getValue(a), direction.getValue(b))

        return FarmingLane(direction, min, max)
    }

    private fun reset() {
        start = null
        lastLocation = null
        crop = null
        maxDistance = 0.0
        detection = false
    }

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!isEnabled()) return

        start?.let {
            event.drawWaypointFilled(it, LorenzColor.WHITE.toColor(), beacon = true)
            event.drawDynamicText(it, "start", 1.5)
        }
        lastLocation?.let {
            event.drawWaypointFilled(it, LorenzColor.WHITE.toColor(), beacon = true)
            event.drawDynamicText(it, "lastLocation", 1.5)
        }
        potentialEnd?.let {
            event.drawWaypointFilled(it, LorenzColor.WHITE.toColor(), beacon = true)
            event.drawDynamicText(it, "potentialEnd", 1.5)
        }
    }

    private fun isEnabled() = GardenApi.inGarden() && detection
}
