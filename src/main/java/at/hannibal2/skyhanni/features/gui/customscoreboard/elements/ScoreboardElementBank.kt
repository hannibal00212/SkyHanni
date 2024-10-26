package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.informationFilteringConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getBank
import at.hannibal2.skyhanni.features.rift.RiftAPI

// widget
// update with widget update event
object ScoreboardElementBank : ScoreboardElement() {
    override fun getDisplay(): String? {
        val bank = getBank()
        if (informationFilteringConfig.hideEmptyLines && (bank == "0" || bank == "0§7 / §60")) return null

        return CustomScoreboardUtils.formatScoreboardNumberDisplayDisplay("Bank", bank, "§6")
    }

    override val configLine = "Bank: §6249M"

    override fun showIsland() = !RiftAPI.inRift()
}

// click: open /bank (does that even exist?)
