package at.hannibal2.skyhanni.features.commands

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.events.MessageSendToServerEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ChatUtils.senderIsSkyhanni
import at.hannibal2.skyhanni.utils.HypixelCommands

@SkyHanniModule
object OpenLastStorage {
    private enum class StorageType(val commands: List<String>, val validPages: IntRange, val openStorage: (Int) -> Unit) {
        ENDER_CHEST(listOf("/enderchest", "/ec"), 1..9, { HypixelCommands.openEnderChest(it) }),
        BACKPACK(listOf("/backpack", "/bp"), 0..18, { HypixelCommands.openBackpack(it) }),
        ;

        val storageName = name.lowercase().replace("_", " ")
        var lastPage: Int? = null
        fun isValidPage(page: Int) = page in validPages

        companion object {
            fun fromCommand(command: String): StorageType? {
                return entries.find { command in it.commands }
            }
        }
    }

    // Default to Ender Chest as last storage type, since every profile on any account has at least one partial ender chest page unlocked
    private var lastStorageType = StorageType.ENDER_CHEST

    private fun openLastStoragePage(storageType: StorageType) {

        storageType.lastPage?.let { storageType.openStorage(it) }

        val message = storageType.lastPage?.let { page ->
            "Opened last ${storageType.storageName} $page."
        } ?: "No last ${storageType.storageName} to open."
        ChatUtils.chat(message)
    }

    @HandleEvent
    fun onMessageSendToServer(event: MessageSendToServerEvent) {
        if (event.senderIsSkyhanni()) return
        val args = event.message.lowercase().split(" ")
        val storageType = StorageType.fromCommand(args[0]) ?: return

        if (handleStorage(args, storageType)) {
            event.cancel()
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

    private fun handleStorage(args: List<String>, storageType: StorageType): Boolean {
        if (args.size > 1 && args[1] == "-") {
            openLastStoragePage(storageType)
            return true
        }

        // No argument means open the first page of the respective storage
        if (args.size <= 1) {
            storageType.lastPage = 1
            lastStorageType = storageType
            return false
        }

        val intArg = args[1].toIntOrNull() ?: return false

        storageType.lastPage = if (storageType.isValidPage(intArg)) {
            intArg
        } else {
            null
        }

        lastStorageType = storageType
        return false
    }
}
