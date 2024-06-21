package at.hannibal2.skyhanni.features.gui.customscoreboard.events

import at.hannibal2.skyhanni.features.dungeon.DungeonAPI
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getSbLines
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern
import at.hannibal2.skyhanni.utils.RegexUtils.allMatches

object Dungeons : Event() {
    override fun getDisplay() = listOf(
        ScoreboardPattern.m7dragonsPattern,
        ScoreboardPattern.autoClosingPattern,
        ScoreboardPattern.startingInPattern,
        ScoreboardPattern.keysPattern,
        ScoreboardPattern.timeElapsedPattern,
        ScoreboardPattern.clearedPattern,
        ScoreboardPattern.soloPattern,
        ScoreboardPattern.teammatesPattern,
        ScoreboardPattern.floor3GuardiansPattern,
    ).allMatches(getSbLines()).map { it.removePrefix("§r") }

    override fun showWhen() = DungeonAPI.inDungeon()

    override val configLine = "§7(All Dungeons Lines)"
}
