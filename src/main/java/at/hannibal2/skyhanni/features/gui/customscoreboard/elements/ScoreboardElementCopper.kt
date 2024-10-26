package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.displayConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.informationFilteringConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.formatStringNum
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getCopper

// scoreboard
// scoreboard update event
object ScoreboardElementCopper : ScoreboardElement() {
    override fun getDisplay(): String? {
        val copper = formatStringNum(getCopper())

        return when {
            informationFilteringConfig.hideEmptyLines && copper == "0" -> null
            displayConfig.displayNumbersFirst -> "§c$copper Copper"
            else -> "Copper: §c$copper"
        }
    }

    override val configLine = "Copper: §c23,495"

    override fun showIsland() = GardenAPI.inGarden()
}

// click: warp barn?
