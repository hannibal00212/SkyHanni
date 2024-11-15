package at.hannibal2.skyhanni.events

import net.minecraft.util.ChatComponentText
import net.minecraft.util.IChatComponent

class ActionBarUpdateEvent(var actionBar: String, var chatComponent: IChatComponent) : LorenzEvent() {
    fun changeActionBar(newText: String) {
        chatComponent = ChatComponentText(newText)
    }
}
class ActionBarBeforeUpdateEvent(val actionBar: String, val chatComponent: IChatComponent) : LorenzEvent()
