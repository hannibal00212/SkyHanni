package at.hannibal2.skyhanni.features.gui.customscoreboard.events

import at.hannibal2.skyhanni.data.model.TabWidget
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getTablistEvent
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher

// widget
// widget update event
object ScoreboardEventStartingSoonTablist : ScoreboardEvent() {
    override fun getDisplay(): List<String>? {
        val name = getTablistEvent() ?: return null

        val soonActiveEventTime = ScoreboardPattern.eventTimeStartsPattern.firstMatcher(TabWidget.EVENT.lines) { group("time") }
            ?: return null

        return listOf(name, " Starts in: §e$soonActiveEventTime")
    }

    override fun showWhen() = TabWidget.EVENT.isActive

    override val configLine = "§7(All Starting Soon Tablist Events)\n§6Mining Fiesta\n §fStarts in: §e52min"
}

// click: open /calendar
