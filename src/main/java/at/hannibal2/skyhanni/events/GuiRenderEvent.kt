package at.hannibal2.skyhanni.events

open class GuiRenderEvent : LorenzEvent() {
    // Renders only while inside a inventory
    class ChestGuiOverlayRenderEvent : GuiRenderEvent()
    // Renders always, and while in a inventory it renders a bit darker, gray
    class GuiOverlayRenderEvent : GuiRenderEvent()
}
