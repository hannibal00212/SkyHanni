package at.hannibal2.skyhanni.features.gui.customscoreboard.events

import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getSBLines
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatches

// scoreboard
// scoreboard update event
object ScoreboardEventEssence : ScoreboardEvent() {
    override fun getDisplay() = ScoreboardPattern.essencePattern.firstMatches(getSBLines())

    override val configLine = "Dragon Essence: §d1,285"

    override val elementPatterns = listOf(ScoreboardPattern.essencePattern)
}
