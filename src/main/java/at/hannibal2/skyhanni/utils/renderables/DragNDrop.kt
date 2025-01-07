package at.hannibal2.skyhanni.utils.renderables

import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyClicked
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object DragNDrop {

    private var currentDrag: DragItem<*>? = null

    private var isInvalidDrop = false

    private const val BUTTON = 0

    private const val BUTTON_MAPPED = BUTTON - 100

    private val invalidItem = Renderable.itemStack(ItemStack(Blocks.barrier), 1.0)

    @SubscribeEvent
    fun onGuiContainerBeforeDraw(event: GuiContainerEvent.PreDraw) {
        isInvalidDrop = false
    }

    @SubscribeEvent
    fun onGuiContainerAfterDraw(event: GuiContainerEvent.PostDraw) {
        val item = currentDrag ?: return
        if (!BUTTON_MAPPED.isKeyHeld()) {
            currentDrag?.onDiscard()
            currentDrag = null
            return
        }
        GlStateManager.translate(event.mouseX.toFloat(), event.mouseY.toFloat(), 0f)
        if (isInvalidDrop) {
            invalidItem.render(event.mouseX, event.mouseY)
        } else {
            item.onRender(event.mouseX, event.mouseY)
        }
        GlStateManager.translate(-event.mouseX.toFloat(), -event.mouseY.toFloat(), 0f)
    }

    fun draggable(
        display: Renderable,
        item: () -> DragItem<*>,
        bypassChecks: Boolean = false,
        condition: () -> Boolean = { true },
    ) = Renderable.clickable(
        display,
        onClick = { currentDrag = item() },
        button = BUTTON,
        bypassChecks = bypassChecks,
        condition = condition,
    )

    fun droppable(
        display: Renderable,
        drop: Droppable,
        bypassChecks: Boolean = false,
        condition: () -> Boolean = { true },
    ): Renderable = object : RenderableWrapper(display) {
        override fun render(posX: Int, posY: Int) {
            if (isHovered(posX, posY) && condition() && Renderable.shouldAllowLink(true, bypassChecks)) {
                handelDroppable(drop)
            }
            content.render(posX, posY)
        }
    }

    fun handelDroppable(drop: Droppable) {
        val item = currentDrag?.get() ?: return
        if (drop.validTarget(item)) {
            if (!BUTTON_MAPPED.isKeyHeld()) {
                drop.handle(item)
                currentDrag = null
            } else {
                drop.preDrop(item)
            }
        } else {
            isInvalidDrop = true
        }
    }

    fun dragOnPress(item: DragItem<*>, onClick: () -> Unit) {
        if (BUTTON_MAPPED.isKeyClicked()) {
            currentDrag = item
            onClick()
        }
    }
}

fun ItemStack.toDragItem(scale: Double = 1.0) = object : DragItem<ItemStack> {

    val render = Renderable.itemStack(this@toDragItem, scale, 0)

    override fun get(): ItemStack = this@toDragItem

    override fun onRender(mouseX: Int, mouseY: Int) = render.render(mouseX, mouseY)
}

interface DragItem<T> {

    fun get(): T
    fun onRender(mouseX: Int, mouseY: Int)

    fun onDiscard() {}

}

interface Droppable {

    fun handle(drop: Any?)

    fun preDrop(drop: Any?) {}

    fun validTarget(item: Any?): Boolean
}
