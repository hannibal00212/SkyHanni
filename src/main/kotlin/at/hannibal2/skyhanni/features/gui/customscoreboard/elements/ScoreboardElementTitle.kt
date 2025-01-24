package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.data.ScoreboardData
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.displayConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardLine.Companion.align
import at.hannibal2.skyhanni.utils.LorenzUtils

// internal and scoreboard
// config change event or scoreboard update event
object ScoreboardElementTitle : ScoreboardElement() {
    override fun getDisplay(): Any {
        val alignment = displayConfig.titleAndFooter.alignTitle

        if (!LorenzUtils.inSkyBlock && !displayConfig.titleAndFooter.useCustomTitleOutsideSkyBlock) {
            return ScoreboardData.objectiveTitle align alignment
        }

        if (!displayConfig.titleAndFooter.useCustomTitle) {
            return ScoreboardData.objectiveTitle align alignment
        }

        return listOf(
            displayConfig.titleAndFooter.customTitle
                .replace("&&", "§")
                .split("\\n")
                .map { it align alignment },
        ).flatten()
    }

    override val configLine = "§6§lSKYBLOCK"
}
