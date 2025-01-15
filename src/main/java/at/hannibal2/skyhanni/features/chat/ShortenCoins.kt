package at.hannibal2.skyhanni.features.chat

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.NumberUtil.formatDouble
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.util.ChatComponentText
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object ShortenCoins {
    private val config get() = SkyHanniMod.feature.chat
    private val patternGroup = RepoPattern.group("chat.coins")

    /**
     * Because this regex isn't matching the whole message but parts of it, the real regex
     * tests can't be included here. The regex tests are just the coins section of the message.
     *
     * §6[Auction] §aEuropaPlus §ebought §fAtmospheric Filter §efor §62,650,000 coins §lCLICK
     * §aYou sold §r§aCicada Symphony Vinyl§r§8 x1 §r§afor §r§650,000 Coins§r§a!
     * §6§lALLOWANCE! §r§eYou earned §r§650,000 coins§r§e!
     * §6[Bazaar] §r§7§r§eSell Offer Setup! §r§a5§r§7x §r§9Enchanted Melon Block §r§7for §r§6250,303 coins§r§7.
     *
     * REGEX-TEST: §62,650,000
     * REGEX-TEST: §r§650,000
     * REGEX-TEST: §r§6250,303
     */
    private val coinsPattern by patternGroup.pattern(
        "format",
        "(?:§.)*§6(?<amount>[\\d,]+)"
    )

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!config.shortenCoinAmounts) return
        val message = event.message
        val modifiedMessage = coinsPattern.toRegex().replace(message) { match ->
            val amount = match.groups["amount"]?.value ?: return@replace match.value
            val amountAsDouble = amount.formatDouble()
            val displayAmount = amountAsDouble.shortFormat(preciseBillions = true)
            "§6$displayAmount"
        }.takeIf { it != message } ?: return

        event.chatComponent = ChatComponentText(modifiedMessage)
    }
}
