package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomLines.handleCustomLine
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.customLineConfig

object ScoreboardElementCustomLine : ScoreboardElement() {
    override fun getDisplay(): Any = customLineConfig.customLine1.handleCustomLine()

    override val configLine: String = "Custom Line 1"
}
