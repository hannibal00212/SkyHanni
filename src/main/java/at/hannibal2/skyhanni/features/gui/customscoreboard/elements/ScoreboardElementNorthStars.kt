package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.data.WinterAPI
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.informationFilteringConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.formatStringNum
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getNorthStars

// scoreboard
// scoreboard update event
object ScoreboardElementNorthStars : ScoreboardElement() {
    override fun getDisplay(): String? {
        val northStars = formatStringNum(getNorthStars())
        if (informationFilteringConfig.hideEmptyLines && northStars == "0") return null

        return CustomScoreboardUtils.formatScoreboardNumberDisplayDisplay("North Stars", northStars, "§d")
    }

    override val configLine = "North Stars: §d756"

    override fun showIsland() = WinterAPI.inWorkshop()
}
