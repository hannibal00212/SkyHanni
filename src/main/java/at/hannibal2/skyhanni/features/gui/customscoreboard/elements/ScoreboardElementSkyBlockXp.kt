package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.api.SkyBlockXPAPI
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils

object ScoreboardElementSkyBlockXp : ScoreboardElement() {
    override fun getDisplay() = buildList {
        val (level, xp) = SkyBlockXPAPI.levelXpPair ?: return@buildList
        val color = SkyBlockXPAPI.getLevelColor().getChatColor()
        add(CustomScoreboardUtils.formatScoreboardNumberDisplayDisplay("SB Level", level.toString(), color))
        add(CustomScoreboardUtils.formatScoreboardNumberDisplayDisplay("XP", "$xp§3/§b100", "§b"))
    }

    override fun showWhen() = SkyBlockXPAPI.levelXpPair != null

    override val configLine = "SB Level: 287\nXP: §b26§3/§b100"
}
