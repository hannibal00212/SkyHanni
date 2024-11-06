package at.hannibal2.skyhanni.features.gui.customscoreboard.events

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getSbLines
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern
import at.hannibal2.skyhanni.utils.CollectionUtils.addNotNull
import at.hannibal2.skyhanni.utils.CollectionUtils.nextAfter
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatches

// scoreboard
// scoreboard update event
object ScoreboardEventTrapper : ScoreboardEvent() {
    override fun getDisplay() = buildList {
        addNotNull(ScoreboardPattern.peltsPattern.firstMatches(getSbLines()))
        ScoreboardPattern.mobLocationPattern.firstMatches(getSbLines())?.let {
            add(it)
            addNotNull(getSbLines().nextAfter(it))
        }
    }

    override val configLine = "Pelts: §5711\nTracker Mob Location:\n§bMushroom Gorge"

    override fun showIsland() = IslandType.THE_FARMING_ISLANDS.isInIsland()
}
