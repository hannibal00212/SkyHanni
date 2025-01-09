package at.hannibal2.skyhanni.data.hypixel.chat.event

import at.hannibal2.skyhanni.utils.ComponentSpan
import net.minecraft.util.IChatComponent

class PlayerAllChatEvent(
    val levelComponent: ComponentSpan?,
    val privateIslandRank: ComponentSpan?,
    val privateIslandGuest: ComponentSpan?,
    val chatColor: String,
    authorComponent: ComponentSpan,
    messageComponent: ComponentSpan,
    chatComponent: IChatComponent,
    blockedReason: String? = null,
) : AbstractChatEvent(authorComponent, messageComponent, chatComponent, blockedReason) {
    val levelColor = levelComponent?.sampleStyleAtStart()?.color
    val level = levelComponent?.getText()?.toInt()
    val isAGuest get() = privateIslandGuest != null
}
