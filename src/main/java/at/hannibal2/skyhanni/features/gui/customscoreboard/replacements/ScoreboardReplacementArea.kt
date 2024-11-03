package at.hannibal2.skyhanni.features.gui.customscoreboard.replacements

import at.hannibal2.skyhanni.data.HypixelData

object ScoreboardReplacementArea : ScoreboardReplacement() {
    override val trigger = "%area%"
    override val name = "Area"
    override fun replacement(): String = HypixelData.skyBlockAreaWithSymbol ?: "N/A"
}
