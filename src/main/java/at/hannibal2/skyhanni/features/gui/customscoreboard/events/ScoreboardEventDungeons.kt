package at.hannibal2.skyhanni.features.gui.customscoreboard.events

import at.hannibal2.skyhanni.features.dungeon.DungeonAPI
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getSbLines
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern
import at.hannibal2.skyhanni.utils.RegexUtils.allMatches

// scoreboard
// scoreboard update event
object ScoreboardEventDungeons : ScoreboardEvent() {

    override fun getDisplay() = elementPatterns.allMatches(getSbLines()).map { it.removePrefix("§r") }

    override val configLine = "§7(All Dungeons Lines)"

    override val elementPatterns = listOf(
        ScoreboardPattern.m7dragonsPattern,
        ScoreboardPattern.autoClosingPattern,
        ScoreboardPattern.startingInPattern,
        ScoreboardPattern.keysPattern,
        ScoreboardPattern.timeElapsedPattern,
        ScoreboardPattern.clearedPattern,
        ScoreboardPattern.soloPattern,
        ScoreboardPattern.teammatesPattern,
        ScoreboardPattern.floor3GuardiansPattern,
    )

    override fun showIsland() = DungeonAPI.inDungeon()
}
