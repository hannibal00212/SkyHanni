package at.hannibal2.skyhanni.features.gui.customscoreboard.events

import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getSbLines
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern
import at.hannibal2.skyhanni.utils.RegexUtils.allMatches

// scoreboard
// scoreboard update event
object ScoreboardEventGarden : ScoreboardEvent() {

    private val patterns = listOf(ScoreboardPattern.pastingPattern, ScoreboardPattern.cleanUpPattern)

    override fun getDisplay() = patterns.allMatches(getSbLines()).map { it.trim() }

    override val configLine = "§7(All Garden Lines)"

    override fun showIsland() = GardenAPI.inGarden()
}
