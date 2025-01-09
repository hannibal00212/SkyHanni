package at.hannibal2.skyhanni.data.hypixel.chat.event

import at.hannibal2.skyhanni.utils.ComponentSpan
import net.minecraft.util.IChatComponent


class GuildChatEvent(
    author: ComponentSpan,
    message: ComponentSpan,
    val guildRank: ComponentSpan?,
    chatComponent: IChatComponent,
    blockedReason: String? = null,
) : AbstractChatEvent(author, message, chatComponent, blockedReason) {
    val authorString by lazy { author.getText() }
    val messageString by lazy { message.getText() }
}
