package at.hannibal2.skyhanni.features.gui.customscoreboard.replacements

import at.hannibal2.skyhanni.utils.LocationUtils.calculatePlayerYaw
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo

object ScoreboardReplacementYaw : ScoreboardReplacement() {
    override val trigger = "%yaw%"
    override val name = "Direction"
    override fun replacement(): String = calculatePlayerYaw().roundTo(2).toString()
}
