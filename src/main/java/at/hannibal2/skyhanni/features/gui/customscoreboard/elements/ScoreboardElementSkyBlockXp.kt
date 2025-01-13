package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.api.SkyBlockXPAPI
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard

object ScoreboardElementSkyBlockXp : ScoreboardElement() {
    override fun getDisplay() = buildList {
        if (CustomScoreboard.displayConfig.displayNumbersFirst) {
            add("${SkyBlockXPAPI.getLevelColor().getChatColor()}${SkyBlockXPAPI.level} SB Level")
            add("§b${SkyBlockXPAPI.xp}§3/§b100 XP")
        } else {
            add("SB Level: ${SkyBlockXPAPI.getLevelColor().getChatColor()}${SkyBlockXPAPI.level}")
            add("XP: §b${SkyBlockXPAPI.xp}§3/§b100")
        }
    }

    override fun showWhen() = SkyBlockXPAPI.level != null && SkyBlockXPAPI.xp != null

    override val configLine = "SB Level: 287\nXP: §b26§3/§b100"
}
