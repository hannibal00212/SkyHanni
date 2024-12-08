package at.hannibal2.skyhanni.features.commands

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.events.MessageSendToServerEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ChatUtils.senderIsSkyhanni
import at.hannibal2.skyhanni.utils.HypixelCommands
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object OpenLastStorage {
    private enum class StorageType {
        ENDER_CHEST,
        BACKPACK
    }

    private var lastBackpack: Int? = null
    private var lastEnderChest: Int? = null

    // Default to Ender Chest as last storage type, since every profile on any
    // account has at least one partial ender chest page unlocked
    private var lastStorageType = StorageType.ENDER_CHEST

    private fun openLastStoragePage(storageType: StorageType) {
        val storageItem = when (storageType) {
            StorageType.BACKPACK -> lastBackpack.also { backpack -> backpack?.let { HypixelCommands.openBackpack(it) } }
            StorageType.ENDER_CHEST -> lastEnderChest.also { enderChest -> enderChest?.let { HypixelCommands.openEnderChest(it) } }
        }

        val storageMessage = storageItem?.let { "Opened last ${storageType.name.lowercase().replace("_", " ")} $it." }
            ?: "No last ${storageType.name.lowercase().replace("_", " ")} to open."

        ChatUtils.chat(storageMessage)
    }

    @SubscribeEvent
    fun onMessageSendToServer(event: MessageSendToServerEvent) {
        if (event.senderIsSkyhanni()) return

        val message = event.message.lowercase()

        if (message.startsWith("/backpack") ||
            message.startsWith("/bp")
        ) {
            handleStorage(event, message, StorageType.BACKPACK)
        } else if (message.startsWith("/enderchest") ||
            message.startsWith("/ec")
        ) {
            handleStorage(event, message, StorageType.ENDER_CHEST)
        }
    }

    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.register("shlastopened") {
            description = "Opens the storage page last accessed by either /ec or /bp"
            category = CommandCategory.USERS_ACTIVE
            aliases = listOf("shlo")
            callback { openLastStoragePage(lastStorageType) }
        }
    }

    private fun handleStorage(event: MessageSendToServerEvent, message: String, storageType: StorageType) {
        val commandPrefix = when (storageType) {
            StorageType.BACKPACK -> arrayOf("/backpack", "/bp")
            StorageType.ENDER_CHEST -> arrayOf("/enderchest", "/ec")
        }

        if (!commandPrefix.any { message.startsWith(it) }) {
            return
        }

        if (message.startsWith("${commandPrefix[0]} -") ||
            message.startsWith("${commandPrefix[1]} -")
        ) {
            event.isCanceled = true
            // Opener function will check whether previous value != null
            openLastStoragePage(storageType)
            return
        }

        val parts = message.split(" ")
        val lastStorageVariable = when (storageType) {
            StorageType.BACKPACK -> ::lastBackpack
            StorageType.ENDER_CHEST -> ::lastEnderChest
        }

        if (parts.size <= 1) {
            // No argument means open the first page of the respective storage
            lastStorageVariable.set(1)
            lastStorageType = storageType
            return
        }

        val intArg = parts[1].toIntOrNull()
        if (intArg != null) {
            lastStorageVariable.set(
                when (storageType) {
                    // "/bp 0" still is a valid command (leads to the overview menu)
                    StorageType.BACKPACK -> if (intArg < 0 || intArg > 18) null else intArg
                    StorageType.ENDER_CHEST -> if (intArg < 1 || intArg > 9) null else intArg
                },
            )
            lastStorageType = storageType
        }

        return
    }
}
