package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.displayConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.informationFilteringConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardNumberTrackingElement
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.formatStringNum
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getSoulflow
import at.hannibal2.skyhanni.features.rift.RiftAPI

// widget
// widget update event
object ScoreboardElementSoulflow : ScoreboardElement(), CustomScoreboardNumberTrackingElement {
    override var previousAmount: Long = 0
    override var temporaryChangeDisplay: String? = null
    override val numberColor = "§3"

    override fun getDisplay(): String? {
        val soulflow = getSoulflow()
        checkDifference(soulflow.toLong())
        val line = formatStringNum(soulflow) + temporaryChangeDisplay.orEmpty()

        return when {
            informationFilteringConfig.hideEmptyLines && line == "0" -> null
            displayConfig.displayNumbersFirst -> "§3$line Soulflow"
            else -> "Soulflow: §3$line"
        }
    }

    override val configLine = "Soulflow: §3761"

    override fun showIsland() = !RiftAPI.inRift()
}
