package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.displayConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.informationFilteringConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getBank
import at.hannibal2.skyhanni.features.rift.RiftAPI

// widget
// update with widget update event
object ScoreboardElementBank : ScoreboardElement() {
    override fun getDisplay(): String? {
        val bank = getBank()

        return when {
            informationFilteringConfig.hideEmptyLines && (bank == "0" || bank == "0§7 / §60") -> null
            displayConfig.displayNumbersFirst -> "§6$bank Bank"
            else -> "Bank: §6$bank"
        }
    }

    override val configLine = "Bank: §6249M"

    override fun showIsland() = !RiftAPI.inRift()
}

// click: open /bank (does that even exist?)
