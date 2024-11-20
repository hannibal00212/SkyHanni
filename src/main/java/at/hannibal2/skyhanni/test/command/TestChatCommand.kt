package at.hannibal2.skyhanni.test.command

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.OSUtils
import at.hannibal2.skyhanni.utils.StringUtils.stripHypixelMessage
import net.minecraft.util.ChatComponentText
import net.minecraft.util.IChatComponent

object TestChatCommand {

    fun command(args: Array<String>) {
        if (args.isEmpty()) {
            val syntaxStrings = listOf(
                "§7Syntax: §e/shtestmessage §7<§e-chat message§7> §7[-lines] [-complex] [-clipboard] [-s]",
                "   §7[-lines]§e: §7Split the message into multiple by newlines",
                "   §7[-complex]§e: §7Parse the message as a JSON chat component",
                "   §7[-clipboard]§e: §7Read the message from the clipboard",
                "   §7[-s]§e: §7Hide the output message",
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
            val isHidden = mutArgs.remove("-s")
            val text = if (isClipboard) {
                OSUtils.readFromClipboard()
                    ?: return@launchCoroutine ChatUtils.userError("Clipboard does not contain a string!")
            } else mutArgs.joinToString(" ")
            if (multiLines) {
                for (line in text.split("\n")) {
                    extracted(isComplex, line, isHidden)
                }
            } else {
                extracted(isComplex, text, isHidden)
            }
        }
    }

    private fun extracted(isComplex: Boolean, text: String, isHidden: Boolean) {
        val component =
            if (isComplex)
                try {
                    IChatComponent.Serializer.jsonToComponent(text) ?: ChatComponentText("")
                } catch (ex: Exception) {
                    ChatUtils.userError("Please provide a valid JSON chat component (either in the command or via -clipboard)")
                    return
                }
            else ChatComponentText(text.replace("&", "§"))
        // TODO add additional hide parameter
//         if (!isHidden) ChatUtils.chat("Testing message: §7${component.formattedText}", prefixColor = "§a")
        test(component, isHidden)
    }

    private fun test(componentText: IChatComponent, isHidden: Boolean) {
        val message = componentText.formattedText.stripHypixelMessage()
        val event = LorenzChatEvent(message, componentText)
        event.postAndCatch()

        if (isHidden) return

        if (event.blockedReason != "") {
            ChatUtils.chat("§cChat blocked: ${event.blockedReason}")
        } else {
            val finalMessage = event.chatComponent
            if (finalMessage.formattedText.stripHypixelMessage() != message) {
                ChatUtils.chat("§eChat modified!")
            }
            ChatUtils.chat(finalMessage)
        }
    }
}
