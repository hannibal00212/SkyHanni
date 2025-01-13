package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.informationFilteringConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getGems
import at.hannibal2.skyhanni.utils.LorenzUtils.inAnyIsland

// widget
// widget update event
object ScoreboardElementGems : ScoreboardElement() {
    override fun getDisplay(): String? {
        val gems = getGems()
        if (informationFilteringConfig.hideEmptyLines && gems == "0") return null

        return CustomScoreboardUtils.formatScoreboardNumberDisplayDisplay("Gems", gems, "§a")
    }

    override val configLine = "Gems: §a57,873"

    override fun showIsland() = !inAnyIsland(IslandType.THE_RIFT, IslandType.CATACOMBS, IslandType.KUUDRA_ARENA)
}
