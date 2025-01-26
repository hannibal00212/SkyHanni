package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.data.PurseApi
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.displayConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.informationFilteringConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.formatNumber
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getPurseEarned
import at.hannibal2.skyhanni.features.rift.RiftApi

// internal
// purse change event (add total purse to event)
object ScoreboardElementPurse : ScoreboardElement() {
    override fun getDisplay(): String? {
        var purse = formatNumber(PurseApi.currentPurse)

        if (!displayConfig.hideCoinsDifference) {
            purse += getPurseEarned().orEmpty()
        }

        return when {
            informationFilteringConfig.hideEmptyLines && purse == "0" -> null
            displayConfig.displayNumbersFirst -> "§6$purse Purse"
            else -> "Purse: §6$purse"
        }
    }

    override val configLine = "Purse: §652,763,737"

    override val elementPatterns = listOf(PurseApi.coinsPattern)

    override fun showIsland() = !RiftApi.inRift()
}
