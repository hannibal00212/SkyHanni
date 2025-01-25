package at.hannibal2.skyhanni.features.garden.farming.lane

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GardenToolChangeEvent
import at.hannibal2.skyhanni.events.garden.farming.CropClickEvent
import at.hannibal2.skyhanni.events.garden.farming.FarmingLaneSwitchEvent
import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.garden.GardenApi
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object FarmingLaneApi {
    val config get() = GardenApi.config.farmingLane

    val lanes get() = GardenApi.storage?.farmingLanes
    var currentLane: FarmingLane? = null
    private var lastNoLaneWarning = SimpleTimeMark.farPast()

    @HandleEvent
    fun onGardenToolChange(event: GardenToolChangeEvent) {
        currentLane = null
    }

    @HandleEvent
    fun onCropClick(event: CropClickEvent) {
        val crop = event.crop
        if (!GardenApi.hasFarmingToolInHand()) return

        val lanes = lanes ?: return
        val lane = lanes[crop]
        if (lane == null) {
            warnNoLane(crop)
        }

        if (currentLane == lane) return
        currentLane = lane
        FarmingLaneSwitchEvent(lane).post()
    }

    private fun warnNoLane(crop: CropType?) {
        if (crop == null || currentLane != null) return
        if (crop in config.ignoredCrops) return
        if (!GardenApi.hasFarmingToolInHand()) return
        if (FarmingLaneCreator.detection) return
        if (!config.distanceDisplay && !config.laneSwitchNotification.enabled) return

        if (lastNoLaneWarning.passedSince() < 30.seconds) return
        lastNoLaneWarning = SimpleTimeMark.now()

        ChatUtils.clickableChat(
            "No ${crop.cropName} lane defined yet! Use §e/shlanedetection",
            onClick = { FarmingLaneCreator.commandLaneDetection() },
            "§eClick to run /shlanedetection!",
        )
    }

    fun FarmingDirection.getValue(location: LorenzVec): Double = when (this) {
        FarmingDirection.NORTH_SOUTH -> location.z
        FarmingDirection.EAST_WEST -> location.x
    }

    fun FarmingDirection.setValue(location: LorenzVec, value: Double): LorenzVec = when (this) {
        FarmingDirection.NORTH_SOUTH -> location.copy(z = value)
        FarmingDirection.EAST_WEST -> location.copy(x = value)
    }
}
