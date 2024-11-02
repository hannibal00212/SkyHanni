package at.hannibal2.skyhanni.features.garden.pests

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.features.garden.GardenPlotAPI
import at.hannibal2.skyhanni.features.garden.GardenPlotAPI.currentSpray
import at.hannibal2.skyhanni.features.garden.GardenPlotAPI.isBarn
import at.hannibal2.skyhanni.features.garden.GardenPlotAPI.isSprayExpired
import at.hannibal2.skyhanni.features.garden.GardenPlotAPI.markExpiredSprayAsNotified
import at.hannibal2.skyhanni.features.garden.GardenPlotAPI.name
import at.hannibal2.skyhanni.features.garden.GardenPlotAPI.plots
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.StringUtils.createCommaSeparatedList
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.TimeUtils.timerColor
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object SprayDisplay {

    private val config get() = PestAPI.config.spray
    private var display: String? = null

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!GardenAPI.inGarden() || !event.isMod(5, 3)) return

        if (config.displayEnabled) {
            display = GardenPlotAPI.getCurrentPlot()?.takeIf { !it.isBarn() }?.let { plot ->
                plot.currentSpray?.let {
                    val timer = it.expiry.timeUntil()
                    "§eSprayed with §a${it.type.displayName} §7- ${timer.timerColor("§b")}${timer.format()}"
                } ?: if (config.showNotSprayed) "§cNot sprayed!" else ""
            }.orEmpty()
        }

        if (config.expiryNotification) {
            sendExpiredPlotsToChat(false)
        }
    }

    @SubscribeEvent
    fun onIslandChange(event: IslandChangeEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!config.expiryNotification || event.newIsland != IslandType.GARDEN) return
        sendExpiredPlotsToChat(true)
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!GardenAPI.inGarden() || !config.displayEnabled) return
        val display = display ?: return
        config.displayPosition.renderString(display, posLabel = "Active Plot Spray Display")
    }

    private fun sendExpiredPlotsToChat(wasAway: Boolean) {
        val expiredPlots = plots.filter { it.isSprayExpired }
        if (expiredPlots.isEmpty()) return

        expiredPlots.forEach { it.markExpiredSprayAsNotified() }
        val wasAwayString = if (wasAway) "§7While you were away, your" else "§7Your"
        val plotString = expiredPlots.map { "§b${it.name}" }.createCommaSeparatedList("§7")
        val sprayString = if (expiredPlots.size > 1) "sprays" else "spray"
        val out = "$wasAwayString $sprayString on §aPlot §7- $plotString §7expired."
        ChatUtils.chat(out)
    }
}
