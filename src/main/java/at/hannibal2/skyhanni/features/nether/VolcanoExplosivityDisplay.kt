package at.hannibal2.skyhanni.features.nether

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.model.TabWidget
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.WidgetUpdateEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

@SkyHanniModule
object VolcanoExplosivityDisplay {

    private val config get() = SkyHanniMod.feature.crimsonIsle
    private val patternGroup = RepoPattern.group("crimson.volcano")

    /**
     * REGEX-TEST:  Volcano: §r§8INACTIVE
     */
    private val statusPattern by patternGroup.pattern(
        "tablistline",
        " *Volcano: (?<status>(?:§.)*\\S+)",
    )
    private var display = ""

    @HandleEvent
    fun onTabListUpdate(event: WidgetUpdateEvent) {
        if (!isEnabled()) return
        if (!event.isWidget(TabWidget.VOLCANO)) return

        if (event.isClear()) {
            display = ""
            return
        }
        // TODO merge widget pattern with statusPattern
        statusPattern.matchMatcher(event.lines.first()) {
            display = "§bVolcano Explosivity§7: ${group("status")}"
        }
    }

    @HandleEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        config.positionVolcano.renderString(display, posLabel = "Volcano Explosivity")
    }

    private fun isEnabled() = IslandType.CRIMSON_ISLE.isInIsland() && config.volcanoExplosivity
}
