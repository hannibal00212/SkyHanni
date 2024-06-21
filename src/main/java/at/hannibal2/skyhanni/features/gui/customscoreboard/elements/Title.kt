package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.data.ScoreboardData
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.displayConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardElementType
import at.hannibal2.skyhanni.utils.LorenzUtils

class Title : Element() {
    override fun getDisplayPair(): List<ScoreboardElementType> {
        val alignment = displayConfig.titleAndFooter.alignTitleAndFooter

        if (!LorenzUtils.inSkyBlock && !displayConfig.titleAndFooter.useCustomTitleOutsideSkyBlock) {
            return listOf(ScoreboardData.objectiveTitle to alignment)
        }

        return if (displayConfig.titleAndFooter.useCustomTitle) {
            listOf(
                displayConfig.titleAndFooter.customTitle
                    .replace("&", "§")
                    .split("\\n")
                    .map { it to alignment },
            ).flatten()
        } else {
            listOf(ScoreboardData.objectiveTitle to alignment)
        }
    }

    override fun showWhen() = true
}
