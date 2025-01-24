package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern
import at.hannibal2.skyhanni.utils.SkyBlockTime
import at.hannibal2.skyhanni.utils.TimeUtils.formatted

// internal
// set timer (could be 1s or more)
object ScoreboardElementDate : ScoreboardElement() {
    override fun getDisplay() = SkyBlockTime.now().formatted(yearElement = false, hoursAndMinutesElement = false)

    override val configLine = "Late Summer 11th"

    override val elementPatterns = listOf(ScoreboardPattern.datePattern)
}
