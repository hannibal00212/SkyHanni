package at.hannibal2.skyhanni.test.command

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.OSUtils
import at.hannibal2.skyhanni.utils.StringUtils.stripHypixelMessage
import net.minecraft.util.ChatComponentText
import net.minecraft.util.IChatComponent

object TestChatCommand {

    fun command(args: Array<String>) {
        if (args.isEmpty()) {
            val syntaxStrings = listOf(
                "§7Syntax: §e/shtestmessage §7<§echat message§7> [flags]",
                "   §7[-lines]§e: §7Split the message into multiple by newlines",
                "   §7[-complex]§e: §7Parse the message as a JSON chat component",
                "   §7[-clipboard]§e: §7Read the message from the clipboard",
                "   §7[-s]§e: §7Hide the testing message",
                "   §7[-sa]§e: §7Hide everything but the final message", // Not really sure why you'd want this
            )
            ChatUtils.userError("Specify a chat message to test!\n${syntaxStrings.joinToString("\n")}")
            return
        }

        SkyHanniMod.launchCoroutine {
            val mutArgs = args.toMutableList()
            val multiLines = mutArgs.remove("-lines")
            val isComplex = mutArgs.remove("-complex")
            // cant use multi lines without clipboard
            val isClipboard = mutArgs.remove("-clipboard") || multiLines
            val isSilentAll = mutArgs.remove("-sa")
            val isSilent = mutArgs.remove("-s") || isSilentAll
            val text = if (isClipboard) {
                OSUtils.readFromClipboard() ?: return@launchCoroutine ChatUtils.userError("Clipboard does not contain a string!")
            } else mutArgs.joinToString(" ")
            if (multiLines) {
                for (line in text.split("\n")) {
                    extracted(isComplex, line, isSilent, isSilentAll)
                }
            } else {
                extracted(isComplex, text, isSilent, isSilentAll)
            }
        }
    }

    private fun extracted(isComplex: Boolean, text: String, isSilent: Boolean, isSilentAll: Boolean) {
        val component = if (isComplex) try {
            IChatComponent.Serializer.jsonToComponent(text) ?: ChatComponentText("")
        } catch (ex: Exception) {
            ChatUtils.userError("Please provide a valid JSON chat component (either in the command or via -clipboard)")
            return
        }
        else ChatComponentText(text.replace("&", "§"))

        println("component unformatted: ${component.unformattedText}")
        println("${component.unformattedTextForChat} ${component.chatStyle} ${component.siblings}")
        println(component)

        val rawText = component.formattedText.stripHypixelMessage().replace("§", "&").replace("\n", "\\n")
        if (!isSilent) ChatUtils.chat("Testing message: §7$rawText", prefixColor = "§a")

        test(component, isSilentAll)
    }

    private fun test(componentText: IChatComponent, isHidden: Boolean) {
        val message = componentText.formattedText.stripHypixelMessage()
        val event = SkyHanniChatEvent(message, componentText)
        event.post()

        if (event.blockedReason != "") {
            if (!isHidden) ChatUtils.chat("§cChat blocked: ${event.blockedReason}")
            return
        }
        val finalMessage = event.chatComponent
        if (!isHidden && finalMessage.formattedText.stripHypixelMessage() != message) {
            ChatUtils.chat("§eChat modified!")
        }
        ChatUtils.chat(finalMessage)
    }
}
