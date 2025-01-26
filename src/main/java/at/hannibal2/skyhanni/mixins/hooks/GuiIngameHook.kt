package at.hannibal2.skyhanni.mixins.hooks

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.PurseApi
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import net.minecraft.client.gui.FontRenderer

fun drawString(
    instance: FontRenderer,
    text: String,
    x: Int,
    y: Int,
    color: Int,
) = tryToReplaceScoreboardLine(text)?.let {
    instance.drawString(it, x, y, color)
} ?: 0

/**
 * Tries to replace a scoreboard line with a modified one
 * @param text The line to check and possibly replace
 * @return The replaced line, or null if it should be hidden
 */
fun tryToReplaceScoreboardLine(text: String): String? {
    try {
        return tryToReplaceScoreboardLineHarder(text)
    } catch (t: Throwable) {
        ErrorManager.logErrorWithData(
            t,
            "Error while changing the scoreboard text.",
            "text" to text
        )
        return text
    }
}

private fun tryToReplaceScoreboardLineHarder(text: String): String? {
    if (SkyHanniMod.feature.misc.hideScoreboardNumbers && text.startsWith("§c") && text.length <= 4) {
        return null
    }
    if (SkyHanniMod.feature.misc.hidePiggyScoreboard) {
        PurseApi.piggyPattern.matchMatcher(text) {
            val coins = group("coins")
            return "Purse: $coins"
        }
    }

    if (SkyHanniMod.feature.misc.colorMonthNames) {
        for (season in Season.entries) {
            if (text.trim().startsWith(season.prefix)) {
                return season.colorCode + text
            }
        }
    }

    return text
}

enum class Season(val prefix: String, val colorCode: String) {
    EARLY_SPRING("Early Spring", "§d"),
    SPRING("Spring", "§d"),
    LATE_SPRING("Late Spring", "§d"),
    EARLY_SUMMER("Early Summer", "§6"),
    SUMMER("Summer", "§6"),
    LATE_SUMMER("Late Summer", "§6"),
    EARLY_AUTUMN("Early Autumn", "§e"),
    AUTUMN("Autumn", "§e"),
    LATE_AUTUMN("Late Autumn", "§e"),
    EARLY_WINTER("Early Winter", "§9"),
    WINTER("Winter", "§9"),
    LATE_WINTER("Late Winter", "§9")
}
