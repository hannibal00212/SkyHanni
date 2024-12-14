package at.hannibal2.skyhanni.features.commands

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.MessageSendToServerEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ChatUtils.senderIsSkyhanni
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.RegexUtils.findMatcher
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object AcceptLastPartyInvite {

    private val config get() = SkyHanniMod.feature.misc.commands

    private val patternGroup = RepoPattern.group("party.invite")

    /**
     * REGEX-TEST: §r§b[MVP§r§c+§r§b] STPREAPER §r§ehas invited you to join their party!
     * REGEX-TEST: §r§a[VIP] VrxyOwnsYou_ §r§ehas invited you to join their party!
     * REGEX-TEST: TutaoOakley §r§ehas invited you to join their party!
     */
    private val inviteReceivedPattern by patternGroup.pattern(
        "received",
        "§r§.(?:\\[.*?] )?(?<player>\\w+) §r§ehas invited you to join their party!"
    )

    /**
     * REGEX-TEST: §eThe party invite from §r§b[MVP§r§f+§r§b] OE07 §r§ehas expired.
     * REGEX-TEST: §eThe party invite from §r§a[VIP] VrxyOwnsYou_ §r§ehas expired.
     * REGEX-TEST: §eThe party invite from §r§7TMOffline96 §r§ehas expired.
     */
    private val inviteExpiredPattern by patternGroup.pattern(
        "expired",
        "§eThe party invite from (?:§r§.\\[.*?] )?(?<player>\\w+) §r§ehas expired\\."
    )

    private var lastInviter = ""

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!config.acceptLastInvite) return
        inviteReceivedPattern.findMatcher(event.message) {
            lastInviter = group("player")
            return
        }
        inviteExpiredPattern.findMatcher(event.message) {
            if (lastInviter == group("player")) {
                lastInviter = ""
                return
            }
        }
    }

    @SubscribeEvent
    fun onMessageSendToServer(event: MessageSendToServerEvent) {
        if (!config.acceptLastInvite) return
        if (event.senderIsSkyhanni()) return
        if (!event.message.startsWith("/party accept", ignoreCase = true) &&
            !event.message.startsWith("/p accept", ignoreCase = true)) {
            return
        }
        event.isCanceled = true
        if (lastInviter == "") {
            ChatUtils.chat("There is no party invite to accept!")
            return
        }
        HypixelCommands.partyAccept(lastInviter)
        lastInviter = ""
    }
}
