package at.hannibal2.skyhanni.features.chat

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.NumberUtil.formatDouble
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RegexUtils.replace
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.util.ChatComponentText

@SkyHanniModule
object ShortenCoins {
    private val config get() = SkyHanniMod.feature.chat
    private val patternGroup = RepoPattern.group("chat.coins")

    /**
     * REGEX-TEST: §6[Auction] §aEuropaPlus §ebought §fAtmospheric Filter §efor §62,650,000 coins §lCLICK
     * REGEX-TEST: §aYou sold §r§aCicada Symphony Vinyl§r§8 x1 §r§afor §r§650,000 Coins§r§a!
     * REGEX-TEST: §6§lALLOWANCE! §r§eYou earned §r§650,000 coins§r§e!
     * REGEX-TEST: §6[Bazaar] §r§7§r§eSell Offer Setup! §r§a5§r§7x §r§9Enchanted Melon Block §r§7for §r§6250,303 coins§r§7.
     * REGEX-FAIL: §aYou have withdrawn §r§610.5k coins§r§a! You now have §r§6991.1M coins §r§ain your account!
     */
    private val coinsPattern by patternGroup.pattern(
        "format",
        "§6(?<amount>[\\d,.]+)(?![\\d.,kMB])",
    )

    @HandleEvent(onlyOnSkyblock = true)
    fun onChat(event: SkyHanniChatEvent) {
        if (!config.shortenCoinAmounts) return
        val message = event.message
        val modifiedMessage = coinsPattern.replace(message) {
            "§6${group("amount").formatDouble().shortFormat()}"
        }.takeIf { it != message } ?: return

        event.chatComponent = ChatComponentText(modifiedMessage)
    }
}
