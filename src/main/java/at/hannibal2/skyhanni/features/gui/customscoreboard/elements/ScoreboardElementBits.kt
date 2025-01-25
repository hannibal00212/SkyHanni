package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.data.BitsApi
import at.hannibal2.skyhanni.data.HypixelData
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.informationFilteringConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getBitsLine
import at.hannibal2.skyhanni.utils.LorenzUtils.inAnyIsland

// internal
// update with bits update event
object ScoreboardElementBits : ScoreboardElement() {
    override fun getDisplay(): String? {
        val bitsToClaim = BitsApi.bitsAvailable
        if (informationFilteringConfig.hideEmptyLines && BitsApi.bits == 0 && (bitsToClaim == -1 || bitsToClaim == 0)) return null

        return CustomScoreboardUtils.formatScoreboardNumberDisplayDisplay("Bits", getBitsLine(), "§b")
    }

    override fun showWhen() = !HypixelData.bingo

    override val configLine = "Bits: §b59,264"

    override val elementPatterns = listOf(BitsApi.bitsScoreboardPattern)

    override fun showIsland() = !inAnyIsland(IslandType.CATACOMBS, IslandType.KUUDRA_ARENA)
}

// click: open /sbmenu
