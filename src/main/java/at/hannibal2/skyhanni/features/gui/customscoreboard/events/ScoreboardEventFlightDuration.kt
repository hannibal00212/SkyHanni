package at.hannibal2.skyhanni.features.gui.customscoreboard.events

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getSBLines
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern
import at.hannibal2.skyhanni.utils.LorenzUtils.inAnyIsland
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatches

// scoreboard
// scoreboard update event
object ScoreboardEventFlightDuration : ScoreboardEvent() {
    override fun getDisplay() = ScoreboardPattern.flightDurationPattern.firstMatches(getSBLines())?.trim()

    override val configLine = "Flight Duration: §a10m 0s"

    override val elementPatterns = listOf(ScoreboardPattern.flightDurationPattern)

    override fun showIsland() = inAnyIsland(
        IslandType.PRIVATE_ISLAND,
        IslandType.PRIVATE_ISLAND_GUEST,
        IslandType.GARDEN,
        IslandType.GARDEN_GUEST,
    )
}
