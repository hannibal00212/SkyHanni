package at.hannibal2.skyhanni.features.commands

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.FriendAPI
import at.hannibal2.skyhanni.data.PartyAPI
import at.hannibal2.skyhanni.data.PartyAPI.partyLeader
import at.hannibal2.skyhanni.data.PartyAPI.transferVoluntaryPattern
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.MessageSendToServerEvent
import at.hannibal2.skyhanni.features.misc.limbo.LimboTimeTracker
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.StringUtils.trimWhiteSpace
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object PartyCommands {

    private val config get() = SkyHanniMod.feature.misc.commands

    fun kickOffline() {
        if (!config.shortCommands) return
        if (PartyAPI.partyMembers.isEmpty()) return
        HypixelCommands.partyKickOffline()
    }

    fun disband() {
        if (!config.shortCommands) return
        if (PartyAPI.partyMembers.isEmpty()) return
        HypixelCommands.partyDisband()
    }

    fun warp() {
        if (!config.shortCommands) return
        if (PartyAPI.partyMembers.isEmpty()) return
        HypixelCommands.partyWarp()
    }

    fun kick(args: Array<String>) {
        if (!config.shortCommands) return
        if (PartyAPI.partyMembers.isEmpty()) return
        if (args.isEmpty()) return
        val kickedPlayer = args[0]
        val kickedReason = args.drop(1).joinToString(" ").trim()
        if (kickedReason.isNotEmpty() && config.partyKickReason) {
            HypixelCommands.partyChat("Kicking $kickedPlayer: $kickedReason")
        }
        HypixelCommands.partyKick(kickedPlayer)
    }

    fun transfer(args: Array<String>) {
        if (args.isEmpty()) {
            if (LimboTimeTracker.inLimbo) {
                LimboTimeTracker.printStats(true)
                return
            }
            HypixelCommands.playtime()
            return
        }
        if (!config.shortCommands) return
        if (PartyAPI.partyMembers.isEmpty()) return
        HypixelCommands.partyTransfer(args[0])
    }

    fun promote(args: Array<String>) {
        if (!config.shortCommands) return
        if (PartyAPI.partyMembers.isEmpty()) return
        if (args.isEmpty()) return
        HypixelCommands.partyPromote(args[0])
    }

    fun reverseTransfer() {
        if (!config.reversePT.command) return
        if (PartyAPI.partyMembers.isEmpty()) return
        val prevPartyLeader = PartyAPI.prevPartyLeader ?: return

        autoPartyTransfer(prevPartyLeader)
    }

    private fun autoPartyTransfer(prevPartyLeader: String) {
        HypixelCommands.partyTransfer(prevPartyLeader)
        config.reversePT.message?.let {
            if (it.isNotBlank()) {
                HypixelCommands.partyChat(it)
            }
        }
    }

    @SubscribeEvent
    fun onMessageSendToServer(event: MessageSendToServerEvent) {
        if (!config.partyKickReason) {
            return
        }
        if (!event.message.startsWith("/party kick ", ignoreCase = true) && !event.message.startsWith("/p kick ", ignoreCase = true)) {
            return
        }
        val args = event.message.substringAfter("kick").trim().split(" ")
        if (args.isEmpty()) return
        val kickedPlayer = args[0]
        val kickReason = args.drop(1).joinToString(" ").trim()
        if (kickReason.isEmpty()) return
        event.cancel()
        HypixelCommands.partyChat("Kicking $kickedPlayer: $kickReason")
        HypixelCommands.partyKick(kickedPlayer)
    }

    fun customTabComplete(command: String): List<String>? {
        if (command == "pk" || command == "pt" || command == "pp" && config.shortCommands) {
            return PartyAPI.partyMembers
        }

        if (command == "p" || command == "party") {
            val friends = if (config.tabComplete.friends) {
                FriendAPI.getAllFriends().filter { it.bestFriend || !config.tabComplete.onlyBestFriends }.map { it.name }
            } else {
                emptyList<String>()
            }
            val allOnLobby = EntityUtils.getPlayerEntities().map { it.name }
            return friends + getPartyCommands() + allOnLobby
        }
        return null
    }

    private fun getPartyCommands(): List<String> {
        return if (config.tabComplete.partyCommands && PartyAPI.partyMembers.isNotEmpty()) {
            otherPartyCommands
        } else emptyList()
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(5, "commands.usePartyTransferAlias", "commands.shortCommands")

        event.move(31, "commands", "misc.commands")
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    fun onChat(event: LorenzChatEvent) {
        if (!config.reversePT.clickable) return
        if (!transferVoluntaryPattern.matches(event.message.trimWhiteSpace().removeColor())) return
        if (partyLeader != LorenzUtils.getPlayerName()) return

        val prevPartyLeader = PartyAPI.prevPartyLeader ?: return
        event.blockedReason = "replacing"

        ChatUtils.clickableChat(
            event.message,
            onClick = { autoPartyTransfer(prevPartyLeader) },
            prefix = false,
        )
    }
}

private val otherPartyCommands = listOf(
    "Disband",
    "KickOffline",
    "Leave",
    "List",
    "Mute",
    "Private",
    "Warp",
    "Settings",
)
