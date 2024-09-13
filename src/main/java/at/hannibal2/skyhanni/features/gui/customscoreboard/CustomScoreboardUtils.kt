package at.hannibal2.skyhanni.features.gui.customscoreboard

import at.hannibal2.skyhanni.config.features.gui.customscoreboard.DisplayConfig
import at.hannibal2.skyhanni.data.BitsAPI
import at.hannibal2.skyhanni.data.HypixelData
import at.hannibal2.skyhanni.features.bingo.BingoAPI
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.displayConfig
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.formatDouble
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.StringUtils.removeResets
import at.hannibal2.skyhanni.utils.StringUtils.trimWhiteSpace
import at.hannibal2.skyhanni.utils.TabListData
import java.util.regex.Pattern

object CustomScoreboardUtils {

    internal fun getGroupFromPattern(list: List<String>, pattern: Pattern, group: String) = list.map {
        it.removeResets().trimWhiteSpace()
    }.firstNotNullOfOrNull { line ->
        pattern.matchMatcher(line) {
            group(group)
        }
    }

    fun getProfileTypeSymbol() = when {
        HypixelData.ironman -> "§7♲ "
        HypixelData.stranded -> "§a☀ "
        HypixelData.bingo -> CustomScoreboard.activeLines.firstNotNullOfOrNull {
            BingoAPI.getIconFromScoreboard(it)?.plus(" ")
        } ?: "§e❤ "

        else -> "§e"
    }

    // TODO change to a non extended function
    internal fun Number.formatNum(): String = when (displayConfig.numberFormat) {
        DisplayConfig.NumberFormat.SHORT -> this.shortFormat()
        DisplayConfig.NumberFormat.LONG -> this.addSeparators()
        else -> "0"
    }

    internal fun String.formatNum() = this.formatDouble().formatNum()

    internal fun getMotes() = getGroupFromPattern(CustomScoreboard.activeLines, ScoreboardPattern.motesPattern, "motes")
    internal fun getBank() = getGroupFromPattern(TabListData.getTabList(), ScoreboardPattern.bankPattern, "bank")

    internal fun getBits() = BitsAPI.bits.coerceAtLeast(0).formatNum()

    internal fun getBitsToClaim() = BitsAPI.bitsAvailable.coerceAtLeast(0).formatNum()

    internal fun getBitsLine() = if (displayConfig.showUnclaimedBits) {
        "§b${getBits()}§7/§b${getBitsToClaim()}"
    } else {
        "§b${getBits()}"
    }

    internal fun getCopper() =
        getGroupFromPattern(CustomScoreboard.activeLines, ScoreboardPattern.copperPattern, "copper")

    internal fun getGems() = getGroupFromPattern(TabListData.getTabList(), ScoreboardPattern.gemsPattern, "gems")

    internal fun getHeat() =
        getGroupFromPattern(CustomScoreboard.activeLines, ScoreboardPattern.heatPattern, "heat")

    internal fun getNorthStars() =
        getGroupFromPattern(CustomScoreboard.activeLines, ScoreboardPattern.northstarsPattern, "northstars")


    class UndetectedScoreboardLines(message: String) : Exception(message)
}
