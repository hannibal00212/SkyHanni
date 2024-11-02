package at.hannibal2.skyhanni.data.hypixel.chat.event

import at.hannibal2.skyhanni.utils.ComponentSpan
import at.hannibal2.skyhanni.utils.StringUtils.cleanPlayerName
import net.minecraft.util.IChatComponent

class PartyChatEvent(
    authorComponent: ComponentSpan,
    messageComponent: ComponentSpan,
    chatComponent: IChatComponent,
    blockedReason: String? = null,
) : AbstractChatEvent(authorComponent, messageComponent, chatComponent, blockedReason) {
    val cleanedAuthor by lazy {
        author.cleanPlayerName()
    }
}
