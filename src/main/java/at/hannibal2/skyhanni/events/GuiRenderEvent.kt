package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent

open class GuiRenderEvent : SkyHanniEvent() {
    // Renders only while inside an inventory
    class ChestGuiOverlayRenderEvent : GuiRenderEvent()
    // Renders always, and while in an inventory it renders a bit darker, gray
    class GuiOverlayRenderEvent : GuiRenderEvent()
}
