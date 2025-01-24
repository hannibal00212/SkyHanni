package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.IChatComponent

class ActionBarUpdateEvent(var actionBar: String, var chatComponent: IChatComponent) : SkyHanniEvent() {
    fun changeActionBar(newText: String) {
        chatComponent = ChatComponentText(newText)
    }
}
