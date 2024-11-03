package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.data.HypixelData
import at.hannibal2.skyhanni.features.dungeon.DungeonAPI
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// internal
// update on island change and every second while in dungeons
object ScoreboardElementLobbyCode : ScoreboardElement() {
    private val dateFormatterMonthFirst = DateTimeFormatter.ofPattern("MM/dd/yyyy")
    private val dateFormatterDayFirst = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    override fun getDisplay() = buildString {
        val formatter = if (CustomScoreboard.displayConfig.useDayFirstDateFormat) dateFormatterDayFirst else dateFormatterMonthFirst
        if (CustomScoreboard.displayConfig.dateInLobbyCode) append("§7${LocalDate.now().format(formatter)} ")
        HypixelData.serverId?.let { append("§8$it") }
        DungeonAPI.roomId?.let { append(" §8$it") }
    }

    override val configLine = "§7${LocalDate.now().format(dateFormatterMonthFirst)} §8mega77CK"
}
