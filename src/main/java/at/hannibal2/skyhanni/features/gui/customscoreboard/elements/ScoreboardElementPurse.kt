package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.data.PurseAPI
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.displayConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.informationFilteringConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardNumberTrackingElement
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.formatNumber
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getPurseEarned
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.utils.SimpleTimeMark

// internal
// purse change event (add total purse to event)
object ScoreboardElementPurse : ScoreboardElement(), CustomScoreboardNumberTrackingElement {
    override var previousValue: Long = 0L
    override val numberColor = "§6"
    override var lastUpdateTime = SimpleTimeMark.farPast()

    override fun getDisplay(): String? {
        val currentPurse = PurseAPI.currentPurse.toLong()
        val purse = formatNumber(currentPurse) + calculateAndFormatDifference(currentPurse)

        updatePreviousValue(currentPurse)

        return when {
            informationFilteringConfig.hideEmptyLines && purse == "0" -> null
            displayConfig.displayNumbersFirst -> "§6$purse Purse"
            else -> "Purse: §6$purse"
        }
    }

    override val configLine = "Purse: §652,763,737"

    override fun showIsland() = !RiftAPI.inRift()
}
