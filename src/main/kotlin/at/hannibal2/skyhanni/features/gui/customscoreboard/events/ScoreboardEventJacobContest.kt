package at.hannibal2.skyhanni.features.gui.customscoreboard.events

import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getSbLines
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern
import at.hannibal2.skyhanni.utils.CollectionUtils.sublistAfter
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatches
import at.hannibal2.skyhanni.utils.RegexUtils.matches

// scoreboard
// scoreboard update event
object ScoreboardEventJacobContest : ScoreboardEvent() {
    // TODO: Use patterns instead of sublistAfter
    override fun getDisplay() = buildList {
        ScoreboardPattern.jacobsContestPattern.firstMatches(getSbLines())?.let { line ->
            add(line)
            addAll(
                getSbLines().sublistAfter(line, amount = 3)
                    .filter { !ScoreboardPattern.footerPattern.matches(it) },
            )
        }
    }

    override val configLine: String = "§eJacob's Contest\n§e○ §fCarrot §a18m17s\n Collected §e8,264"

    override val elementPatterns = listOf(ScoreboardPattern.jacobsContestPattern)
}

