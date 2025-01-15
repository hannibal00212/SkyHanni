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
     * REGEX-TEST: §6[Auction] §aEuropaPlus §ebought §fAtmospheric Filter §efor §62,650,000 coins §lCLICK
     * REGEX-TEST: §aYou sold §r§aCicada Symphony Vinyl§r§8 x1 §r§afor §r§650,000 Coins§r§a!
     */
    private val coinsPattern by patternGroup.pattern(
        "format",
        "§6(?<amount>[\\d,]+)"
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
