package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.data.DateFormat
import at.hannibal2.skyhanni.data.HypixelData
import at.hannibal2.skyhanni.features.dungeon.DungeonAPI
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard

// internal
// update on island change and every second while in dungeons
object ScoreboardElementLobbyCode : ScoreboardElement() {
    override fun getDisplay() = buildString {
        if (CustomScoreboard.displayConfig.dateInLobbyCode) append("§7${CustomScoreboard.displayConfig.dateFormat} ")
        HypixelData.serverId?.let { append("§8$it") }
        DungeonAPI.roomId?.let { append(" §8$it") }
    }

    override val configLine = "§7${DateFormat.US_SLASH_MMDDYYYY} §8mega77CK"
}
