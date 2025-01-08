package at.hannibal2.skyhanni.features.chat

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.groupOrNull
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.StringUtils.cleanPlayerName
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object CompactSplashPotionMessage {

    private val config get() = SkyHanniMod.feature.chat.compactPotionMessages

    @Suppress("MaxLineLength")
    private val potionEffectPatternList = listOf(
        "§a§lBUFF! §fYou were splashed by (?<playerName>.*) §fwith §r(?<effectName>.*)§r§f! Press TAB or type /effects to view your active effects!".toPattern(),
        "§a§lBUFF! §fYou have gained §r(?<effectName>.*)§r§f! Press TAB or type /effects to view your active effects!".toPattern(),
        "§a§lBUFF! §fYou splashed yourself with §r(?<effectName>.*)§r§f! Press TAB or type /effects to view your active effects!".toPattern(),

        // Fix for Hypixel having a different message for Poisoned Candy.
        // Did not make the first pattern optional to prevent conflicts with Dungeon Buffs/other things
        "§a§lBUFF! §fYou have gained §r(?<effectName>§2Poisoned Candy I)§r§f!".toPattern(),
        "§a§lBUFF! §fYou splashed yourself with §r(?<effectName>§2Poisoned Candy I)§r§f!".toPattern(),
        "§a§lBUFF! §fYou were splashed by (?<playerName>.*) §fwith §r(?<effectName>§2Poisoned Candy I)§r§f!".toPattern()
    )

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!isEnabled()) return
        if (!event.message.isPotionMessage()) return
        event.blockedReason = "compact_potion_effect"
    }

    private fun sendMessage(message: String) {
        if (config.clickableChatMessage) {
            ChatUtils.hoverableChat(
                message,
                listOf("§eClick to view your potion effects."),
                "/effects",
                prefix = false
            )
        } else {
            ChatUtils.chat(message, prefix = false)
        }
    }

    private fun String.isPotionMessage(): Boolean {
        return potionEffectPatternList.any {
            it.matchMatcher(this) {
                val effectName = group("effectName")
                // If splashed by a player, append their name.
                val byPlayer = groupOrNull("playerName")?.let { player ->
                    val displayName = player.cleanPlayerName(displayName = true)
                    " §aby $displayName"
                }.orEmpty()
                sendMessage("§a§lPotion Effect! §r$effectName$byPlayer")
            } != null
        }
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled
}
