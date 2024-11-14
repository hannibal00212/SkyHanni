package at.hannibal2.skyhanni.features.gui.customscoreboard.events

import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getSbLines
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern
import at.hannibal2.skyhanni.utils.RegexUtils.allMatches

// scoreboard
// scoreboard update event
object ScoreboardEventQueue : ScoreboardEvent() {

    override fun getDisplay() = elementPatterns.allMatches(getSbLines())

    override val configLine = "Queued: Glacite Mineshafts\nPosition: §b#45 §fSince: §a00:00"

    override val elementPatterns = listOf(
        ScoreboardPattern.queuePattern,
        ScoreboardPattern.queueTierPattern,
        ScoreboardPattern.queuePositionPattern,
    )
}
