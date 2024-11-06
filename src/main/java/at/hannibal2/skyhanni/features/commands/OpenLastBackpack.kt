package at.hannibal2.skyhanni.features.commands

import at.hannibal2.skyhanni.events.MessageSendToServerEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ChatUtils.senderIsSkyhanni
import at.hannibal2.skyhanni.utils.HypixelCommands
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object OpenLastBackpack {
    private var lastBackpack = -1

    @SubscribeEvent
    fun onMessageSendToServer(event: MessageSendToServerEvent) {
        if (event.senderIsSkyhanni()) return

        if (!event.message.startsWith("/backpack", ignoreCase = true) &&
            !event.message.startsWith("/bp", ignoreCase = true)
        ) {
            return
        }

        if (event.message.startsWith("/backpack -", ignoreCase = true) ||
            event.message.startsWith("/bp -", ignoreCase = true)
        ) {
            event.isCanceled = true
            if (lastBackpack == -1) {
                ChatUtils.chat("There is no previous backpack to reopen!")
                return
            }
            HypixelCommands.openBackpack(lastBackpack)
            ChatUtils.chat("Opened last backpack $lastBackpack.")
            return
        }

        val parts = event.message.split(" ")
        if (parts.size <= 1) {
            // "/bp" without any argument opens backpack #1
            lastBackpack = 1
            return
        }
        val intArg = parts[1].toIntOrNull()
        if (intArg != null) {
            // "/bp 0" still is a valid command (leads to the overview menu)
            lastBackpack = if (intArg < 0 || intArg > 18) -1 else intArg
        }
    }
}
