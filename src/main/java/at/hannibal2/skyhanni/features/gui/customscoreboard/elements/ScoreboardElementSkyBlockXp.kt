package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.api.SkyBlockXpApi
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard

object ScoreboardElementSkyBlockXp : ScoreboardElement() {
    override fun getDisplay() = buildList {
        val (level, xp) = SkyBlockXpApi.levelXpPair ?: return@buildList
        if (CustomScoreboard.displayConfig.displayNumbersFirst) {
            add("${SkyBlockXpApi.getLevelColor().getChatColor()}$level SB Level")
            add("§b$xp§3/§b100 XP")
        } else {
            add("SB Level: ${SkyBlockXpApi.getLevelColor().getChatColor()}$level")
            add("XP: §b$xp§3/§b100")
        }
    }

    override fun showWhen() = SkyBlockXpApi.levelXpPair != null

    override val configLine = "SB Level: 287\nXP: §b26§3/§b100"
}
