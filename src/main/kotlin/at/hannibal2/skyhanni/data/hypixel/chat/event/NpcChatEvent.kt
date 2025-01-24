package at.hannibal2.skyhanni.data.hypixel.chat.event

import at.hannibal2.skyhanni.utils.ComponentSpan
import net.minecraft.util.IChatComponent

class NpcChatEvent(
    authorComponent: ComponentSpan,
    messageComponent: ComponentSpan,
    chatComponent: IChatComponent,
    blockedReason: String? = null,
) : AbstractChatEvent(authorComponent, messageComponent, chatComponent, blockedReason)
