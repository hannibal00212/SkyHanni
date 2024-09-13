package at.hannibal2.skyhanni.features.combat

import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.RegexUtils.matches

@SkyHanniModule
object SpidersDenAPI {
    private fun getSbLines(): List<String> = CustomScoreboard.activeLines
    fun isAtTopOfNest(): Boolean = getSbLines().any { ScoreboardPattern.broodmotherPattern.matches(it) }
}
