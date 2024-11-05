package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.displayConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.informationFilteringConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardNumberTrackingElement
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.formatStringNum
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getCopper

// scoreboard
// scoreboard update event
object ScoreboardElementCopper : ScoreboardElement(), CustomScoreboardNumberTrackingElement {
    override var previousAmount: Long = 0
    override var temporaryChangeDisplay: String? = null
    override val numberColor = "§c"

    override fun getDisplay(): String? {
        val copper = getCopper()
        checkDifference(copper.toLong())
        val line = formatStringNum(copper) + (temporaryChangeDisplay ?: "")

        return when {
            informationFilteringConfig.hideEmptyLines && line == "0" -> null
            displayConfig.displayNumbersFirst -> "§c$line Copper"
            else -> "Copper: §c$line"
        }
    }

    override val configLine = "Copper: §c23,495"

    override fun showIsland() = GardenAPI.inGarden()
}

// click: warp barn?
