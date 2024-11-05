package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.data.BitsAPI
import at.hannibal2.skyhanni.data.HypixelData
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.PurseAPI
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.displayConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.informationFilteringConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardNumberTrackingElement
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getBitsLine
import at.hannibal2.skyhanni.utils.LorenzUtils.inAnyIsland

// internal
// update with bits update event
object ScoreboardElementBits : ScoreboardElement(), CustomScoreboardNumberTrackingElement {
    override var previousAmount: Long = BitsAPI.bits.toLong()
    override var temporaryChangeDisplay: String? = null
    override val numberColor = "§b"

    override fun getDisplay(): String? {
        val bits = BitsAPI.bits.toLong()
        val bitsToClaim = BitsAPI.bitsAvailable
        checkDifference(bits)
        val line = getBitsLine() + (temporaryChangeDisplay ?: "")

        return when {
            informationFilteringConfig.hideEmptyLines && bits == 0L && (bitsToClaim == -1 || bitsToClaim == 0) -> null
            displayConfig.displayNumbersFirst -> "$line Bits"
            else -> "Bits: $line"
        }
    }

    override fun showWhen() = !HypixelData.bingo

    override val configLine = "Bits: §b59,264"

    override fun showIsland() = !inAnyIsland(IslandType.CATACOMBS, IslandType.KUUDRA_ARENA)
}

// click: open /sbmenu
