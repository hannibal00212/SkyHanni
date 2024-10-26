package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.data.MiningAPI
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.displayConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.informationFilteringConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils

// internal
// update with cold update event
object ScoreboardElementCold : ScoreboardElement() {
    override fun getDisplay(): String? {
        val cold = -MiningAPI.cold
        if (informationFilteringConfig.hideEmptyLines && cold == 0) return null

        return CustomScoreboardUtils.formatScoreboardNumberDisplayDisplay("Cold", "$cold❄", "§b")
    }

    override val configLine = "Cold: §b0❄"

    override fun showIsland() = MiningAPI.inColdIsland()
}

// click: warp basecamp
