package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.utils.GuiRenderUtils
import at.hannibal2.skyhanni.utils.InventoryUtils
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.Container
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack

abstract class GuiContainerEvent(open val gui: GuiContainer, open val container: Container) : SkyHanniEvent() {

    data class BackgroundDrawnEvent(
        override val gui: GuiContainer,
        override val container: Container,
        val mouseX: Int,
        val mouseY: Int,
        val partialTicks: Float,
    ) : GuiContainerEvent(gui, container)

    data class PreDraw(
        override val gui: GuiContainer,
        override val container: Container,
        val mouseX: Int,
        val mouseY: Int,
        val partialTicks: Float,
    ) : GuiContainerEvent(gui, container), Cancellable {
        fun drawDefaultBackground() =
            GuiRenderUtils.drawGradientRect(0, 0, gui.width, gui.height, -1072689136, -804253680, 0.0)
    }

    data class PostDraw(
        override val gui: GuiContainer,
        override val container: Container,
        val mouseX: Int,
        val mouseY: Int,
        val partialTicks: Float,
    ) : GuiContainerEvent(gui, container)

    data class CloseWindowEvent(override val gui: GuiContainer, override val container: Container) :
        GuiContainerEvent(gui, container), Cancellable

    abstract class DrawSlotEvent(gui: GuiContainer, container: Container, open val slot: Slot) :
        GuiContainerEvent(gui, container) {

        data class GuiContainerDrawSlotPre(
            override val gui: GuiContainer,
            override val container: Container,
            override val slot: Slot,
        ) :
            DrawSlotEvent(gui, container, slot), Cancellable

        data class GuiContainerDrawSlotPost(
            override val gui: GuiContainer,
            override val container: Container,
            override val slot: Slot,
        ) :
            DrawSlotEvent(gui, container, slot)
    }

    data class ForegroundDrawnEvent(
        override val gui: GuiContainer,
        override val container: Container,
        val mouseX: Int,
        val mouseY: Int,
        val partialTicks: Float,
    ) : GuiContainerEvent(gui, container)

    data class SlotClickEvent(
        override val gui: GuiContainer,
        override val container: Container,
        val item: ItemStack?,
        val slot: Slot?,
        val slotId: Int,
        val clickedButton: Int,
        val clickType: ClickType?,
    ) : GuiContainerEvent(gui, container), Cancellable {

        fun makePickblock() {
            if (this.clickedButton == 2 && this.clickType == ClickType.MIDDLE) return
            slot?.slotNumber?.let { slotNumber ->
                InventoryUtils.clickSlot(slotNumber, container.windowId, 2, 3)
                cancel()
            }
        }
    }

    enum class ClickType(val id: Int) {
        NORMAL(0),
        SHIFT(1),
        HOTBAR(2),
        MIDDLE(3),
        DROP(4),
        ;

        companion object {
            fun getTypeById(id: Int) = entries.firstOrNull { it.id == id }
        }
    }
}
