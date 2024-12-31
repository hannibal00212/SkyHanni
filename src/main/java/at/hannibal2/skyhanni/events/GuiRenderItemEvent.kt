package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import net.minecraft.client.gui.FontRenderer
import net.minecraft.item.ItemStack

abstract class GuiRenderItemEvent : SkyHanniEvent() {
    abstract class RenderOverlayEvent(
        open val fontRenderer: FontRenderer,
        open val stack: ItemStack?,
        open val x: Int,
        open val y: Int,
        open val text: String?,
    ) : GuiRenderItemEvent() {

        data class GuiRenderItemPost(
            override val fontRenderer: FontRenderer,
            override val stack: ItemStack?,
            override val x: Int,
            override val y: Int,
            override val text: String?,
        ) : RenderOverlayEvent(fontRenderer, stack, x, y, text)
    }
}
