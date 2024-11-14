package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getSbLines
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern
import at.hannibal2.skyhanni.utils.LorenzUtils.inAnyIsland
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatches

// scoreboard
// scoreboard update event
object ScoreboardElementVisiting : ScoreboardElement() {
    override fun getDisplay() = ScoreboardPattern.visitingPattern.firstMatches(getSbLines())

    override val configLine = " §a✌ §7(§a1§7/6)"

    override val elementPatterns = listOf(ScoreboardPattern.visitingPattern)

    override fun showIsland() = inAnyIsland(
        IslandType.PRIVATE_ISLAND, IslandType.PRIVATE_ISLAND_GUEST,
        IslandType.GARDEN, IslandType.GARDEN_GUEST,
    )
}
