package at.hannibal2.skyhanni.features.chat

import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object GuildMessages {

    private val patternGroup = RepoPattern.group("chat.guild")

    /**
     * REGEX-TEST: §f    §r§eToday:           §r§2403 Guild Experience
     * REGEX-TEST: §f    §r§eNov 20 2024:   §r§219069 Guild Experience
     */
    private val guildExpSummary by patternGroup.pattern(
        "exp.summary",
        "§f\\s+§r§e(?:Today|[A-Za-z]{3} \\d{1,2} \\d{4}):\\s+§r§2(?<gexp>\\d+)\\s+Guild Experience",
    )

    private const val guildExpStart = "§bGuild Exp Contributions:"
    private var isTrackingGuildExp = false
    private var totalGexp = 0
    private var counter = 0

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (event.message == guildExpStart) {
            isTrackingGuildExp = true
            totalGexp = 0
            counter = 0
            return
        }

        if (isTrackingGuildExp) {
            guildExpSummary.matchMatcher(event.message) {
                val gexp = group("gexp").toInt()
                totalGexp += gexp
                counter++
                return
            }

            ChatUtils.chat("\n§f    §r§l§eTotal:            §r§2$totalGexp Guild Experience", prefix = false)
            ChatUtils.chat("§f    §r§l§eAverage:        §r§2${totalGexp / counter} Guild Experience", prefix = false)
            isTrackingGuildExp = false
        }
    }
}
