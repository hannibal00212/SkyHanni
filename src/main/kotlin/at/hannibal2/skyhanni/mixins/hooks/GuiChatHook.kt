package at.hannibal2.skyhanni.mixins.hooks

import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import net.minecraft.util.IChatComponent

object GuiChatHook {

    lateinit var replacement: ChatComponentText

    fun replaceEntireComponent(title: String, chatStyle: ChatStyle) {
        if (!this::replacement.isInitialized) return

        // Initialise new component
        val newComponent = ChatComponentText(title)
        newComponent.setChatStyle(chatStyle)

        replacement = newComponent
    }

    fun replaceOnlyHoverEvent(hoverEvent: HoverEvent) {
        if (!this::replacement.isInitialized) return

        // Initialise new component
        val newComponent = ChatComponentText(replacement.chatComponentText_TextValue)
        newComponent.setChatStyle(replacement.chatStyle)
        newComponent.chatStyle.chatHoverEvent = hoverEvent

        replacement = newComponent
    }

    fun getReplacementAsIChatComponent(): IChatComponent {
        if (!this::replacement.isInitialized) {
            // Return an extremely basic chat component as to not error downstream
            return ChatComponentText("Original component was not set")
        }
        return replacement
    }
}
