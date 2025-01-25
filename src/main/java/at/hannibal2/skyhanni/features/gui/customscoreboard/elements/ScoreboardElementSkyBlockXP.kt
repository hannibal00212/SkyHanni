package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.api.SkyBlockXPApi
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard

object ScoreboardElementSkyBlockXP : ScoreboardElement() {
    override fun getDisplay() = buildList {
        val (level, xp) = SkyBlockXPApi.levelXPPair ?: return@buildList
        if (CustomScoreboard.displayConfig.displayNumbersFirst) {
            add("${SkyBlockXPApi.getLevelColor().getChatColor()}$level SB Level")
            add("§b$xp§3/§b100 XP")
        } else {
            add("SB Level: ${SkyBlockXPApi.getLevelColor().getChatColor()}$level")
            add("XP: §b$xp§3/§b100")
        }
    }

    override fun showWhen() = SkyBlockXPApi.levelXPPair != null

    override val configLine = "SB Level: 287\nXP: §b26§3/§b100"
}
