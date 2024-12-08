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
    private enum class StorageType(val commands: List<String>) {
        ENDER_CHEST(listOf("/enderchest", "/ec")),
        BACKPACK(listOf("/backpack", "/bp")),
        ;

        companion object {
            fun fromCommand(command: String): StorageType? {
                return entries.find { command in it.commands }
            }
        }
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
        val lastStorageVariable = when (storageType) {
            StorageType.BACKPACK -> ::lastBackpack
            StorageType.ENDER_CHEST -> ::lastEnderChest
        }

        if (args.size <= 1) {
            // No argument means open the first page of the respective storage
            lastStorageVariable.set(1)
            lastStorageType = storageType
            return false
        }

        val intArg = args[1].toIntOrNull() ?: return false

        lastStorageVariable.set(
            when (storageType) {
                // "/bp 0" still is a valid command (leads to the overview menu)
                StorageType.BACKPACK -> if (intArg < 0 || intArg > 18) null else intArg
                StorageType.ENDER_CHEST -> if (intArg < 1 || intArg > 9) null else intArg
            },
        )
        lastStorageType = storageType

        return false
    }
}
