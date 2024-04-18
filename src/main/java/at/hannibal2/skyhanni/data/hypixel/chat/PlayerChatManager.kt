package at.hannibal2.skyhanni.data.hypixel.chat

import at.hannibal2.skyhanni.data.hypixel.chat.event.PlayerShowItemChatEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.groupOrNull
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.StringUtils.matchMatcher
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class PlayerChatManager {

    private val patternGroup = RepoPattern.group("data.chat.player")

    /**
     * REGEX-TEST: §8[§r§6428§r§8] §r§b[MVP§5+§b] Alea1337§f: t
     * REGEX-TEST: §8[§r§e102§r§8] §r§7☠ §r§b[MVP§d+§b] cobyjoey§f§r§f: first person to type "halo0011 is my favorite player on the game I love halo0011!!!"
     * REGEX-TEST: §8[§r§5396§r§8] §r§7☢ §r§b[MVP§c+§b] hannibal2§f: hello
     * REGEX-TEST: §8[§r§e97§r§8] §r§7☃ §r§7Tambaloo§7§r§7: i did capital i
     * REGEX-TEST: §8[§r§f76§r§8] §r§7❂ §r§a[VIP] Asymmetrically§f§r§f: i need to put on my necron
     * REGEX-TEST: §8[§r§c446§r§8] §r§b§l⚛ §r§6[MVP§1++§6] XueRuu§f§r§f: TROPHY FISH! You caught a Lavahorse DIAMOND.
     * REGEX-TEST: §b[MVP§c+§b] hannibal2§f: test
     */
    private val globalPattern by patternGroup.pattern(
        "global",
        "(?:§8\\[§r(?<levelColor>§.)(?<level>\\d+)§r§8] §r)?(?<author>§.+)(?:§f|§7§r§7): (?<message>.*)"
    )

    /**
     * REGEX-TEST: §9Party §8> §b§l⚛ §b[MVP§f+§b] Dankbarkeit§f: §rx: -190, y: 5, z: -163
     * REGEX-TEST: §9Party §8> §6⚔ §6[MVP§3++§6] RealBacklight§f: §r!warp
     * REGEX-TEST: §9Party §8> §b[MVP§3+§b] Eisengolem§f: §r!pt
     */
    private val partyPattern by patternGroup.pattern(
        "party",
        "§9Party §8> (?<author>[^:]*): §r(?<message>.*)"
    )

    /**
     * REGEX-TEST: §b[MVP§c+§b] hannibal2§f§7 is holding §r§8[§6Heroic Aspect of the Void§8]
     * REGEX-TEST: §b[MVP§c+§b] hannibal2§f§7 is holding §r§8[§7[Lvl 2] §dSpider§8]
     * REGEX-TEST: §b[MVP§c+§b] hannibal2§f§7 is friends with a §r§8[§7[Lvl 200] §8[§6103§8§4✦§8] §6Golden Dragon§8]
     * REGEX-TEST: §b[MVP§c+§b] hannibal2§f§7 is wearing §r§8[§5Glistening Implosion Belt§8]
     * REGEX-TEST: §b[MVP§c+§b] hannibal2§f§7 is friends with a §r§8[§7[Lvl 100] §dEnderman§8]
     * REGEX-TEST: §b[MVP§c+§b] hannibal2§f§7 has §r§8[§6Heroic Aspect of the Void§8]
     * REGEX-TEST: §8[§5396§8] §7☢ §r§b[MVP§c+§b] hannibal2§f§7 is holding §r§8[§6Buzzing InfiniVacuum™ Hooverius§8]
     */
    private val itemShowPattern by patternGroup.pattern(
        "party",
        "(?:§8\\[(?<levelColor>§.)(?<level>\\d+)§8] )?(?<author>.*)§f§7 (?<action>is (?:holding|friends with a|wearing)|has) §r(?<itemName>.*)"
    )

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        itemShowPattern.matchMatcher(event.message) {
            val levelColor = groupOrNull("levelColor")
            val level = groupOrNull("level")?.formatInt()
            val author = group("author")
            val action = group("action")
            val itemName = group("itemName")

            // for consistency
            val message = "§7$action §r$itemName"
            PlayerShowItemChatEvent(levelColor, level, author, message, action, itemName).postChat(event)
        }
        globalPattern.matchMatcher(event.message) {
            val author = group("author")
            val message = LorenzUtils.stripVanillaMessage(group("message"))
            if (author.contains("[NPC]")) {
                NpcChatEvent(author, message.removePrefix("§f")).postChat(event)
                NpcChatEvent(author, message.removePrefix("§f")).postChat(event)
            } else {
                val levelColor = groupOrNull("levelColor")
                val level = groupOrNull("level")?.formatInt()
                PlayerAllChatEvent(levelColor, level, author, message).postChat(event)
            }
            return
        }
        partyPattern.matchMatcher(event.message) {
            val author = group("author")
            val message = group("message")
            PartyChatEvent(author, message).postChat(event)
            return
        }
    }

    private fun AbstractChatEvent.postChat(event: LorenzChatEvent) {
        if (postAndCatch()) {
            event.cancel()
        }
        blockedReason?.let {
            event.blockedReason = it
        }
    }
}
