package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.data.WinterAPI
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.displayConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.informationFilteringConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.formatStringNum
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getNorthStars
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern

// scoreboard
// scoreboard update event
object ScoreboardElementNorthStars : ScoreboardElement() {
    override fun getDisplay(): String? {
        val northStars = formatStringNum(getNorthStars())

        return when {
            informationFilteringConfig.hideEmptyLines && northStars == "0" -> null
            displayConfig.displayNumbersFirst -> "§d$northStars North Stars"
            else -> "North Stars: §d$northStars"
        }
    }

    override val configLine = "North Stars: §d756"

    override val elementPatterns = listOf(ScoreboardPattern.northstarsPattern)

    override fun showIsland() = WinterAPI.inWorkshop()
}
