package at.hannibal2.skyhanni.data.hypixel.chat.event

import at.hannibal2.skyhanni.utils.ComponentSpan
import net.minecraft.util.IChatComponent

class PlayerShowItemChatEvent(
    val levelComponent: ComponentSpan?,
    val action: ComponentSpan,
    author: ComponentSpan,
    val item: ComponentSpan,
    message: ComponentSpan,
    chatComponent: IChatComponent,
    blockedReason: String? = null,
) : AbstractChatEvent(author, message, chatComponent, blockedReason)
