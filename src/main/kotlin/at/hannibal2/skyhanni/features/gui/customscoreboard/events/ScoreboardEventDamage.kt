package at.hannibal2.skyhanni.features.gui.customscoreboard.events

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getSbLines
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.RegexUtils.allMatches

// scoreboard
// scoreboard update event
object ScoreboardEventDamage : ScoreboardEvent() {

    override fun getDisplay() = elementPatterns.allMatches(getSbLines())

    override val configLine = "Dragon HP: §a6,180,925 §c❤\nYour Damage: §c375,298.5"

    override val elementPatterns = listOf(
        ScoreboardPattern.bossHPPattern,
        ScoreboardPattern.bossDamagePattern,
    )

    override fun showIsland() = IslandType.THE_END.isInIsland()
}
