package at.hannibal2.skyhanni.features.gui.customscoreboard.events

import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getSBLines
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern
import at.hannibal2.skyhanni.utils.RegexUtils.allMatches

// scoreboard
// scoreboard update event
object ScoreboardEventJacobMedals : ScoreboardEvent() {
    override fun getDisplay() = ScoreboardPattern.medalsPattern.allMatches(getSBLines())

    override val configLine = "§6§lGOLD §fmedals: §613\n§f§lSILVER §fmedals: §f3\n§c§lBRONZE §fmedals: §c4"

    override val elementPatterns = listOf(ScoreboardPattern.medalsPattern)

    // Can appear on any island when calling Anita through the AbiPhone
}
