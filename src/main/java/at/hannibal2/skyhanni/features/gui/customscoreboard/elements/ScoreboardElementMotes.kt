package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.informationFilteringConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.formatStringNum
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getMotes
import at.hannibal2.skyhanni.features.rift.RiftAPI

// scoreboard
// scoreboard update event
object ScoreboardElementMotes : ScoreboardElement() {
    override fun getDisplay(): String? {
        val motes = formatStringNum(getMotes())
        if (informationFilteringConfig.hideEmptyLines && motes == "0") return null

        return CustomScoreboardUtils.formatScoreboardNumberDisplayDisplay("Motes", motes, "§d")
    }

    override val configLine = "Motes: §d64,647"

    override fun showIsland() = RiftAPI.inRift()
}
