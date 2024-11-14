package at.hannibal2.skyhanni.features.gui.customscoreboard.events

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.model.TabWidget
import at.hannibal2.skyhanni.features.combat.SpidersDenAPI
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import java.util.regex.Pattern

// scoreboard
// widget update event
object ScoreboardEventBroodmother : ScoreboardEvent() {
    override fun getDisplay() = TabWidget.BROODMOTHER.lines.map { it.trim() }

    override val configLine = "Broodmother§7: §eDormant"

    override val elementPatterns = listOf(SpidersDenAPI.broodmotherPattern)

    override fun showIsland() = IslandType.SPIDER_DEN.isInIsland()
}
