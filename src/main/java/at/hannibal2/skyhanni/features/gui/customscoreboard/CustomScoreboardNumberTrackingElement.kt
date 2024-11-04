package at.hannibal2.skyhanni.features.gui.customscoreboard

import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.formatNumber
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import kotlin.time.Duration.Companion.seconds

interface CustomScoreboardNumberTrackingElement {
    var previousValue: Long
    val numberColor: String
    var lastUpdateTime: SimpleTimeMark

    fun calculateAndFormatDifference(currentValue: Long): String {
        val difference = currentValue - previousValue
        if (!CustomScoreboard.displayConfig.showNumberDifference) return ""

        if (lastUpdateTime.passedSince() > 5.seconds) return ""

        return when {
            difference > 0 -> " §7(+$numberColor${formatNumber(difference)}§7)"
            difference < 0 -> " §7($numberColor${formatNumber(difference)}§7)"
            else -> ""
        }
    }

    fun updatePreviousValue(currentValue: Long) {
        if (lastUpdateTime.passedSince() < 5.seconds) return

        if (currentValue != previousValue) {
            lastUpdateTime = SimpleTimeMark.now()
            previousValue = currentValue
        }
    }
}

