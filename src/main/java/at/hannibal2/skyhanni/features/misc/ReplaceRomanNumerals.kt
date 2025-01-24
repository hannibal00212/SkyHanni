package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.hypixel.chat.event.SystemMessageEvent
import at.hannibal2.skyhanni.events.ChatHoverEvent
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.minecraft.ToolTipEvent
import at.hannibal2.skyhanni.mixins.hooks.GuiChatHook
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.romanToDecimal
import at.hannibal2.skyhanni.utils.RecalculatingValue
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.StringUtils.applyIfPossible
import at.hannibal2.skyhanni.utils.StringUtils.isRoman
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.TimeLimitedCache
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object ReplaceRomanNumerals {
    // Using toRegex here since toPattern doesn't seem to provide the necessary functionality
    private val splitRegex = "((§\\w)|(\\s+)|(\\W))+|(\\w*)".toRegex()
    private val cachedStrings = TimeLimitedCache<String, String>(5.seconds)

    private val patternGroup = RepoPattern.group("replace.roman.numerals")

    @Suppress("MaxLineLength")
    private val allowedPatterns by patternGroup.list(
        "allowed.patterns",
        "§o§a(?:Combat|Farming|Fishing|Mining|Foraging|Enchanting|Alchemy|Carpentry|Runecrafting|Taming|Social|)( Level)? (?<roman>[IVXLCDM]+)§r",
        "(?:§5§o)?§7Progress to (?:Collection|Level|Tier|Floor|Milestone|Chocolate Factory) (?<roman>[IVXLCDM]+): §.(?:.*)%",
        "§5§o  §e(?:\\w+) (?<roman>[IVXLCDM]+)",
        "(?:§.)*Abiphone (?<roman>[IVXLCDM]+) .*",
        "§o§a§a(?:§c§lMM§c )?The Catacombs §8- §eFloor (?<roman>[IVXLCDM]+)§r",
        ".*Extra Farming Fortune (?<roman>[IVXLCDM]+)",
        ".*(?:Collection|Level|Tier|Floor|Milestone) (?<roman>[IVXLCDM]+)(?: ?§(?:7|r).*)?",
        "(?:§5§o§a ✔|§5§o§c ✖) §.* (?<roman>[IVXLCDM]+)",
        "§o§a✔ §.* (?<roman>[IVXLCDM]+)§r",
        "§5§o§7Purchase §a.* (?<roman>[IVXLCDM]+) §7.*",
        "§5§o(?:§7)§.(?<roman>[IVXLCDM]+).*",
        ".*Heart of the Mountain (?<roman>[IVXLCDM]+) ?.*",
    )

    /**
     * REGEX-TEST: §eSelect an option: §r§a[§aOk, then what?§a]
     */
    private val isSelectOptionPattern by patternGroup.pattern(
        "string.isselectoption",
        "§eSelect an option: .*",
    )

    // TODO: Remove after pr 1717 is ready and switch to ItemHoverEvent
    @HandleEvent(priority = HandleEvent.LOWEST)
    fun onTooltip(event: ToolTipEvent) {
        if (!isEnabled()) return

        event.toolTip.replaceAll { it.tryReplace() }
    }

    @HandleEvent(priority = HandleEvent.LOWEST)
    fun onChatHover(event: ChatHoverEvent) {
        if (event.getHoverEvent().action != HoverEvent.Action.SHOW_TEXT) return
        if (!isEnabled()) return

        val lore = event.getHoverEvent().value.formattedText.split("\n").toMutableList()
        lore.replaceAll { it.tryReplace() }

        val chatComponentText = ChatComponentText(lore.joinToString("\n"))
        val hoverEvent = HoverEvent(event.component.chatStyle.chatHoverEvent?.action, chatComponentText)

        GuiChatHook.replaceOnlyHoverEvent(hoverEvent)
    }

    @HandleEvent
    fun onSystemMessage(event: SystemMessageEvent) {
        if (!isEnabled() || event.message.isSelectOption()) return
        event.applyIfPossible { it.tryReplace() }
    }

    @HandleEvent(priority = HandleEvent.LOW)
    fun onRepoReload(event: RepositoryReloadEvent) {
        cachedStrings.clear()
    }

    private fun String.isSelectOption(): Boolean = isSelectOptionPattern.matches(this)

    private fun String.tryReplace(): String = cachedStrings.getOrPut(this) {
        if (allowedPatterns.matches(this)) replace() else this
    }

    fun replaceLine(line: String): String {
        if (!isEnabled()) return line

        return cachedStrings.getOrPut(line) {
            line.replace()
        }
    }

    private fun String.replace() = splitRegex.findAll(this).map { it.value }.joinToString("") {
        it.takeIf { it.isValidRomanNumeral() && it.removeFormatting().romanToDecimal() != 2000 }?.coloredRomanToDecimal() ?: it
    }

    private fun String.removeFormatting() = removeColor().replace(",", "")

    private fun String.isValidRomanNumeral() = removeFormatting().let { it.isRoman() && it.isNotEmpty() }

    private fun String.coloredRomanToDecimal() = removeFormatting().let { replace(it, it.romanToDecimal().toString()) }

    private fun isEnabled() = LorenzUtils.inSkyBlock && SkyHanniMod.feature.misc.replaceRomanNumerals.get()

    init {
        RecalculatingValue
    }

    @HandleEvent
    fun onDebug(event: DebugDataCollectEvent) {
        event.title("Replace Roman Numerals")
        event.addIrrelevant {
            val map = cachedStrings.toMap()
            add("cachedStrings: (${map.size})")
            for ((original, changed) in map) {
                if (original == changed) {
                    add("unchanged: '$original'")
                } else {
                    add("'$original' -> '$changed'")
                }
            }
        }
    }
}
