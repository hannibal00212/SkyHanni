package at.hannibal2.skyhanni.features.gui.customscoreboard.events

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getSbLines
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatches

object Redstone : Event() {
    override fun getDisplay() = listOfNotNull(ScoreboardPattern.redstonePattern.firstMatches(getSbLines()))

    override fun showWhen() = IslandType.PRIVATE_ISLAND.isInIsland()

    override val configLine = "§e§l⚡ §cRedstone: §e§b7%"
}
