package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.displayConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.informationFilteringConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardNumberTrackingElement
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.formatStringNum
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getMotes
import at.hannibal2.skyhanni.features.rift.RiftAPI

// scoreboard
// scoreboard update event
object ScoreboardElementMotes : ScoreboardElement(), CustomScoreboardNumberTrackingElement {
    override var previousAmount: Long = 0
    override var temporaryChangeDisplay: String? = null
    override val numberColor = "§d"

    override fun getDisplay(): String? {
        val motes = getMotes()
        checkDifference(motes.toLong())
        val line = formatStringNum(motes) + temporaryChangeDisplay.orEmpty()

        return when {
            informationFilteringConfig.hideEmptyLines && line == "0" -> null
            displayConfig.displayNumbersFirst -> "§d$line Motes"
            else -> "Motes: §d$line"
        }
    }

    override val configLine = "Motes: §d64,647"

    override fun showIsland() = RiftAPI.inRift()
}
