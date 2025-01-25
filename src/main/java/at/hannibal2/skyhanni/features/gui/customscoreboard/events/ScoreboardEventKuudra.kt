package at.hannibal2.skyhanni.features.gui.customscoreboard.events

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getSBLines
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.RegexUtils.allMatches

// scoreboard
// scoreboard update event
object ScoreboardEventKuudra : ScoreboardEvent() {

    override fun getDisplay() = elementPatterns.allMatches(getSBLines())

    override val configLine = "§7(All Kuudra Lines)"

    override val elementPatterns = listOf(
        ScoreboardPattern.autoClosingPattern,
        ScoreboardPattern.startingInPattern,
        ScoreboardPattern.timeElapsedPattern,
        ScoreboardPattern.instanceShutdownPattern,
        ScoreboardPattern.wavePattern,
        ScoreboardPattern.tokensPattern,
        ScoreboardPattern.submergesPattern,
    )

    override fun showIsland() = IslandType.KUUDRA_ARENA.isInIsland()
}
