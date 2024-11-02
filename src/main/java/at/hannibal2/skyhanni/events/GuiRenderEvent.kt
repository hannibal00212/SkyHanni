package at.hannibal2.skyhanni.events

open class GuiRenderEvent : LorenzEvent() {
    class ChestGuiOverlayRenderEvent : GuiRenderEvent()
    class GuiOverlayRenderEvent : GuiRenderEvent()
}
