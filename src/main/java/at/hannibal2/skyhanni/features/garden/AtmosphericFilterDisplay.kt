package at.hannibal2.skyhanni.features.garden

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.enums.OutsideSBFeature
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.SkyblockSeason

@SkyHanniModule
object AtmosphericFilterDisplay {

    private val config get() = SkyHanniMod.feature.garden.atmosphericFilterDisplay

    private var display = ""

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return
        @Suppress("IsInIslandEarlyReturn")
        if (!GardenApi.inGarden() && !config.outsideGarden) return
        display = drawDisplay(SkyblockSeason.currentSeason ?: return)
    }

    @HandleEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (GardenApi.inGarden()) {
            config.position.renderString(display, posLabel = "Atmospheric Filter Perk Display")
        } else {
            config.positionOutside.renderString(display, posLabel = "Atmospheric Filter Perk Display")
        }
    }

    private fun drawDisplay(season: SkyblockSeason): String = buildString {
        if (!config.onlyBuff) {
            append(season.getSeason(config.abbreviateSeason))
            append("§7: ")
        }
        append(season.getPerk(config.abbreviatePerk))
    }

    private fun isEnabled() = LorenzUtils.onHypixel && config.enabled && (
        (OutsideSBFeature.ATMOSPHERIC_FILTER.isSelected() && !LorenzUtils.inSkyBlock) ||
            (LorenzUtils.inSkyBlock && (GardenApi.inGarden() || config.outsideGarden))
        )
}
