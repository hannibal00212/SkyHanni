package at.hannibal2.skyhanni.features.garden.farming

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.config.features.garden.CropStartLocationConfig.CropLocationMode
import at.hannibal2.skyhanni.data.ClickType
import at.hannibal2.skyhanni.events.garden.farming.CropClickEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.features.garden.GardenApi
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceSqToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled

@SkyHanniModule
object GardenStartLocation {

    private val config get() = GardenApi.config.cropStartLocation
    private var shouldShowLastFarmedWaypoint = false

    private fun setLocationCommand() {
        if (!GardenApi.inGarden()) {
            ChatUtils.userError("This Command only works in the garden!")
            return
        }
        if (!config.enabled) {
            ChatUtils.chatAndOpenConfig(
                "This feature is disabled. Enable it in the config: §e/sh crop start location",
                GardenApi.config::cropStartLocation
            )
            return
        }

        val startLocations = GardenApi.storage?.cropStartLocations
        if (startLocations == null) {
            ChatUtils.userError("The config is not yet loaded, retry in a second.")
            return
        }

        val crop = GardenApi.getCurrentlyFarmedCrop()
        if (crop == null) {
            ChatUtils.userError("Hold a crop specific farming tool in the hand!")
            return
        }

        startLocations[crop] = LocationUtils.playerLocation()
        ChatUtils.chat("You changed your Crop Start Location for ${crop.cropName}!")
    }

    @HandleEvent
    fun onCropClick(event: CropClickEvent) {
        if (!isEnabled()) return
        if (event.clickType != ClickType.LEFT_CLICK || !GardenApi.hasFarmingToolInHand()) return
        val startLocations = GardenApi.storage?.cropStartLocations ?: return
        val lastFarmedLocations = GardenApi.storage?.cropLastFarmedLocations ?: return
        val crop = GardenApi.getCurrentlyFarmedCrop() ?: return
        if (crop != GardenCropSpeed.lastBrokenCrop) return

        if (!startLocations.contains(crop)) {
            startLocations[crop] = LocationUtils.playerLocation()
            ChatUtils.chat("Auto updated your Crop Start Location for ${crop.cropName}")
        }

        lastFarmedLocations[crop] = LocationUtils.playerLocation().roundLocationToBlock()
        shouldShowLastFarmedWaypoint = false
    }

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!isEnabled()) return
        val crop = GardenApi.cropInHand ?: return

        if (showStartWaypoint()) {
            GardenApi.storage?.cropStartLocations?.get(crop)
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
            val location = GardenApi.storage?.cropLastFarmedLocations?.get(crop)
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

    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.register("shcropstartlocation") {
            description = "Manually sets the crop start location"
            category = CommandCategory.USERS_ACTIVE
            callback { setLocationCommand() }
        }
    }

    private fun shouldShowBoth() = config.mode == CropLocationMode.BOTH
    private fun showStartWaypoint() = config.mode != CropLocationMode.LAST_FARMED
    private fun showLastFarmedWaypoint() = config.mode != CropLocationMode.START

    fun isEnabled() = GardenApi.inGarden() && config.enabled
}
