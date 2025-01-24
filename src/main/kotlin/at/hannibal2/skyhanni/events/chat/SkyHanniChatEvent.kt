package at.hannibal2.skyhanni.events.chat

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import net.minecraft.util.IChatComponent

class SkyHanniChatEvent(
    val message: String,
    var chatComponent: IChatComponent,
    var blockedReason: String = "",
    var chatLineId: Int = 0,
) : SkyHanniEvent()
