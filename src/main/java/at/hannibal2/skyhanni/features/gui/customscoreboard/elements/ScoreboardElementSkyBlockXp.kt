package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.api.SkyBlockXPAPI
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard

object ScoreboardElementSkyBlockXp : ScoreboardElement() {
    override fun getDisplay() = buildList {
        val (level, xp) = SkyBlockXPAPI.levelXpPair ?: return@buildList
        if (CustomScoreboard.displayConfig.displayNumbersFirst) {
            add("${SkyBlockXPAPI.getLevelColor().getChatColor()}$level SB Level")
            add("§b$xp§3/§b100 XP")
        } else {
            add("SB Level: ${SkyBlockXPAPI.getLevelColor().getChatColor()}$level")
            add("XP: §b$xp§3/§b100")
        }
    }

    override fun showWhen() = SkyBlockXPAPI.levelXpPair != null

    override val configLine = "SB Level: 287\nXP: §b26§3/§b100"
}
