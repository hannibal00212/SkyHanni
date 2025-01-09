package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.currentIslandEvents
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.eventsConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getElementsFromAny
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardLine
import at.hannibal2.skyhanni.utils.CollectionUtils.takeIfNotEmpty

// everything
// whenever an event gets updated
object ScoreboardElementEvents : ScoreboardElement() {
    override fun getDisplay() =
        if (eventsConfig.showAllActiveEvents) currentIslandEvents.mapNotNull { it.getLines().takeIfNotEmpty() }.flatten()
        else currentIslandEvents.firstNotNullOfOrNull { it.getLines().takeIfNotEmpty() }

    override val configLine = "§7Wide Range of Events\n§7(too much to show all)"

    override fun getLines(): List<ScoreboardLine> = if (showWhen()) getElementsFromAny(getDisplay()) else listOf()
}
