package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.features.gui.customscoreboard.ChunkedStatsLine.Companion.getChunkedStats
import at.hannibal2.skyhanni.features.gui.customscoreboard.ChunkedStatsLine.Companion.shouldShowChunkedStats
import at.hannibal2.skyhanni.features.gui.customscoreboard.ChunkedStatsLine.Companion.showChunkedStatsIsland
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.chunkedConfig

// internal, widget and scoreboard
// a bit of everything
object ScoreboardElementChunkedStats : ScoreboardElement() {
    override fun getDisplay() = getChunkedStats().chunked(chunkedConfig.maxStatsPerLine)
        .map { it.joinToString(" §f| ") }

    override fun showWhen() = shouldShowChunkedStats()

    override val configLine = "§652,763,737 §7| §d64,647 §7| §6249M\n§b59,264 §7| §c23,495 §7| §a57,873\n§c♨ 0 §7| §b0❄ §7| §d756"

    override fun showIsland() = showChunkedStatsIsland()
}
