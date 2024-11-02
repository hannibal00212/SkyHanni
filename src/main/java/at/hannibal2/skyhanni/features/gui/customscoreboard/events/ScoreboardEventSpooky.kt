package at.hannibal2.skyhanni.features.gui.customscoreboard.events

import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getSbLines
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatches
import at.hannibal2.skyhanni.utils.StringUtils.removeResets
import at.hannibal2.skyhanni.utils.TabListData

// scoreboard && tablist
// scoreboard update event and tablist footer update event
object ScoreboardEventSpooky : ScoreboardEvent() {
    override fun getDisplay() = buildList {
        ScoreboardPattern.spookyPattern.firstMatches(getSbLines())?.let { time ->
            add(time)
            add("§7Your Candy: ")
            add(
                TabListData.getFooter()
                    .removeResets()
                    .split("\n")
                    .firstOrNull { it.startsWith("§7Your Candy:") }
                    ?.removePrefix("§7Your Candy: ") ?: "§cCandy not found",
            )
        }
    }

    // TODO: Add isSpookyActive() somewhere

    override val configLine = "§6Spooky Festival§f 50:54\n§7Your Candy:\n§a1 Green§7, §50 Purple §7(§61 §7pts.)"
}
