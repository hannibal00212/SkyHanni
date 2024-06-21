package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.data.WinterAPI
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.displayConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.informationFilteringConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.formatStringNum
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getNorthStars
import at.hannibal2.skyhanni.features.gui.customscoreboard.HIDDEN

object NorthStars : Element() {
    override fun getDisplay(): List<Any> {
        val northStars = formatStringNum(getNorthStars())

        return listOf(
            when {
                informationFilteringConfig.hideEmptyLines && northStars == "0" -> HIDDEN
                displayConfig.displayNumbersFirst -> "§d$northStars North Stars"
                else -> "North Stars: §d$northStars"
            },
        )
    }

    override fun showWhen() = WinterAPI.inWorkshop()

    override val configLine = "North Stars: §d756"
}
