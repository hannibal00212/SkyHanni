package at.hannibal2.skyhanni.features.gui.customscoreboard

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.formatNumber
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

interface CustomScoreboardNumberTrackingElement {
    var previousAmount: Long
    var temporaryChangeDisplay: String?
    val numberColor: String

    fun checkDifference(currentAmount: Long) {
        if (currentAmount != previousAmount) {
            val changeAmount = currentAmount - previousAmount
            showTemporaryChange(changeAmount)
            previousAmount = currentAmount
        }
    }

    private fun showTemporaryChange(changeAmount: Long, durationMillis: Long = 5000) {
        temporaryChangeDisplay = if (changeAmount > 0) {
            " §7($numberColor+${formatNumber(changeAmount)}§7)$numberColor"
        } else {
            " §7($numberColor${formatNumber(changeAmount)}§7)$numberColor"
        }

        SkyHanniMod.coroutineScope.launch {
            delay(durationMillis)
            temporaryChangeDisplay = null
        }
    }
}

