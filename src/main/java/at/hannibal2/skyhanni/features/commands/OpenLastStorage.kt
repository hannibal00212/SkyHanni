package at.hannibal2.skyhanni.features.commands

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.MessageSendToServerEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ChatUtils.senderIsSkyhanni
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.NumberUtil.formatIntOrUserError

@SkyHanniModule
object OpenLastStorage {

    private val config get() = SkyHanniMod.feature.misc.lastStorage
    private val storage get() = ProfileStorageData.profileSpecific?.lastStorage

    private var lastStorageType: StorageType
        get() = storage?.type ?: StorageType.ENDER_CHEST
        set(value) { storage?.type = value }

    private var lastStoragePage: Int?
        get() = storage?.page
        set(value) { storage?.page = value }

    enum class StorageType(private val validPages: IntRange, val runCommand: (Int) -> Unit, vararg val commands: String) {
        ENDER_CHEST(1..9, { HypixelCommands.enderChest(it) }, "/enderchest", "/ec"),
        BACKPACK(0..18, { HypixelCommands.backPack(it) }, "/backpack", "/bp"),
        ;

        val storageName = name.lowercase().replace("_", " ")
        fun isValidPage(page: Int) = page in validPages

        companion object {
            fun fromCommand(command: String): StorageType? {
                return entries.find { command in it.commands }
            }
        }
    }

    private fun openLastStoragePage(type: StorageType) {
        lastStoragePage?.let { type.runCommand(it) } ?: ChatUtils.sendMessageToServer("/${config.fallbackCommand}")

        val message = lastStoragePage?.let { page ->
            "Opened last ${type.storageName} $page."
        } ?: "No last ${type.storageName} found. Running /${config.fallbackCommand}."
        ChatUtils.chat(message)
    }

    @HandleEvent
    fun onMessageSendToServer(event: MessageSendToServerEvent) {
        if (!isEnabled()) return
        if (event.senderIsSkyhanni()) return
        val args = event.message.lowercase().split(" ")
        val type = StorageType.fromCommand(args[0]) ?: return

        if (handleStorage(args, type)) {
            event.cancel()
        }
    }

    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.register("shlastopened") {
            description = "Opens the storage page last accessed by either /ec or /bp"
            category = CommandCategory.USERS_ACTIVE
            aliases = listOf("shlo")
            callback {
                if (isEnabled()) {
                    openLastStoragePage(lastStorageType)
                } else {
                    ChatUtils.chatAndOpenConfig(
                        "This feature is disabled, enable it in the config if you want to use it.",
                        config::openLastStorage,
                    )
                }
            }
        }
    }

    private fun handleStorage(args: List<String>, type: StorageType): Boolean {
        if (args.getOrNull(1) == "-") {
            openLastStoragePage(type)
            return true
        }

        if (args.size <= 1) {
            // No argument means open the first page of the respective storage
            lastStoragePage = 1
        } else {
            val pageNumber = args[1].formatIntOrUserError() ?: return false
            lastStoragePage = pageNumber.takeIf { type.isValidPage(it) }
        }
        lastStorageType = type
        return false
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(71, "misc.openLastStorage", "misc.lastStorage.openLastStorage")
    }

    private fun isEnabled() = config.openLastStorage
}
