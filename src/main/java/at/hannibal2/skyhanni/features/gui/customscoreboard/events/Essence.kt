package at.hannibal2.skyhanni.features.gui.customscoreboard.events

import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getSbLines
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatches

object Essence : Event() {
    override fun getDisplay() = listOfNotNull(ScoreboardPattern.essencePattern.firstMatches(getSbLines()))

    override fun showWhen() = true

    override val configLine = "Dragon Essence: §d1,285"
}
