package at.hannibal2.skyhanni.features.gui.customscoreboard.replacements

import at.hannibal2.skyhanni.utils.SkyBlockTime
import at.hannibal2.skyhanni.utils.TimeUtils.formatted

object ScoreboardReplacementDate : ScoreboardReplacement() {
    override val trigger = "%date%"
    override val name = "Date"
    override fun replacement(): String = SkyBlockTime.now().formatted(hoursAndMinutesElement = false, yearElement = false)
}
