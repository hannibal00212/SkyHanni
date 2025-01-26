package at.hannibal2.skyhanni.features.gui.customscoreboard.events

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getSBLines
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.RegexUtils.allMatches
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatches

// scoreboard
// scoreboard update event
object ScoreboardEventCarnival : ScoreboardEvent() {

    private val patterns = listOf(
        ScoreboardPattern.carnivalTokensPattern,
        ScoreboardPattern.carnivalTasksPattern,
        ScoreboardPattern.timeLeftPattern,
        ScoreboardPattern.carnivalCatchStreakPattern,
        ScoreboardPattern.carnivalFruitsPattern,
        ScoreboardPattern.carnivalAccuracyPattern,
        ScoreboardPattern.carnivalKillsPattern,
        ScoreboardPattern.carnivalScorePattern,
    )

    override fun getDisplay() = buildList {
        ScoreboardPattern.carnivalPattern.firstMatches(getSBLines())?.let {
            add(it)
            addAll(patterns.allMatches(getSBLines()))
        }
    }

    override val configLine = "§7(All Carnival Lines)"

    override val elementPatterns = listOf(ScoreboardPattern.carnivalPattern) + patterns

    override fun showIsland() = IslandType.HUB.isInIsland()
}
