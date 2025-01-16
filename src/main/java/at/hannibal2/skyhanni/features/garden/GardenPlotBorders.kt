package at.hannibal2.skyhanni.features.garden

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.minecraft.KeyPressEvent
import at.hannibal2.skyhanni.events.minecraft.RenderWorldEvent
import at.hannibal2.skyhanni.features.garden.GardenPlotAPI.renderPlot
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LocationUtils.distanceSqToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import org.lwjgl.input.Keyboard
import kotlin.time.Duration.Companion.milliseconds

@SkyHanniModule
object GardenPlotBorders {

    private val config get() = GardenAPI.config.plotBorders
    private var timeLastSaved = SimpleTimeMark.farPast()
    private var showBorders = false

    @HandleEvent
    fun onKeyPress(event: KeyPressEvent) {
        if (!isEnabled()) return
        if (timeLastSaved.passedSince() < 250.milliseconds) return

        if (event.keyCode == Keyboard.KEY_G && Keyboard.isKeyDown(Keyboard.KEY_F3)) {
            timeLastSaved = SimpleTimeMark.now()
            showBorders = !showBorders
        }
        if (event.keyCode == Keyboard.KEY_F3 && Keyboard.isKeyDown(Keyboard.KEY_G)) {
            timeLastSaved = SimpleTimeMark.now()
            showBorders = !showBorders
        }
    }

    @HandleEvent
    fun onRenderWorld(event: RenderWorldEvent) {
        if (!isEnabled()) return
        if (!showBorders) return
        val plot = GardenPlotAPI.getCurrentPlot() ?: getClosestPlot() ?: return
        event.renderPlot(plot, LorenzColor.YELLOW.toColor(), LorenzColor.DARK_BLUE.toColor(), showBuildLimit = true)
    }

    private fun getClosestPlot(): GardenPlotAPI.Plot? =
        GardenPlotAPI.plots.minByOrNull { it.middle.distanceSqToPlayer() }

    fun isEnabled() = GardenAPI.inGarden() && config
}
