package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.api.SkyBlockXPApi
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils

object ScoreboardElementSkyBlockXP : ScoreboardElement() {
    override fun getDisplay() = buildList {
        val (level, xp) = SkyBlockXPApi.levelXPPair ?: return@buildList
        val color = SkyBlockXPApi.getLevelColor().getChatColor()
        add(CustomScoreboardUtils.formatScoreboardNumberDisplayDisplay("SB Level", level.toString(), color))
        add(CustomScoreboardUtils.formatScoreboardNumberDisplayDisplay("XP", "$xp§3/§b100", "§b"))
    }

    override fun showWhen() = SkyBlockXPApi.levelXPPair != null

    override val configLine = "SB Level: 287\nXP: §b26§3/§b100"
}
