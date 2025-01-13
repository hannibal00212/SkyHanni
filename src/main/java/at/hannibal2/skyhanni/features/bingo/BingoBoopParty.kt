package at.hannibal2.skyhanni.features.bingo

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.hypixel.chat.event.PrivateMessageChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.StringUtils.removeResets
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object BingoBoopParty {

    val config get() = SkyHanniMod.feature.event.bingo.boopParty
    val patternGroup = RepoPattern.group("bingo")
    private val boopPattern by patternGroup.pattern(
        "boop",
        "§dFrom.*§d§lBoop!"
    )

    @SubscribeEvent
    fun onChat(event: PrivateMessageChatEvent) {
        if (!isEnabled()) return

        val msg = event.messageComponent.textComponent.formattedText.removeResets()
        boopPattern.matchMatcher(msg) {
            val username = event.author
            ChatUtils.clickableChat("Click to invite $username to the party", onClick = {
                HypixelCommands.partyInvite(username)
            })
        }
    }

    fun isEnabled() = LorenzUtils.isBingoProfile && config
}
