package at.hannibal2.skyhanni.features.gui.customscoreboard.replacements

import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getBits

object ScoreboardReplacementBits : ScoreboardReplacement() {
    override val trigger: String = "%bits%"
    override val name: String = "Bits"
    override fun replacement(): String = getBits()
}
