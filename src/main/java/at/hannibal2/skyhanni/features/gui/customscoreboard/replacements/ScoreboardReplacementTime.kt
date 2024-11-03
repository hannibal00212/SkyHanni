package at.hannibal2.skyhanni.features.gui.customscoreboard.replacements

import at.hannibal2.skyhanni.utils.SkyBlockTime
import at.hannibal2.skyhanni.utils.TimeUtils.formatted

object ScoreboardReplacementTime : ScoreboardReplacement() {
    override val trigger = "%time%"
    override val name = "Time"
    override fun replacement(): String = SkyBlockTime.now().formatted(dayAndMonthElement = false, yearElement = false)
}
