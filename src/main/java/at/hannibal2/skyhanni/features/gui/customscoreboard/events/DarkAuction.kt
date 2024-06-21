package at.hannibal2.skyhanni.features.gui.customscoreboard.events

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getSbLines
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern
import at.hannibal2.skyhanni.utils.CollectionUtils.addNotNull
import at.hannibal2.skyhanni.utils.CollectionUtils.nextAfter
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.RegexUtils.allMatches
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatches

object DarkAuction : Event() {
    override fun getDisplay() = buildList {
        addAll(listOf(ScoreboardPattern.startingInPattern, ScoreboardPattern.timeLeftPattern).allMatches(getSbLines()))

        ScoreboardPattern.darkAuctionCurrentItemPattern.firstMatches(getSbLines())?.let {
            add(it)
            addNotNull(getSbLines().nextAfter(it))
        }
    }

    override fun showWhen() = IslandType.DARK_AUCTION.isInIsland()

    override val configLine = "Time Left: §b11\nCurrent Item:\n §5Travel Scroll to Sirius"
}
