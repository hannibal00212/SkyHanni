package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.data.BitsAPI
import at.hannibal2.skyhanni.data.HypixelData
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.displayConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.informationFilteringConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getBitsLine
import at.hannibal2.skyhanni.utils.LorenzUtils.inAnyIsland

// internal
// update with bits update event
object ScoreboardElementBits : ScoreboardElement() {
    override fun getDisplay(): String? {
        val bitsToClaim = BitsAPI.bitsAvailable
        if (informationFilteringConfig.hideEmptyLines && BitsAPI.bits == 0 && (bitsToClaim == -1 || bitsToClaim == 0)) return null

        return CustomScoreboardUtils.formatScoreboardNumberDisplayDisplay("Bits", getBitsLine(), "§b")
    }

    override fun showWhen() = !HypixelData.bingo

    override val configLine = "Bits: §b59,264"

    override fun showIsland() = !inAnyIsland(IslandType.CATACOMBS, IslandType.KUUDRA_ARENA)
}

// click: open /sbmenu
