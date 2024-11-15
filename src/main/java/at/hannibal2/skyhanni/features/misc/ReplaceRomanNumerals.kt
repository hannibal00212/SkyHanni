package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.hypixel.chat.event.SystemMessageEvent
import at.hannibal2.skyhanni.events.ChatHoverEvent
import at.hannibal2.skyhanni.events.LorenzToolTipEvent
import at.hannibal2.skyhanni.features.inventory.patternGroup
import at.hannibal2.skyhanni.mixins.hooks.GuiChatHook
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.romanToDecimal
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.StringUtils.applyIfPossible
import at.hannibal2.skyhanni.utils.StringUtils.isRoman
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object ReplaceRomanNumerals {
    // Using toRegex here since toPattern doesn't seem to provide the necessary functionality
    private val splitRegex = "((§\\w)|(\\s+)|(\\W))+|(\\w*)".toRegex()

    //
    /**
     * REGEX-TEST: §eSelect an option: §r§a[§aOk, then what?§a]
     */
    private val isSelectOptionPattern by patternGroup.pattern(
        "string.isselectoption",
        "§eSelect an option: .*"
    )

    // TODO: Remove after pr 1717 is ready and switch to ItemHoverEvent
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onTooltip(event: LorenzToolTipEvent) {
        if (!isEnabled()) return

        event.toolTip.replaceAll { it.transformLine() }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onChatHover(event: ChatHoverEvent) {
        if (event.getHoverEvent().action != HoverEvent.Action.SHOW_TEXT) return
        if (!isEnabled()) return

        val lore = event.getHoverEvent().value.formattedText.split("\n").toMutableList()
        lore.replaceAll { it.transformLine() }

        val chatComponentText = ChatComponentText(lore.joinToString("\n"))
        val hoverEvent = HoverEvent(event.component.chatStyle.chatHoverEvent?.action, chatComponentText)

        GuiChatHook.replaceOnlyHoverEvent(hoverEvent)
    }

    @SubscribeEvent
    fun onSystemMessage(event: SystemMessageEvent) {
        if (!isEnabled() || event.message.isSelectOption()) return
        event.applyIfPossible { it.transformLine() }
    }

    private fun String.isSelectOption(): Boolean = isSelectOptionPattern.matches(this)

    private fun String.transformLine() = splitRegex.findAll(this).map { it.value }.joinToString("") {
        it.takeIf { it.isValidRomanNumeral() && it.removeFormatting().romanToDecimal() != 2000 }?.coloredRomanToDecimal() ?: it
    }

    private fun String.removeFormatting() = removeColor().replace(",", "")

    private fun String.isValidRomanNumeral() = removeFormatting()
        .let { it.isRoman() && it.isNotEmpty() }

    private fun String.coloredRomanToDecimal() = removeFormatting()
        .let { replace(it, it.romanToDecimal().toString()) }

    private fun isEnabled() = LorenzUtils.inSkyBlock && SkyHanniMod.feature.misc.replaceRomanNumerals
}
