package at.hannibal2.skyhanni.features.commands

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.features.misc.PartyCommandsConfig
import at.hannibal2.skyhanni.data.FriendAPI
import at.hannibal2.skyhanni.data.PartyAPI
import at.hannibal2.skyhanni.data.hypixel.chat.event.PartyChatEvent
import at.hannibal2.skyhanni.events.TabCompletionEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object PartyChatCommands {

    private val config get() = SkyHanniMod.feature.misc.partyCommands
    private val storage get() = SkyHanniMod.feature.storage

    data class PartyChatCommand(
        val names: List<String>,
        val isEnabled: () -> Boolean,
        val requiresPartyLead: Boolean,
        val executable: (PartyChatEvent) -> Unit,
    )

    private var lastWarp = SimpleTimeMark.farPast()
    private var lastAllInvite = SimpleTimeMark.farPast()

    private val allPartyCommands = listOf(
        PartyChatCommand(
            listOf("pt", "ptme", "transfer"),
            { config.transferCommand },
            requiresPartyLead = true,
            executable = {
                HypixelCommands.partyTransfer(it.cleanedAuthor)
            }
        ),
        PartyChatCommand(
            listOf("pw", "warp", "warpus"),
            { config.warpCommand && lastWarp.passedSince() > 5.seconds },
            requiresPartyLead = true,
            executable = {
                lastWarp = SimpleTimeMark.now()
                HypixelCommands.partyWarp()
            }
        ),
        PartyChatCommand(
            listOf("allinv", "allinvite"),
            { config.allInviteCommand && lastAllInvite.passedSince() > 2.seconds },
            requiresPartyLead = true,
            executable = {
                lastAllInvite = SimpleTimeMark.now()
                HypixelCommands.partyAllInvite()
            }
        ),
    )

    private val indexedPartyChatCommands = buildMap {
        for (command in allPartyCommands) {
            for (name in command.names) {
                put(name.lowercase(), command)
            }
        }
    }

    private fun isTrustedUser(name: String): Boolean {
        val friend = FriendAPI.getAllFriends().find { it.name == name }
        return when (config.defaultRequiredTrustLevel) {
            PartyCommandsConfig.TrustedUser.FRIENDS -> friend != null
            PartyCommandsConfig.TrustedUser.BEST_FRIENDS -> friend?.bestFriend == true
            PartyCommandsConfig.TrustedUser.ANYONE -> true
            PartyCommandsConfig.TrustedUser.NO_ONE -> false
        }
    }

    private val commandPrefixes = ".!?".toSet()

    private fun isBlockedUser(name: String): Boolean {
        return storage.blacklistedUsers.any { it.equals(name, ignoreCase = true) }
    }

    @SubscribeEvent
    fun onPartyCommand(event: PartyChatEvent) {
        if (event.message.firstOrNull() !in commandPrefixes) return
        val commandLabel = event.message.substring(1).substringBefore(' ')
        val command = indexedPartyChatCommands[commandLabel.lowercase()] ?: return
        val name = event.cleanedAuthor

        if (name == LorenzUtils.getPlayerName()) return
        if (!command.isEnabled()) return
        if (command.requiresPartyLead && PartyAPI.partyLeader != LorenzUtils.getPlayerName()) return
        if (isBlockedUser(name)) {
            if (config.showIgnoredReminder) ChatUtils.clickableChat(
                "§cIgnoring chat command from ${event.author}. " +
                    "Stop ignoring them using /shignore remove <player> or click here!",
                onClick = { blacklistModify(event.author) },
                "§eClick to ignore ${event.author}!",
            )
            return
        }
        if (!isTrustedUser(name)) {
            if (config.showIgnoredReminder) {
                ChatUtils.chat(
                    "§cIgnoring chat command from $name. " +
                        "Change your party chat command settings or /friend (best) them.",
                )
            }
            return
        }
        command.executable(event)
    }

    @SubscribeEvent
    fun onTabComplete(event: TabCompletionEvent) {
        if (PartyAPI.partyLeader == null) return
        val prefix = event.fullText.firstOrNull() ?: return
        if (prefix !in commandPrefixes) return

        val commandText = event.fullText.substring(1)
        indexedPartyChatCommands.keys
            .filter { it.startsWith(commandText) }
            .map { "$prefix$it" }
            .forEach(event::addSuggestion)
    }

    /**
     * TODO use a utils function for add/remove/list/clear
     * function(args: Array<String>, list: List<String>, listName: String,
     * precondition(string): () -> Boolean, onAdd(string), onRemove(string), onList(list))
     */
    fun blacklist(input: Array<String>) {
        if (input.size !in 1..2) {
            ChatUtils.userError("Usage: /shignore <add/remove/list/clear> <name>")
            return
        }
        when (val firstArg = input[0]) {
            "add" -> {
                if (input.size != 2) {
                    ChatUtils.userError("Usage: /shignore <add/remove/list/clear> <name>")
                    return
                }
                if (isBlockedUser(input[1])) {
                    ChatUtils.userError("${input[1]} is already ignored!")
                } else blacklistModify(input[1])
            }

            "remove" -> {
                if (input.size != 2) {
                    ChatUtils.userError("Usage: /shignore <add/remove/list/clear> <name>")
                    return
                }
                if (!isBlockedUser(input[1])) {
                    ChatUtils.userError("${input[1]} isn't ignored!")
                } else blacklistModify(input[1])
            }

            "list" -> {
                if (input.size == 2) {
                    blacklistView(input[1])
                } else blacklistView()
            }

            "clear" -> {
                ChatUtils.clickableChat(
                    "Are you sure you want to do this? Click here to confirm.",
                    onClick = {
                        storage.blacklistedUsers.clear()
                        ChatUtils.chat("Cleared your ignored players list!")
                    },
                    "§eClick to confirm.",
                    oneTimeClick = true,
                )
            }

            else -> blacklistModify(firstArg)
        }
    }

    private fun blacklistModify(player: String) {
        if (player !in storage.blacklistedUsers) {
            ChatUtils.chat("§cNow ignoring §b$player§e!")
            storage.blacklistedUsers.add(player)
            return
        }
        ChatUtils.chat("§aStopped ignoring §b$player§e!")
        storage.blacklistedUsers.remove(player)
        return
    }

    private fun blacklistView() {
        val blacklist = storage.blacklistedUsers
        if (blacklist.size <= 0) {
            ChatUtils.chat("Your ignored players list is empty!")
            return
        }
        var message = "Ignored player list:"
        if (blacklist.size > 15) {
            message += "\n§e"
            blacklist.forEachIndexed { i, blacklistedMessage ->
                message += blacklistedMessage
                if (i < blacklist.size - 1) {
                    message += ", "
                }
            }
        } else {
            blacklist.forEach { message += "\n§e$it" }
        }
        ChatUtils.chat(message)
    }

    private fun blacklistView(player: String) {
        if (isBlockedUser(player)) {
            ChatUtils.chat("$player §ais §eignored.")
        } else {
            ChatUtils.chat("$player §cisn't §eignored.")
        }
    }
}
