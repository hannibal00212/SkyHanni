package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.data.MiningAPI
import at.hannibal2.skyhanni.data.ScoreboardData
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.displayConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.informationFilteringConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.HIDDEN
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern
import at.hannibal2.skyhanni.utils.RegexUtils.anyMatches

object Cold : ScoreboardElement() {
    override fun getDisplay(): List<Any> {
        val cold = -MiningAPI.cold

        return listOf(
            when {
                informationFilteringConfig.hideEmptyLines && cold == 0 -> HIDDEN
                displayConfig.displayNumbersFirst -> "§b$cold❄ Cold"
                else -> "Cold: §b$cold❄"
            },
        )
    }

    override fun showWhen() = ScoreboardPattern.coldPattern.anyMatches(ScoreboardData.sidebarLinesFormatted)

    override val configLine = "Cold: §b0❄"

    override fun showIsland() = MiningAPI.inColdIsland()
}
