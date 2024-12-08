package at.hannibal2.skyhanni.features.gui.customscoreboard.events

import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getSbLines
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern
import at.hannibal2.skyhanni.utils.RegexUtils.allMatches

// scoreboard
// scoreboard update event
object ScoreboardEventQueue : ScoreboardEvent() {

    private val patterns = listOf(
        ScoreboardPattern.queuePattern,
        ScoreboardPattern.queueTierPattern,
        ScoreboardPattern.queuePositionPattern,
    )

    override fun getDisplay() = patterns.allMatches(getSbLines())

    override val configLine = "Queued: Glacite Mineshafts\nPosition: §b#45 §fSince: §a00:00"
}
