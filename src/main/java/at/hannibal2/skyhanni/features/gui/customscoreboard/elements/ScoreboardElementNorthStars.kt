package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.data.WinterApi
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.informationFilteringConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.formatStringNum
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getNorthStars
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern

// scoreboard
// scoreboard update event
object ScoreboardElementNorthStars : ScoreboardElement() {
    override fun getDisplay(): String? {
        val northStars = formatStringNum(getNorthStars())
        if (informationFilteringConfig.hideEmptyLines && northStars == "0") return null

        return CustomScoreboardUtils.formatScoreboardNumberDisplayDisplay("North Stars", northStars, "§d")
    }

    override val configLine = "North Stars: §d756"

    override val elementPatterns = listOf(ScoreboardPattern.northstarsPattern)

    override fun showIsland() = WinterApi.inWorkshop()
}
