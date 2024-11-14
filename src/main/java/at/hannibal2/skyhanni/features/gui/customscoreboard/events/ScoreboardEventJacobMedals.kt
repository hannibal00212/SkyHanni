package at.hannibal2.skyhanni.features.gui.customscoreboard.events

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getSbLines
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern
import at.hannibal2.skyhanni.utils.LorenzUtils.inAnyIsland
import at.hannibal2.skyhanni.utils.RegexUtils.allMatches

// scoreboard
// scoreboard update event
object ScoreboardEventJacobMedals : ScoreboardEvent() {
    override fun getDisplay() = ScoreboardPattern.medalsPattern.allMatches(getSbLines())

    override val configLine = "§6§lGOLD §fmedals: §613\n§f§lSILVER §fmedals: §f3\n§c§lBRONZE §fmedals: §c4"

    override val elementPatterns = listOf(ScoreboardPattern.medalsPattern)

    override fun showIsland() = inAnyIsland(IslandType.GARDEN, IslandType.HUB)
}
