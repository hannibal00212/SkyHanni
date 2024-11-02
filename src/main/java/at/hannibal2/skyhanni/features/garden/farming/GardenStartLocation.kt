package at.hannibal2.skyhanni.features.garden.farming

import at.hannibal2.skyhanni.config.features.garden.CropStartLocationConfig.CropLocationMode
import at.hannibal2.skyhanni.data.ClickType
import at.hannibal2.skyhanni.events.CropClickEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceSqToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object GardenStartLocation {

    private val config get() = GardenAPI.config.cropStartLocation
    private var shouldShowLastFarmedWaypoint = false

    fun setLocationCommand() {
        if (!GardenAPI.inGarden()) {
            ChatUtils.userError("This Command only works in the garden!")
            return
        }
        if (!config.enabled) {
            ChatUtils.chatAndOpenConfig(
                "This feature is disabled. Enable it in the config: §e/sh crop start location",
                GardenAPI.config::cropStartLocation
            )
            return
        }

        val startLocations = GardenAPI.storage?.cropStartLocations
        if (startLocations == null) {
            ChatUtils.userError("The config is not yet loaded, retry in a second.")
            return
        }

        val crop = GardenAPI.getCurrentlyFarmedCrop()
        if (crop == null) {
            ChatUtils.userError("Hold a crop specific farming tool in the hand!")
            return
        }

        startLocations[crop] = LocationUtils.playerLocation()
        ChatUtils.chat("You changed your Crop Start Location for ${crop.cropName}!")
    }

    @SubscribeEvent
    fun onCropClick(event: CropClickEvent) {
        if (!isEnabled()) return
        if (event.clickType != ClickType.LEFT_CLICK || !GardenAPI.hasFarmingToolInHand()) return
        val startLocations = GardenAPI.storage?.cropStartLocations ?: return
        val lastFarmedLocations = GardenAPI.storage?.cropLastFarmedLocations ?: return
        val crop = GardenAPI.getCurrentlyFarmedCrop() ?: return
        if (crop != GardenCropSpeed.lastBrokenCrop) return

        if (!startLocations.contains(crop)) {
            startLocations[crop] = LocationUtils.playerLocation()
            ChatUtils.chat("Auto updated your Crop Start Location for ${crop.cropName}")
        }

        lastFarmedLocations[crop] = LocationUtils.playerLocation().roundLocationToBlock()
        shouldShowLastFarmedWaypoint = false
    }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!isEnabled()) return
        val crop = GardenAPI.cropInHand ?: return

        if (showStartWaypoint()) {
            GardenAPI.storage?.cropStartLocations?.get(crop)
                ?.roundLocationToBlock()
                ?.also {
                    event.drawWaypointFilled(it, LorenzColor.WHITE.toColor())
                    event.drawDynamicText(it, "§b${crop.cropName}", 1.5)
                    if (shouldShowBoth()) {
                        event.drawDynamicText(it, "§aStart Location", 1.1, yOff = 12f)
                    }
                }
        }

        if (showLastFarmedWaypoint()) {
            val location = GardenAPI.storage?.cropLastFarmedLocations?.get(crop)
            if (location != null) {
                if (location.distanceSqToPlayer() >= 100.0) {
                    shouldShowLastFarmedWaypoint = true
                }
                if (shouldShowLastFarmedWaypoint) {
                    event.drawWaypointFilled(location, LorenzColor.LIGHT_PURPLE.toColor(), seeThroughBlocks = true, beacon = true)
                    event.drawDynamicText(location, "§b${crop.cropName}", 1.5)
                    if (shouldShowBoth()) {
                        event.drawDynamicText(location, "§eLast Farmed", 1.1, yOff = 12f)
                    }
                }
            }
        }
    }

    private fun shouldShowBoth() = config.mode == CropLocationMode.BOTH
    private fun showStartWaypoint() = config.mode != CropLocationMode.LAST_FARMED
    private fun showLastFarmedWaypoint() = config.mode != CropLocationMode.START

    fun isEnabled() = GardenAPI.inGarden() && config.enabled
}
