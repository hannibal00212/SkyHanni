package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.hypixel.chat.event.SystemMessageEvent
import at.hannibal2.skyhanni.events.ChatHoverEvent
import at.hannibal2.skyhanni.events.item.ItemHoverEvent
import at.hannibal2.skyhanni.mixins.hooks.GuiChatHook
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.romanToDecimal
import at.hannibal2.skyhanni.utils.RegexUtils.findMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.StringUtils.applyIfPossible
import at.hannibal2.skyhanni.utils.StringUtils.isRoman
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText

@SkyHanniModule
object ReplaceRomanNumerals {
    private val patternGroup = RepoPattern.group("replace.roman")

    /**
     * REGEX-TEST: §9Dedication IV
     * REGEX-FAIL: §cD§6y§ee§as
     */
    private val findRomanNumeralPattern by patternGroup.pattern(
        "findroman",
        "[ ➜](?=[MDCLXVI])(?<roman>M*(?:C[MD]|D?C{0,3})(?:X[CL]|L?X{0,3})(?:I[XV]|V?I{0,3}))(?<extra>.?)"
    )

    /**
     * REGEX-TEST: K
     */
    private val isWordPattern by patternGroup.pattern(
        "findword",
        "^[\\w-']"
    )

    /**
     * REGEX-TEST: ➜
     */
    private val allowedCharactersAfter by patternGroup.pattern(
        "allowedcharactersafter",
        "[➜):]?"
    )

    @HandleEvent(priority = HandleEvent.LOWEST)
    fun onTooltip(event: ItemHoverEvent) {
        if (!isEnabled()) return

        event.toolTip.replaceAll { it.transformLine() }
    }

    @HandleEvent(priority = HandleEvent.LOWEST)
    fun onChatHover(event: ChatHoverEvent) {
        if (event.getHoverEvent().action != HoverEvent.Action.SHOW_TEXT) return
        if (!isEnabled()) return

        val lore = event.getHoverEvent().value.formattedText.split("\n").toMutableList()
        lore.replaceAll { it.transformLine() }

        val chatComponentText = ChatComponentText(lore.joinToString("\n"))
        val hoverEvent = HoverEvent(event.component.chatStyle.chatHoverEvent?.action, chatComponentText)

        GuiChatHook.replaceOnlyHoverEvent(hoverEvent)
    }

    @HandleEvent
    fun onSystemMessage(event: SystemMessageEvent) {
        if (!isEnabled()) return
        event.applyIfPossible { it.transformLine() }
    }

    /**
     * Transforms a line with a roman numeral to a line with a decimal numeral.
     * Override block one is to be used for tablist or other places where there is no need to check for normal text containing
     * the word "I".
     *
     * Currently not replaced:
     * - "§7Bonzo I Reward:" in the collection rewards when hovering on the collection
     */
    private fun String.transformLine(overrideBlockOne: Boolean = false): String {
        val (romanNumeral, rest) = findRomanNumeralPattern.findMatcher(this.removeFormatting()) {
            group("roman") to group("extra")
        } ?: return this

        if (romanNumeral.isNullOrEmpty() || !romanNumeral.isRoman() || isWordPattern.matches(rest)) {
            return recursiveSplit(romanNumeral)
        }

        val parsedRomanNumeral = romanNumeral.romanToDecimal()

        return takeIf { parsedRomanNumeral != 1 || overrideBlockOne || rest.isEmpty() || allowedCharactersAfter.matches(rest) }
            ?.replaceFirst(romanNumeral, parsedRomanNumeral.toString())?.transformLine()
            ?: recursiveSplit(romanNumeral)
    }

    private fun String.recursiveSplit(romanNumeral: String) =
        this.split(romanNumeral, limit = 2).let { it[0] + romanNumeral + it[1].transformLine() }

    private fun String.removeFormatting() = removeColor().replace(",", "")

    private fun isEnabled() = LorenzUtils.inSkyBlock && SkyHanniMod.feature.misc.replaceRomanNumerals
}
