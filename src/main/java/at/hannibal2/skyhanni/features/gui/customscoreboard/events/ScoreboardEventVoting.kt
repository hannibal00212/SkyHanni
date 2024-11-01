package at.hannibal2.skyhanni.features.gui.customscoreboard.events

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getSbLines
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.RegexUtils.allMatches

// scoreboard
// scoreboard update event
object ScoreboardEventVoting : ScoreboardEvent() {

    private val patterns = listOf(
        ScoreboardPattern.yearVotesPattern,
        ScoreboardPattern.votesPattern,
        ScoreboardPattern.waitingForVotePattern,
    )

    override fun getDisplay() = patterns.allMatches(getSbLines())

    // TODO: add area check

    override val configLine = "§7(All Voting Lines)"

    override fun showIsland() = IslandType.HUB.isInIsland()
}
