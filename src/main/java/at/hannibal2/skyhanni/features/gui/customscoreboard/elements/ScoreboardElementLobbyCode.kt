package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.data.HypixelData
import at.hannibal2.skyhanni.features.dungeon.DungeonAPI
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// internal
// update on island change and every second while in dungeons
object ScoreboardElementLobbyCode : ScoreboardElement() {
    private val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")

    override fun getDisplay() = buildString {
        if (CustomScoreboard.displayConfig.dateInLobbyCode) append("§7${LocalDate.now().format(formatter)} ")
        HypixelData.serverId?.let { append("a§8$it") }
        DungeonAPI.getRoomID()?.let { append(" b§8$it") }
    }

    override val configLine = "§710/23/2024 §8mega77CK"
}
