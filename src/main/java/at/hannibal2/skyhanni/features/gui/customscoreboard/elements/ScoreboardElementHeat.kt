package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.data.MiningApi
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.informationFilteringConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getHeat
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern

// scoreboard
// scoreboard update event
object ScoreboardElementHeat : ScoreboardElement() {
    override fun getDisplay(): String? {
        val heat = getHeat() ?: return null
        if (informationFilteringConfig.hideEmptyLines && heat == "§c♨ 0") return null

        return CustomScoreboardUtils.formatScoreboardNumberDisplayDisplay("Heat", heat, "§c")
    }

    override val configLine = "Heat: §c♨ 14"

    override val elementPatterns = listOf(ScoreboardPattern.heatPattern)

    override fun showIsland() = MiningApi.inCrystalHollows()
}
