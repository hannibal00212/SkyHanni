package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.displayConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getTimeSymbol
import at.hannibal2.skyhanni.utils.SkyBlockTime
import at.hannibal2.skyhanni.utils.TimeUtils.formatted

// internal
// 1s update
object ScoreboardElementTime : ScoreboardElement() {
    override fun getDisplay(): String {
        val time = SkyBlockTime.now()
            .formatted(
                dayAndMonthElement = false,
                yearElement = false,
                timeFormat24h = displayConfig.skyblockTime24hFormat,
                exactMinutes = displayConfig.skyblockTimeExactMinutes,
            )
        return "§7$time ${getTimeSymbol()}".trim()
    }

    override val configLine = "§710:40pm §b☽"
}
