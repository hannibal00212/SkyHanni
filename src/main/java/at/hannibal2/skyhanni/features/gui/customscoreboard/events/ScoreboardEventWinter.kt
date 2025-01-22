package at.hannibal2.skyhanni.features.gui.customscoreboard.events

import at.hannibal2.skyhanni.data.WinterAPI
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getSbLines
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern
import at.hannibal2.skyhanni.utils.RegexUtils.allMatches

// scoreboard
// scoreboard update event
object ScoreboardEventWinter : ScoreboardEvent() {

    override fun getDisplay() = elementPatterns.allMatches(getSbLines()).filter { !it.endsWith("Soon!") }

    override val configLine = "§7(All Winter Event Lines)"

    override val elementPatterns = listOf(
        ScoreboardPattern.winterEventStartPattern,
        ScoreboardPattern.winterNextWavePattern,
        ScoreboardPattern.winterWavePattern,
        ScoreboardPattern.winterMagmaLeftPattern,
        ScoreboardPattern.winterTotalDmgPattern,
        ScoreboardPattern.winterCubeDmgPattern,
    )

    override fun showIsland() = WinterAPI.inWorkshop()
}
