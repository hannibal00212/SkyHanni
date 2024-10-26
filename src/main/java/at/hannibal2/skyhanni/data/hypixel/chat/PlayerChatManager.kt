package at.hannibal2.skyhanni.data.hypixel.chat

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.hypixel.chat.event.AbstractChatEvent
import at.hannibal2.skyhanni.data.hypixel.chat.event.CoopChatEvent
import at.hannibal2.skyhanni.data.hypixel.chat.event.GuildChatEvent
import at.hannibal2.skyhanni.data.hypixel.chat.event.NpcChatEvent
import at.hannibal2.skyhanni.data.hypixel.chat.event.PartyChatEvent
import at.hannibal2.skyhanni.data.hypixel.chat.event.PlayerAllChatEvent
import at.hannibal2.skyhanni.data.hypixel.chat.event.PlayerShowItemChatEvent
import at.hannibal2.skyhanni.data.hypixel.chat.event.PrivateMessageChatEvent
import at.hannibal2.skyhanni.data.hypixel.chat.event.SystemMessageEvent
import at.hannibal2.skyhanni.events.SkyHanniChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ComponentMatcher
import at.hannibal2.skyhanni.utils.ComponentMatcherUtils.intoSpan
import at.hannibal2.skyhanni.utils.ComponentMatcherUtils.matchStyledMatcher
import at.hannibal2.skyhanni.utils.ComponentSpan
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.util.IChatComponent

/**
 * Reading normal chat events, and splitting them up into many different player chat events, with all available extra information
 */
@SkyHanniModule
object PlayerChatManager {

    private val patternGroup = RepoPattern.group("data.chat.player")

    /**
     * REGEX-TEST: [58] §7nea89o§7: haiiiii
     * REGEX-TEST: [266] ♫ §b[MVP§d+§b] lrg89§f: a
     * REGEX-TEST: [302] ♫ [MVP+] lrg89: problematic
     */
    private val globalPattern by patternGroup.pattern(
        "global",
        "(?:\\[(?<level>\\d+)] )?(?<author>.+?)(?<chatColor>§f|§7|): (?<message>.*)"
    )

    /**
     * REGEX-TEST: §9Party §8> §b[MVP§d+§b] lrg89§f: peee
     * REGEX-TEST: §9Party §8> §7nea89o§f: peee
     */
    private val partyPattern by patternGroup.pattern(
        "party",
        "§9Party §8> (?<author>[^:]*)§f: (?<message>.*)"
    )

    /**
     * REGEX-TEST: §bCo-op > §7nea89o§f: hallooooo
     */
    private val coopPattern by patternGroup.pattern(
        "coop",
        "§bCo-op > (?<author>[^:]+)§f: (?<message>.*)"
    )

    /**
     * REGEX-TEST: §2Guild > §b[MVP§d+§b] infave §e[Em]§f: CEMENT DRINKERS INCORPORATED
     * REGEX-TEST: §2Guild > §6⚔ §6[MVP§3++§6] RealBacklight§f: !warp
     * REGEX-TEST: §2Guild > §b[MVP§d+§b] lrg89 §e[Iron]§f: h
     */
    private val guildPattern by patternGroup.pattern(
        "guild",
        "§2Guild > (?<author>.+?) ?(?<guildRank>§e\\[\\w*])?§f: (?<message>.*)",
    )

    /**
     * REGEX-TEST: To nea89o: lol
     * REGEX-TEST: From nea89o: hiii
     * REGEX-TEST: From stash: Pufferfish
     * REGEX-TEST: From stash: Wheat
     * REGEX-TEST: To [MVP+] Eisengolem: Boop!
     * REGEX-TEST: From [MVP+] Eisengolem: Boop!
     * REGEX-TEST: To [MVP+] Eisengolem: danke
     */
    private val privateMessagePattern by patternGroup.pattern(
        "privatemessage",
        "^(?!From stash: )(?<direction>From|To) (?<author>[^:]*): (?<message>.*)"
    )

    /**
     * REGEX-TEST: §b[MVP§c+§b] hannibal2§f§7 is holding §8[§6Heroic Aspect of the Void§8]
     * REGEX-TEST: §b[MVP§c+§b] hannibal2§f§7 is holding §8[§7[Lvl 2] §dSpider§8]
     * REGEX-TEST: §b[MVP§c+§b] hannibal2§f§7 is friends with a §8[§7[Lvl 200] §8[§6103§8§4✦§8] §6Golden Dragon§8]
     * REGEX-TEST: §b[MVP§c+§b] hannibal2§f§7 is wearing §8[§5Glistening Implosion Belt§8]
     * REGEX-TEST: §b[MVP§c+§b] hannibal2§f§7 is friends with a §8[§7[Lvl 100] §dEnderman§8]
     * REGEX-TEST: §b[MVP§c+§b] hannibal2§f§7 has §8[§6Heroic Aspect of the Void§8]
     * REGEX-TEST: §8[§b209§8] §b[MVP§d+§b] lrg89§f§7 is holding §8[§5Heroic Aspect of the Void§8]
     */
    @Suppress("MaxLineLength")
    private val itemShowPattern by patternGroup.pattern(
        "itemshow",
        "(?:§8\\[(?<levelColor>§.)(?<level>\\d+)§8] )?(?<author>.*)§f§7 (?<action>is (?:holding|friends with a|wearing)|has) (?<itemName>.*)"
    )

    /**
     * REGEX-TEST: ♫ §c[Buddy ツ] §b[MVP§d+§b] lrg89
     * REGEX-TEST: ℻ §b[MVP§5+§b] Alea1337
     */
    private val privateIslandRankPattern by patternGroup.pattern(
        "privateislandrank",
        "(?<prefix>.*?)(?<privateIslandRank>§.\\[(?!MVP(§.\\++)?§.]|VIP\\+*|YOU§.TUBE|ADMIN|MOD|GM)[^]]+\\]) (?<suffix>.*)"
    )

    /**
     * REGEX-TEST: ♫ §a[✌] §f[Gamer] §b[MVP§d+§b] lrg89
     * REGEX-TEST: ℻ §b[MVP§5+§b] Alea1337
     * REGEX-TEST: ♫ §a[✌] §c[Buddy ツ] §b[MVP§d+§b] lrg89
     */
    private val privateIslandGuestPattern by patternGroup.pattern(
        "privateislandguest",
        "(?<prefix>.*)(?<guest>§a\\[✌] )(?<suffix>.*)"
    )

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        val chatComponent = event.chatComponent.intoSpan().stripHypixelMessage()
        coopPattern.matchStyledMatcher(chatComponent) {
            val author = groupOrThrow("author")
            val message = groupOrThrow("message")
            CoopChatEvent(author, message, event.chatComponent).postChat(event)
            return
        }
        partyPattern.matchStyledMatcher(chatComponent) {
            PartyChatEvent(groupOrThrow("author"), groupOrThrow("message"), event.chatComponent)
                .postChat(event)
            return
        }
        guildPattern.matchStyledMatcher(chatComponent) {
            GuildChatEvent(
                groupOrThrow("author"),
                groupOrThrow("message"),
                group("guildRank"),
                event.chatComponent
            ).postChat(event)
            return
        }
        privateMessagePattern.matchStyledMatcher(chatComponent) {
            val direction = groupOrThrow("direction").getText()
            val author = groupOrThrow("author")
            val message = groupOrThrow("message")
            PrivateMessageChatEvent(direction, author, message, event.chatComponent).postChat(event)
            return
        }
        itemShowPattern.matchStyledMatcher(chatComponent) {
            val level = group("level")
            val author = groupOrThrow("author")
            val action = groupOrThrow("action")
            val itemName = groupOrThrow("itemName")

            PlayerShowItemChatEvent(
                level,
                action,
                author,
                itemName,
                author + action + itemName,
                event.chatComponent
            ).postChat(event)
            return
        }
        globalPattern.matchStyledMatcher(chatComponent) {
            if (isGlobalChat(event)) return
        }

        sendSystemMessage(event)
    }

    private fun ComponentMatcher.isGlobalChat(event: SkyHanniChatEvent): Boolean {
        var author = groupOrThrow("author")
        val message = groupOrThrow("message").removePrefix("§f")
        if (author.getText().contains("[NPC]")) {
            NpcChatEvent(author, message, event.chatComponent).postChat(event)
            return true
        }

        var privateIslandRank: ComponentSpan? = null
        var privateIslandGuest: ComponentSpan? = null
        if (IslandType.PRIVATE_ISLAND.isInIsland() || IslandType.PRIVATE_ISLAND_GUEST.isInIsland()) {
            privateIslandGuestPattern.matchStyledMatcher(author) {
                privateIslandGuest = groupOrThrow("guest")
                val prefix = groupOrThrow("prefix")
                val suffix = groupOrThrow("suffix")
                author = prefix + suffix
            }
            privateIslandRankPattern.matchStyledMatcher(author) {
                privateIslandRank = groupOrThrow("privateIslandRank")
                val prefix = groupOrThrow("prefix")
                val suffix = groupOrThrow("suffix")
                author = prefix + suffix
            }
        }

        PlayerAllChatEvent(
            levelComponent = group("level"),
            privateIslandRank = privateIslandRank,
            privateIslandGuest = privateIslandGuest,
            chatColor = groupOrThrow("chatColor").getText(),
            authorComponent = author,
            messageComponent = message,
            chatComponent = event.chatComponent,
        ).postChat(event)
        return true
    }

    private fun sendSystemMessage(event: SkyHanniChatEvent) {
        with(SystemMessageEvent(event.message, event.chatComponent)) {
            post()
            event.handleChat(blockedReason, chatComponent)
        }
    }

    private fun AbstractChatEvent.postChat(event: SkyHanniChatEvent) {
        post()
        event.handleChat(blockedReason, chatComponent)
    }

    private fun SkyHanniChatEvent.handleChat(
        blockedReason: String?,
        chatComponent: IChatComponent,
    ) {
        blockedReason?.let {
            this.blockedReason = it
        }
        this.chatComponent = chatComponent
    }
}
