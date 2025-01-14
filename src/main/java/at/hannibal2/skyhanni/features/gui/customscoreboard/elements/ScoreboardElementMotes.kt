package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.displayConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.informationFilteringConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.formatStringNum
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getMotes
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern
import at.hannibal2.skyhanni.features.rift.RiftAPI

// scoreboard
// scoreboard update event
object ScoreboardElementMotes : ScoreboardElement() {
    override fun getDisplay(): String? {
        val motes = formatStringNum(getMotes())

        return when {
            informationFilteringConfig.hideEmptyLines && motes == "0" -> null
            displayConfig.displayNumbersFirst -> "§d$motes Motes"
            else -> "Motes: §d$motes"
        }
    }

    override val configLine = "Motes: §d64,647"

    override val elementPatterns = listOf(ScoreboardPattern.motesPattern)

    override fun showIsland() = RiftAPI.inRift()
}
