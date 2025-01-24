package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.data.HypixelData
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern
import at.hannibal2.skyhanni.utils.StringUtils.firstLetterUppercase

// internal and scoreboard
// island change event
object ScoreboardElementProfile : ScoreboardElement() {
    override fun getDisplay() = buildString {
        append(CustomScoreboardUtils.getProfileTypeSymbol())
        if (CustomScoreboard.displayConfig.showProfileName) {
            append(HypixelData.profileName.firstLetterUppercase())
        } else {
            when {
                HypixelData.ironman -> append("Ironman")
                HypixelData.stranded -> append("Stranded")
                HypixelData.bingo -> append("Bingo")
                else -> append("Normal")
            }
        }
    }

    override val configLine = "§7♲ Ironman"

    override val elementPatterns = listOf(ScoreboardPattern.profileTypePattern)
}

// click: does a command for profile management exist?
