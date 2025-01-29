package at.hannibal2.skyhanni.features.gui.customscoreboard.events

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getSBLines
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern
import at.hannibal2.skyhanni.utils.CollectionUtils.addNotNull
import at.hannibal2.skyhanni.utils.CollectionUtils.nextAfter
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatches

// scoreboard
// scoreboard update event
object ScoreboardEventTrapper : ScoreboardEvent() {
    override fun getDisplay() = buildList {
        addNotNull(ScoreboardPattern.peltsPattern.firstMatches(getSBLines()))
        ScoreboardPattern.mobLocationPattern.firstMatches(getSBLines())?.let {
            add(it)
            addNotNull(getSBLines().nextAfter(it))
        }
    }

    override val configLine = "Pelts: §5711\nTracker Mob Location:\n§bMushroom Gorge"

    override val elementPatterns = listOf(
        ScoreboardPattern.peltsPattern,
        ScoreboardPattern.mobLocationPattern,
    )

    override fun showIsland() = IslandType.THE_FARMING_ISLANDS.isInIsland()
}
