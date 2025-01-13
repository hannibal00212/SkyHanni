package at.hannibal2.skyhanni.features.gui.customscoreboard.replacements

import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getBitsAvailable

object ScoreboardReplacementBitsAvailable : ScoreboardReplacement() {
    override val trigger: String = "%bits_available%"
    override val name: String = "Bits Available"
    override fun replacement(): String = getBitsAvailable()
}
