package at.hannibal2.skyhanni.features.gui.customscoreboard

import at.hannibal2.skyhanni.config.features.gui.customscoreboard.DisplayConfig
import at.hannibal2.skyhanni.data.BitsAPI
import at.hannibal2.skyhanni.data.HypixelData
import at.hannibal2.skyhanni.data.PurseAPI
import at.hannibal2.skyhanni.data.ScoreboardData
import at.hannibal2.skyhanni.data.model.TabWidget
import at.hannibal2.skyhanni.features.bingo.BingoAPI
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.displayConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardLine.Companion.align
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.formatDouble
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RegexUtils.groupOrNull
import at.hannibal2.skyhanni.utils.RegexUtils.matchGroup
import at.hannibal2.skyhanni.utils.StringUtils.removeResets
import at.hannibal2.skyhanni.utils.StringUtils.trimWhiteSpace
import java.util.regex.Pattern

@Suppress("TooManyFunctions")
object CustomScoreboardUtils {

    fun formatScoreboardNumberDisplayDisplay(text: String, number: String, color: String) = when (displayConfig.numberDisplayFormat) {
        ScoreboardNumberDisplayFormat.TEXT_COLOR_NUMBER -> "§f$text: $color$number"
        ScoreboardNumberDisplayFormat.COLOR_TEXT_NUMBER -> "$color$text: $number"
        ScoreboardNumberDisplayFormat.COLOR_NUMBER_TEXT -> "$color$number $color$text"
        ScoreboardNumberDisplayFormat.COLOR_NUMBER_RESET_TEXT -> "$color$number §f$text"
    }

    enum class ScoreboardNumberDisplayFormat(val config: String) {
        TEXT_COLOR_NUMBER("§fPurse: §6123"),
        COLOR_TEXT_NUMBER("§6Purse: 123"),
        COLOR_NUMBER_TEXT("§6123 Purse"),
        COLOR_NUMBER_RESET_TEXT("§6123 §fPurse"),
        ;

        override fun toString() = config
    }

    private fun getGroup(pattern: Pattern, list: List<String>, group: String) =
        list.map { it.removeResets().trimWhiteSpace() }.firstNotNullOfOrNull { line ->
            pattern.matchGroup(line, group)
        }

    fun getProfileTypeSymbol() = when {
        HypixelData.ironman -> "§7♲ "
        HypixelData.stranded -> "§a☀ "
        HypixelData.bingo -> ScoreboardData.sidebarLinesFormatted.firstNotNullOfOrNull {
            BingoAPI.getIconFromScoreboard(it)?.plus(" ") // TODO: add bingo rank to bingo api
        } ?: "§e❤ "

        else -> "§e"
    }

    internal fun formatNumber(number: Number): String = when (displayConfig.numberFormat) {
        DisplayConfig.NumberFormat.SHORT -> number.shortFormat()
        DisplayConfig.NumberFormat.LONG -> number.addSeparators()
        else -> "0"
    }

    internal fun formatStringNum(string: String) = formatNumber(string.formatDouble())

    internal fun getMotes() = getGroup(ScoreboardPattern.motesPattern, getSbLines(), "motes") ?: "0"

    internal fun getSoulflow() = TabWidget.SOULFLOW.matchMatcherFirstLine { group("amount") } ?: "0"

    internal fun getPurseEarned() = getGroup(PurseAPI.coinsPattern, getSbLines(), "earned")?.let { " §7(§e+$it§7)§6" }

    internal fun getBank() = TabWidget.BANK.matchMatcherFirstLine {
        group("amount") + (groupOrNull("personal")?.let { " §7/ §6$it" }.orEmpty())
    } ?: "0"

    internal fun getBits() = formatNumber(BitsAPI.bits.coerceAtLeast(0))

    internal fun getBitsAvailable() = formatNumber(BitsAPI.bitsAvailable.coerceAtLeast(0))

    internal fun getBitsLine() = if (displayConfig.showUnclaimedBits) {
        "§b${getBits()}§7/§b${getBitsAvailable()}"
    } else "§b${getBits()}"

    internal fun getCopper() = getGroup(ScoreboardPattern.copperPattern, getSbLines(), "copper") ?: "0"

    internal fun getGems() = TabWidget.GEMS.matchMatcherFirstLine { group("gems") } ?: "0"

    internal fun getHeat() = getGroup(ScoreboardPattern.heatPattern, getSbLines(), "heat")

    internal fun getNorthStars() = getGroup(ScoreboardPattern.northstarsPattern, getSbLines(), "northStars") ?: "0"

    internal fun getTimeSymbol() = getGroup(ScoreboardPattern.timePattern, getSbLines(), "symbol").orEmpty()

    internal fun getTablistEvent() = TabWidget.EVENT.matchMatcherFirstLine { groupOrNull("color") + group("event") }

    internal fun getElementsFromAny(element: Any?): List<ScoreboardLine> = when (element) {
        null -> listOf()
        is List<*> -> element.mapNotNull { it?.toScoreboardElement() }
        else -> listOfNotNull(element.toScoreboardElement())
    }

    private fun Any.toScoreboardElement(): ScoreboardLine? = when (this) {
        is String -> this.align()
        is ScoreboardLine -> this
        else -> null
    }

    internal fun getSbLines() = ScoreboardData.sidebarLinesFormatted
}
