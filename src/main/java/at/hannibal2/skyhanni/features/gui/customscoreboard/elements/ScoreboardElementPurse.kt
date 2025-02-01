package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.data.PurseApi
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.displayConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.informationFilteringConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.formatNumber
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getPurseEarned
import at.hannibal2.skyhanni.features.rift.RiftApi

// internal
// purse change event (add total purse to event)
object ScoreboardElementPurse : ScoreboardElement() {
    override fun getDisplay(): String? {
        var purse = formatNumber(PurseApi.currentPurse)
        if (informationFilteringConfig.hideEmptyLines && purse == "0") return null

        if (!displayConfig.hideCoinsDifference) {
            purse += getPurseEarned().orEmpty()
        }

        return CustomScoreboardUtils.formatScoreboardNumberDisplayDisplay("Purse", purse, "§6")
    }

    override val configLine = "Purse: §652,763,737"

    override val elementPatterns = listOf(PurseApi.coinsPattern)

    override fun showIsland() = !RiftApi.inRift()
}
