package at.hannibal2.skyhanni.features.chat.playerchat

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.hypixel.chat.event.SystemMessageEvent
import at.hannibal2.skyhanni.features.misc.MarkedPlayerManager
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.StringUtils.applyIfPossible
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.IChatComponent

@SkyHanniModule
object PlayerChatModifier {

    private val config get() = SkyHanniMod.feature.chat.playerMessage
    private val patterns = mutableListOf<Regex>()

    init {
        patterns.add("§[ab6]\\[(?:VIP|MVP)(?:§.|\\+)*] {1,2}(?:§[7ab6])?(\\w{2,16})".toRegex()) // ranked player with prefix everywhere
        patterns.add("§[7ab6](\\w{2,16})§r(?!§7x)(?!\$)".toRegex()) // all players without rank prefix in notification messages
    }

    @HandleEvent
    fun onChat(event: SystemMessageEvent) {
        event.applyIfPossible { cutMessage(it) }
    }

    private fun findClickableTexts(chatComponent: IChatComponent, clickEvents: MutableList<ClickEvent>) {
        for (sibling in chatComponent.siblings) {
            findClickableTexts(sibling, clickEvents)
        }
        val clickEvent = chatComponent.chatStyle.chatClickEvent ?: return
        clickEvent.action ?: return
        if (clickEvents.any { it.value == clickEvent.value }) return
        clickEvents.add(clickEvent)
    }

    private fun findHoverTexts(chatComponent: IChatComponent, hoverEvents: MutableList<HoverEvent>) {
        for (sibling in chatComponent.siblings) {
            findHoverTexts(sibling, hoverEvents)
        }
        val hoverEvent = chatComponent.chatStyle.chatHoverEvent ?: return
        hoverEvent.action ?: return
        if (hoverEvents.any { it.value == hoverEvent.value }) return
        hoverEvents.add(hoverEvent)
    }

    private fun cutMessage(input: String): String {
        var string = input

        if (config.playerRankHider) {
            for (pattern in patterns) {
                string = string.replace(pattern, "§b$1")
            }
            string = string.replace("§[7ab6]((?:\\w+){2,16})'s", "§b$1's")
            string = string.replace("§[7ab6]((?:\\w+){2,16}) (§.)", "§b$1 $2")
        }

        string = MarkedPlayerManager.replaceInChat(string)

        return string
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "chat.playerRankHider", "chat.playerMessage.playerRankHider")
        event.move(3, "chat.chatFilter", "chat.playerMessage.chatFilter")
    }
}
