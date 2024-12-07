package at.hannibal2.skyhanni.features.commands

import at.hannibal2.skyhanni.events.MessageSendToServerEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ChatUtils.senderIsSkyhanni
import at.hannibal2.skyhanni.utils.HypixelCommands
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object OpenLastStorage {
    private enum class StorageType {
        ENDER_CHEST, BACKPACK
    }

    private var lastBackpack = -1
    private var lastEnderChest = -1
    // Default to Ender Chest as last storage type, since every profile on any
    // account has at least one partial ender chest page unlocked
    private var lastStorageType = StorageType.ENDER_CHEST

    private fun openLastBackPack() {
        HypixelCommands.openBackpack(lastBackpack)
        ChatUtils.chat("Opened last backpack $lastBackpack.")
    }

    private fun openLastEnderChest() {
        HypixelCommands.openEnderChest(lastEnderChest)
        ChatUtils.chat("Opened last ender chest $lastEnderChest.")
    }

    @SubscribeEvent
    fun onMessageSendToServer(event: MessageSendToServerEvent) {
        if (event.senderIsSkyhanni()) return

        if (handleBackpack(event)) return
        if (handleEnderChest(event)) return

        // Non-Hypixel combined "last opened" command for backpack + ender chest
        if (!event.message.startsWith("/lastopened", ignoreCase = true) &&
            // cannot use startsWith because of /locraw
            !event.message.equals("/lo", ignoreCase = true)
        ) {
            return
        }

        event.isCanceled = true

        if (lastStorageType == StorageType.ENDER_CHEST) {
            openLastEnderChest()
        } else if (lastStorageType == StorageType.BACKPACK) {
            openLastBackPack()
        }
    }

    private fun handleBackpack(event: MessageSendToServerEvent): Boolean {
        if (!event.message.startsWith("/backpack", ignoreCase = true) &&
            !event.message.startsWith("/bp", ignoreCase = true)
        ) {
            return false
        }

        if (event.message.startsWith("/backpack -", ignoreCase = true) ||
            event.message.startsWith("/bp -", ignoreCase = true)
        ) {
            event.isCanceled = true
            if (lastBackpack == -1) {
                ChatUtils.chat("There is no previous backpack to reopen!")
                return true
            }
            openLastBackPack()
            return true
        }

        val parts = event.message.split(" ")
        if (parts.size <= 1) {
            // "/bp" without any argument opens backpack #1
            lastBackpack = 1
            return true
        }
        val intArg = parts[1].toIntOrNull()
        if (intArg != null) {
            // "/bp 0" still is a valid command (leads to the overview menu)
            lastBackpack = if (intArg < 0 || intArg > 18) -1 else intArg
        }

        return true
    }

    private fun handleEnderChest(event: MessageSendToServerEvent): Boolean {
        if (!event.message.startsWith("/enderchest", ignoreCase = true) &&
            !event.message.startsWith("/ec", ignoreCase = true)
        ) {
            return false
        }

        if (event.message.startsWith("/enderchest -", ignoreCase = true) ||
            event.message.startsWith("/ec -", ignoreCase = true)
        ) {
            event.isCanceled = true
            if (lastEnderChest == -1) {
                ChatUtils.chat("There is no previous ender chest to reopen!")
                return true
            }
            openLastEnderChest()
            return true
        }

        val parts = event.message.split(" ")
        if (parts.size <= 1) {
            // "/ec" without any argument opens ender chest page #1
            lastEnderChest = 1
            return true
        }
        val intArg = parts[1].toIntOrNull()
        if (intArg != null) {
            // Surprisingly, "/ec [int <= 0]" would still be a valid command and lead to "/ec 1"!
            lastEnderChest = if (intArg < 1 || intArg > 9) -1 else intArg
        }

        return true
    }
}
