package at.hannibal2.skyhanni.features.garden.pests

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.garden.pests.PestSpawnEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeUtils.format

@SkyHanniModule
object PestSpawnTimer {

    private val config get() = PestApi.config.pestTimer

    var lastSpawnTime = SimpleTimeMark.farPast()

    @HandleEvent
    fun onPestSpawn(event: PestSpawnEvent) {
        lastSpawnTime = SimpleTimeMark.now()
    }

    @HandleEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (config.onlyWithVacuum && !PestApi.hasVacuumInHand()) return

        val display = if (lastSpawnTime.isFarPast()) {
            "§cNo pest spawned since joining."
        } else {
            val timeSinceLastPest = lastSpawnTime.passedSince().format()
            "§eLast pest spawned §b$timeSinceLastPest ago"
        }

        config.position.renderString(display, posLabel = "Pest Spawn Timer")
    }

    fun isEnabled() = GardenAPI.inGarden() && config.enabled
}
