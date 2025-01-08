package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.displayConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.informationFilteringConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getSoulflow
import at.hannibal2.skyhanni.features.rift.RiftAPI

// widget
// widget update event
object ScoreboardElementSoulflow : ScoreboardElement() {
    override fun getDisplay(): String? {
        val soulflow = getSoulflow()
        return when {
            informationFilteringConfig.hideEmptyLines && soulflow == "0" -> null
            displayConfig.displayNumbersFirst -> "§3$soulflow Soulflow"
            else -> "Soulflow: §3$soulflow"
        }
    }

    override val configLine = "Soulflow: §3761"

    override fun showIsland() = !RiftAPI.inRift()
}
