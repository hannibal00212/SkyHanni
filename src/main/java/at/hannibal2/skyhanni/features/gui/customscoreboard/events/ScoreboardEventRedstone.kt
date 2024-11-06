package at.hannibal2.skyhanni.features.gui.customscoreboard.events

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getSbLines
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatches

// scoreboard
// scoreboard update event
object ScoreboardEventRedstone : ScoreboardEvent() {
    override fun getDisplay() = ScoreboardPattern.redstonePattern.firstMatches(getSbLines())

    override val configLine = "§e§l⚡ §cRedstone: §e§b7%"

    override fun showIsland() = IslandType.PRIVATE_ISLAND.isInIsland()
}
