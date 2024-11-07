package at.hannibal2.skyhanni.features.gui.customscoreboard.events

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getSbLines
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.RegexUtils.allMatches

// scoreboard
// scoreboard update event
object ScoreboardEventDojo : ScoreboardEvent() {

    private val patterns = listOf(
        ScoreboardPattern.dojoChallengePattern,
        ScoreboardPattern.dojoDifficultyPattern,
        ScoreboardPattern.dojoPointsPattern,
        ScoreboardPattern.dojoTimePattern,
    )

    override fun getDisplay() = patterns.allMatches(getSbLines())

    override fun showWhen() = LorenzUtils.skyBlockArea == "Dojo Arena"

    override val configLine = "§7(All Dojo Lines)"

    override fun showIsland() = IslandType.CRIMSON_ISLE.isInIsland()
}
