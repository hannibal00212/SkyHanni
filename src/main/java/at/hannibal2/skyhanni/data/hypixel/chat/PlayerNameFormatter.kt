package at.hannibal2.skyhanni.data.hypixel.chat

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.features.chat.PlayerMessagesConfig
import at.hannibal2.skyhanni.data.hypixel.chat.event.CoopChatEvent
import at.hannibal2.skyhanni.data.hypixel.chat.event.GuildChatEvent
import at.hannibal2.skyhanni.data.hypixel.chat.event.PartyChatEvent
import at.hannibal2.skyhanni.data.hypixel.chat.event.PlayerAllChatEvent
import at.hannibal2.skyhanni.data.hypixel.chat.event.PlayerShowItemChatEvent
import at.hannibal2.skyhanni.data.hypixel.chat.event.PrivateMessageChatEvent
import at.hannibal2.skyhanni.features.bingo.BingoAPI
import at.hannibal2.skyhanni.features.chat.playerchat.PlayerChatFilter
import at.hannibal2.skyhanni.features.misc.MarkedPlayerManager
import at.hannibal2.skyhanni.features.misc.compacttablist.AdvancedPlayerList
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils.changeColor
import at.hannibal2.skyhanni.utils.ComponentMatcherUtils.matchStyledMatcher
import at.hannibal2.skyhanni.utils.ComponentSpan
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.StringUtils
import at.hannibal2.skyhanni.utils.StringUtils.applyFormattingFrom
import at.hannibal2.skyhanni.utils.StringUtils.cleanPlayerName
import at.hannibal2.skyhanni.utils.StringUtils.toCleanChatComponent
import at.hannibal2.skyhanni.utils.chat.Text
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import com.google.gson.JsonArray
import com.google.gson.JsonNull
import net.minecraft.util.ChatComponentText
import net.minecraft.util.IChatComponent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * Listening to the player chat events, and applying custom chat options to them.
 * E.g. part order, rank hider, etc
 */
@SkyHanniModule
object PlayerNameFormatter {
    private val config get() = SkyHanniMod.feature.chat.playerMessage

    private val patternGroup = RepoPattern.group("data.chat.player.name")

    /**
     * REGEX-TEST: §7☢ §r§b[MVP§d+§b] hannibal2
     * REGEX-TEST: §7☢ §r§b[MVP§d+§b] hannibal2
     * REGEX-TEST: §7☢ §r§bhannibal2
     * REGEX-TEST: §7☢ §rhannibal2
     * REGEX-TEST: §7☢ §b[MVP§c+§b] hannibal2
     * REGEX-TEST: ♫ §b[MVP§d+§b] lrg89
     */
    private val emblemPattern by patternGroup.pattern(
        "emblem",
        "(?<emblem>(?:§.){0,2}.) (?<author>.*)"
    )

    @SubscribeEvent
    fun onPlayerAllChat(event: PlayerAllChatEvent) {
        if (!isEnabled()) return
        val levelColor = event.levelColor
        val levelComponent = event.levelComponent
        val message = event.messageComponent
        val authorComponent = event.authorComponent
        val privateIslandRank = event.privateIslandRank
        val privateIslandGuest = event.privateIslandGuest

        val shouldFilter = config.chatFilter && PlayerChatFilter.shouldChatFilter(message.intoComponent())
        val chatColor = if (shouldFilter) "§7" else if (config.sameChatColor) "§f" else event.chatColor

        val name = nameFormat(
            authorComponent,
            levelColor?.toString(),
            level = levelComponent,
            privateIslandRank = privateIslandRank,
            privateIslandGuest = privateIslandGuest
        )
        val all = ChatComponentText("")
        all.appendSibling(name)
        all.appendText(": ")
        all.appendSibling(chatColor.toCleanChatComponent())
        all.appendSibling(message.intoComponent())
        event.chatComponent = StringUtils.replaceIfNeeded(event.chatComponent, all) ?: return
    }

    @SubscribeEvent
    fun onCoopChat(event: CoopChatEvent) {
        if (!isEnabled()) return
        event.chatComponent = StringUtils.replaceIfNeeded(
            event.chatComponent,
            Text.text("§bCo-op > ") {
                appendSibling(nameFormat(event.authorComponent))
                appendText("§f: ")
                appendSibling(event.messageComponent.intoComponent())
            }
        ) ?: return
    }

    @SubscribeEvent
    fun onGuildChat(event: GuildChatEvent) {
        if (!isEnabled()) return
        event.chatComponent = StringUtils.replaceIfNeeded(
            event.chatComponent,
            Text.text("§2Guild > ") {
                appendSibling(nameFormat(event.authorComponent, guildRank = event.guildRank))
                appendText("§f: ")
                appendSibling(event.messageComponent.intoComponent())
            }
        ) ?: return
    }

    @SubscribeEvent
    fun onPartyChat(event: PartyChatEvent) {
        if (!isEnabled()) return
        event.chatComponent = StringUtils.replaceIfNeeded(
            event.chatComponent,
            Text.text("§9Party §8> ") {
                appendSibling(nameFormat(event.authorComponent))
                appendText("§f: ")
                appendSibling(event.messageComponent.intoComponent())
            }
        ) ?: return
    }

    @SubscribeEvent
    fun onPrivateChat(event: PrivateMessageChatEvent) {
        if (!isEnabled()) return
        event.chatComponent =
            StringUtils.replaceIfNeeded(event.chatComponent, Text.text("§d${event.direction}") {
                appendText(" ")
                appendSibling(nameFormat(event.authorComponent))
                appendText("§f: ")
                appendSibling(event.messageComponent.intoComponent())
            }) ?: return
    }

    @SubscribeEvent
    fun onPlayerShowItemChat(event: PlayerShowItemChatEvent) {
        if (!isEnabled()) return
        event.chatComponent = StringUtils.replaceIfNeeded(event.chatComponent, Text.text("") {
            appendSibling(
                nameFormat(
                    event.authorComponent,
                    levelColor = event.levelComponent?.sampleStyleAtStart()?.color?.toString(),
                    level = event.levelComponent
                )
            )

            appendText(" ")
            appendSibling(event.action.intoComponent().changeColor(LorenzColor.GRAY))

            appendText(" ")
            appendSibling(event.item.intoComponent())
        }) ?: return
    }

    private fun nameFormat(
        author: ComponentSpan,
        levelColor: String? = null,
        level: ComponentSpan? = null,
        guildRank: ComponentSpan? = null,
        privateIslandRank: ComponentSpan? = null,
        privateIslandGuest: ComponentSpan? = null,
    ): ChatComponentText {
        var cleanAuthor = cleanAuthor(author)

        var emblemFormat: IChatComponent? = null
        emblemPattern.matchStyledMatcher(author) {
            emblemFormat = componentOrThrow("emblem")
            cleanAuthor = groupOrThrow("author").stripHypixelMessage()
        }

        val name = formatAuthor(cleanAuthor.getText(), levelColor).applyFormattingFrom(cleanAuthor)
        val levelFormat = formatLevel(levelColor, level)
        val guildRankFormat = guildRank?.intoComponent()
        val privateIslandRankFormat = privateIslandRank?.intoComponent()
        val privateIslandGuestFormat = privateIslandGuest?.intoComponent()

        val cleanName = cleanAuthor.getText().cleanPlayerName()
        val (faction, ironman, bingo) = AdvancedPlayerList.tabPlayerData[cleanName]?.let {
            val faction = it.faction.icon?.trim()?.toCleanChatComponent()
            val ironman = if (it.ironman) "§7♲".toCleanChatComponent() else null
            val bingo = it.bingoLevel?.let { level -> BingoAPI.getBingoIcon(level).toCleanChatComponent() }
            listOf(faction, ironman, bingo)
        } ?: listOf(null, null, null)

        val map = mutableMapOf<PlayerMessagesConfig.MessagePart, IChatComponent?>()
        map[PlayerMessagesConfig.MessagePart.SKYBLOCK_LEVEL] = levelFormat
        map[PlayerMessagesConfig.MessagePart.EMBLEM] = emblemFormat
        map[PlayerMessagesConfig.MessagePart.PLAYER_NAME] = name
        map[PlayerMessagesConfig.MessagePart.CRIMSON_FACTION] = faction
        map[PlayerMessagesConfig.MessagePart.MODE_IRONMAN] = ironman
        map[PlayerMessagesConfig.MessagePart.BINGO_LEVEL] = bingo
        map[PlayerMessagesConfig.MessagePart.GUILD_RANK] = guildRankFormat
        map[PlayerMessagesConfig.MessagePart.PRIVATE_ISLAND_RANK] = privateIslandRankFormat
        map[PlayerMessagesConfig.MessagePart.PRIVATE_ISLAND_GUEST] = privateIslandGuestFormat

        val all = ChatComponentText("")
        var first = true
        for (text in config.partsOrder.mapNotNull { map[it] }) {
            if (first) {
                first = false
            } else {
                if (!all.unformattedText.endsWith(" ")) {
                    all.appendText(" ")
                }
            }
            all.appendSibling(text)
        }

        return all
    }

    private fun formatLevel(rawColor: String?, rawLevel: ComponentSpan?): IChatComponent? {
        val color = rawColor ?: return null
        val level = rawLevel?.getText() ?: error("level is null, color is not null")
        val levelData = "$color$level"
        val result = if (config.hideLevelBrackets) levelData else "§8[$levelData§8]"
        return result.applyFormattingFrom(rawLevel)
    }

    private fun cleanAuthor(author: ComponentSpan): ComponentSpan {
        // TODO: I don't think we even need to strip this ???
        return author.stripHypixelMessage().removePrefix("§f")
    }

    private fun formatAuthor(author: String, levelColor: String?): String {
        if (author.contains("ADMIN")) return author
        if (config.ignoreYouTube && author.contains("YOUTUBE")) return author

        var result = author.cleanPlayerName(displayName = true)
        levelColor?.let {
            if (config.useLevelColorForName) {
                val cleanPlayerName = author.cleanPlayerName()
                result = result.replace(cleanPlayerName, it + cleanPlayerName)
            }
        }

        return MarkedPlayerManager.replaceInChat(result)
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && config.enable

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.transform(41, "chat.PlayerMessagesConfig.partsOrder") { element ->
            val newList = JsonArray()
            for (entry in element.asJsonArray) {
                if (entry is JsonNull) continue
                if (entry.asString != "EMPTY_CHAR") {
                    newList.add(entry)
                }
            }
            newList
        }
    }
}
