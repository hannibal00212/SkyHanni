package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.data.hypixel.chat.event.PartyChatEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.OSUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.StringUtils.cleanPlayerName
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.StringUtils.removeResets
import at.hannibal2.skyhanni.utils.StringUtils.trimWhiteSpace
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.random.Random

@SkyHanniModule
object PartyAPI {

    private val patternGroup = RepoPattern.group("data.party")
    private val youJoinedPartyPattern by patternGroup.pattern(
        "you.joined",
        "§eYou have joined (?<name>.*)'s? §eparty!",
    )
    private val othersJoinedPartyPattern by patternGroup.pattern(
        "others.joined",
        "(?<name>.*) §ejoined the party\\.",
    )
    private val othersInThePartyPattern by patternGroup.pattern(
        "others.inparty",
        "§eYou'll be partying with: (?<names>.*)",
    )
    private val otherLeftPattern by patternGroup.pattern(
        "others.left",
        "(?<name>.*) §ehas left the party\\.",
    )
    private val otherKickedPattern by patternGroup.pattern(
        "others.kicked",
        "(?<name>.*) §ehas been removed from the party\\.",
    )
    private val otherOfflineKickedPattern by patternGroup.pattern(
        "others.offline",
        "§eKicked (?<name>.*) because they were offline\\.",
    )
    private val otherDisconnectedPattern by patternGroup.pattern(
        "others.disconnect",
        "(?<name>.*) §ewas removed from your party because they disconnected\\.",
    )
    private val transferOnLeavePattern by patternGroup.pattern(
        "others.transfer.leave",
        "The party was transferred to (?<newowner>.*) because (?<name>.*) left",
    )
    val transferVoluntaryPattern by patternGroup.pattern(
        "others.transfer.voluntary",
        "The party was transferred to (?<newowner>.*) by (?<name>.*)",
    )
    private val disbandedPattern by patternGroup.pattern(
        "others.disband",
        ".* §ehas disbanded the party!",
    )
    private val kickedPattern by patternGroup.pattern(
        "you.kicked",
        "§eYou have been kicked from the party by .* §e",
    )
    private val partyMembersStartPattern by patternGroup.pattern(
        "members.start",
        "§6Party Members \\(\\d+\\)",
    )
    private val partyMemberListPattern by patternGroup.pattern(
        "members.list.withkind",
        "Party (?<kind>Leader|Moderators|Members): (?<names>.*)",
    )
    private val kuudraFinderJoinPattern by patternGroup.pattern(
        "kuudrafinder.join",
        "§dParty Finder §f> (?<name>.*?) §ejoined the group! \\(§[a-fA-F0-9]+Combat Level \\d+§e\\)",
    )
    private val dungeonFinderJoinPattern by patternGroup.pattern(
        "dungeonfinder.join",
        "§dParty Finder §f> (?<name>.*?) §ejoined the dungeon group! \\(§[a-fA-F0-9].* Level \\d+§[a-fA-F0-9]\\)",
    )

    val partyMembers = mutableListOf<String>()

    var partyLeader: String? = null
    var prevPartyLeader: String? = null

    fun listMembers() {
        val size = partyMembers.size
        if (size == 0) {
            ChatUtils.chat("No tracked party members!")
            return
        }
        ChatUtils.chat("Tracked party members §7($size) §f:", prefixColor = "§a")
        for (member in partyMembers) {
            ChatUtils.chat(" §a- §7$member" + if (partyLeader == member) " §a(Leader)" else "", false)
        }

        if (partyLeader == LorenzUtils.getPlayerName()) {
            ChatUtils.chat("§aYou are leader")
        }

        if (Random.nextDouble() < 0.1) {
            OSUtils.openBrowser("https://www.youtube.com/watch?v=iANP7ib7CPA")
            ChatUtils.hoverableChat("§7Are You Ready To Party?", listOf("§b~Spongebob"), prefix = false)
        }
    }

    @SubscribeEvent
    fun onPartyChat(event: PartyChatEvent) {
        val name = event.author.cleanPlayerName()
        addPlayer(name)
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        val message = event.message.trimWhiteSpace().removeResets()

        // new member joined
        youJoinedPartyPattern.matchMatcher(message) {
            val name = group("name").cleanPlayerName()
            partyLeader = name
            addPlayer(name)
        }
        othersJoinedPartyPattern.matchMatcher(message) {
            val name = group("name").cleanPlayerName()
            if (partyMembers.isEmpty()) {
                partyLeader = LorenzUtils.getPlayerName()
            }
            addPlayer(name)
        }
        othersInThePartyPattern.matchMatcher(message) {
            for (name in group("names").split(", ")) {
                addPlayer(name.cleanPlayerName())
            }
        }
        kuudraFinderJoinPattern.matchMatcher(message) {
            val name = group("name").cleanPlayerName()
            addPlayer(name)
        }
        dungeonFinderJoinPattern.matchMatcher(message) {
            val name = group("name").cleanPlayerName()
            addPlayer(name)
        }

        // one member got removed
        otherLeftPattern.matchMatcher(message) {
            val name = group("name").cleanPlayerName()
            removeWithLeader(name)
        }
        otherKickedPattern.matchMatcher(message) {
            val name = group("name").cleanPlayerName()
            removeWithLeader(name)
        }
        otherOfflineKickedPattern.matchMatcher(message) {
            val name = group("name").cleanPlayerName()
            removeWithLeader(name)
        }
        otherDisconnectedPattern.matchMatcher(message) {
            val name = group("name").cleanPlayerName()
            partyMembers.remove(name)
        }
        transferOnLeavePattern.matchMatcher(message.removeColor()) {
            val name = group("name").cleanPlayerName()
            partyLeader = group("newowner").cleanPlayerName()
            partyMembers.remove(name)
        }
        transferVoluntaryPattern.matchMatcher(message.removeColor()) {
            partyLeader = group("newowner").cleanPlayerName()
            prevPartyLeader = group("name").cleanPlayerName()
        }

        // party disbanded
        disbandedPattern.matchMatcher(message) {
            partyLeft()
        }
        kickedPattern.matchMatcher(message) {
            partyLeft()
        }
        if (message == "§eYou left the party." ||
            message == "§cThe party was disbanded because all invites expired and the party was empty." ||
            message == "§cYou are not currently in a party." ||
            message == "§cYou are not in a party."
        ) {
            partyLeft()
        }

        // party list
        partyMembersStartPattern.matchMatcher(message.removeResets()) {
            partyMembers.clear()
        }

        partyMemberListPattern.matchMatcher(message.removeColor()) {
            val kind = group("kind")
            val isPartyLeader = kind == "Leader"
            for (name in group("names").split(" ● ")) {
                val playerName = name.replace(" ●", "").cleanPlayerName()
                addPlayer(playerName)
                if (isPartyLeader) {
                    partyLeader = playerName
                }
            }
        }
    }

    private fun removeWithLeader(name: String) {
        partyMembers.remove(name)
        if (name == prevPartyLeader) {
            prevPartyLeader = null
        }
    }

    private fun addPlayer(playerName: String) {
        if (partyMembers.contains(playerName)) return
        if (playerName == LorenzUtils.getPlayerName()) return
        partyMembers.add(playerName)
    }

    private fun partyLeft() {
        partyMembers.clear()
        partyLeader = null
        prevPartyLeader = null
    }
}
