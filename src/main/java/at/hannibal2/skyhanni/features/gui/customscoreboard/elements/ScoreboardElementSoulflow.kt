package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.informationFilteringConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getSoulflow
import at.hannibal2.skyhanni.features.rift.RiftAPI

// widget
// widget update event
object ScoreboardElementSoulflow : ScoreboardElement() {
    override fun getDisplay(): String? {
        val soulflow = getSoulflow()
        if (informationFilteringConfig.hideEmptyLines && soulflow == "0") return null

        return CustomScoreboardUtils.formatScoreboardNumberDisplayDisplay("Soulflow", soulflow, "§3")
    }

    override val configLine = "Soulflow: §3761"

    override fun showIsland() = !RiftAPI.inRift()
}
