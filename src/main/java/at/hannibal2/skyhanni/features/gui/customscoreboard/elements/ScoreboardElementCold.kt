package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.data.MiningApi
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.displayConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.informationFilteringConfig

// internal
// update with cold update event
object ScoreboardElementCold : ScoreboardElement() {
    override fun getDisplay(): String? {
        val cold = -MiningApi.cold

        return when {
            informationFilteringConfig.hideEmptyLines && cold == 0 -> null
            displayConfig.displayNumbersFirst -> "§b$cold❄ Cold"
            else -> "Cold: §b$cold❄"
        }
    }

    override val configLine = "Cold: §b0❄"

    override val elementPatterns = listOf(MiningApi.coldPattern)

    override fun showIsland() = MiningApi.inColdIsland()
}

// click: warp basecamp
