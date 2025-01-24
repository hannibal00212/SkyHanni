package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.displayConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.informationFilteringConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardNumberTrackingElement
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.formatStringNum
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getCopper
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern
import at.hannibal2.skyhanni.utils.LorenzUtils.inAnyIsland

// scoreboard
// scoreboard update event
object ScoreboardElementCopper : ScoreboardElement(), CustomScoreboardNumberTrackingElement {
    override var previousAmount: Long = 0
    override var temporaryChangeDisplay: String? = null
    override val numberColor = "§c"

    override fun getDisplay(): String? {
        val copper = getCopper()
        checkDifference(copper.toLong())
        val line = formatStringNum(copper) + temporaryChangeDisplay.orEmpty()

        return when {
            informationFilteringConfig.hideEmptyLines && line == "0" -> null
            displayConfig.displayNumbersFirst -> "§c$line Copper"
            else -> "Copper: §c$line"
        }
    }

    override val configLine = "Copper: §c23,495"

    override val elementPatterns = listOf(ScoreboardPattern.copperPattern)

    override fun showIsland() = inAnyIsland(IslandType.GARDEN, IslandType.GARDEN_GUEST)
}

// click: warp barn?
