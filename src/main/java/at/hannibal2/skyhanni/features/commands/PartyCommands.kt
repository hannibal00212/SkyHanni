package at.hannibal2.skyhanni.features.commands

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.data.PartyAPI
import at.hannibal2.skyhanni.data.PartyAPI.partyLeader
import at.hannibal2.skyhanni.data.PartyAPI.transferVoluntaryPattern
import at.hannibal2.skyhanni.events.MessageSendToServerEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.features.misc.limbo.LimboTimeTracker
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.StringUtils.trimWhiteSpace

@SkyHanniModule
object PartyCommands {

    private val config get() = SkyHanniMod.feature.misc.commands

    private fun kickOffline() {
        if (!config.shortCommands) return
        if (PartyAPI.partyMembers.isEmpty()) return
        HypixelCommands.partyKickOffline()
    }

    private fun disband() {
        if (!config.shortCommands) return
        if (PartyAPI.partyMembers.isEmpty()) return
        HypixelCommands.partyDisband()
    }

    private fun warp() {
        if (!config.shortCommands) return
        if (PartyAPI.partyMembers.isEmpty()) return
        HypixelCommands.partyWarp()
    }

    private fun kick(args: Array<String>) {
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

    private fun transfer(args: Array<String>) {
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

    private fun promote(args: Array<String>) {
        if (!config.shortCommands) return
        if (PartyAPI.partyMembers.isEmpty()) return
        if (args.isEmpty()) return
        HypixelCommands.partyPromote(args[0])
    }

    private fun reverseTransfer() {
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

    @HandleEvent
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
        return null
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(5, "commands.usePartyTransferAlias", "commands.shortCommands")

        event.move(31, "commands", "misc.commands")
    }

    @HandleEvent(priority = HandleEvent.LOW)
    fun onChat(event: SkyHanniChatEvent) {
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

    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.register("pko") {
            description = "Kicks offline party members"
            category = CommandCategory.SHORTENED_COMMANDS
            callback { kickOffline() }
        }
        event.register("pw") {
            description = "Warps your party"
            category = CommandCategory.SHORTENED_COMMANDS
            callback { warp() }
        }
        event.register("pk") {
            description = "Kick a specific party member"
            category = CommandCategory.SHORTENED_COMMANDS
            callback { kick(it) }
        }
        event.register("pt") {
            description = "Transfer the party to another party member"
            category = CommandCategory.SHORTENED_COMMANDS
            callback { transfer(it) }
        }
        event.register("pp") {
            description = "Promote a specific party member"
            category = CommandCategory.SHORTENED_COMMANDS
            callback { promote(it) }
        }
        event.register("pd") {
            description = "Disbands the party"
            category = CommandCategory.SHORTENED_COMMANDS
            callback { disband() }
        }
        event.register("rpt") {
            description = "Reverse transfer party to the previous leader"
            category = CommandCategory.SHORTENED_COMMANDS
            callback { reverseTransfer() }
        }
    }
}
