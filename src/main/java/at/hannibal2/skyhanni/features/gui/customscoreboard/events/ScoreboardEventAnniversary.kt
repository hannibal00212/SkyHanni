package at.hannibal2.skyhanni.features.gui.customscoreboard.events

import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getSbLines
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatches

// scoreboard
// scoreboard update event
object ScoreboardEventAnniversary : ScoreboardEvent() {
    override fun getDisplay() = ScoreboardPattern.anniversaryPattern.firstMatches(getSbLines())

    override val configLine = "§d5th Anniversary§f 167:59:54"
}
