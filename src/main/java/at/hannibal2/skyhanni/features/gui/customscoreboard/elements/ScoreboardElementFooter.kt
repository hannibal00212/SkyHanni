package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.data.HypixelData
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.displayConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardLine.Companion.align
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern

// internal
// update on config load
object ScoreboardElementFooter : ScoreboardElement() {
    override fun getDisplay() = when (HypixelData.hypixelAlpha) {
        true -> displayConfig.titleAndFooter.customAlphaFooter
        false -> displayConfig.titleAndFooter.customFooter
    }.replace("&&", "§")
        .split("\\n")
        .map { it align displayConfig.titleAndFooter.alignFooter }

    override val configLine = "§ewww.hypixel.net"

    override val elementPatterns = listOf(ScoreboardPattern.footerPattern)
}
