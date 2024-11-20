package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.utils.GuiRenderUtils
import at.hannibal2.skyhanni.utils.InventoryUtils
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.Container
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.Cancelable

abstract class GuiContainerEvent(open val gui: GuiContainer, open val container: Container) : LorenzEvent() {

    data class BackgroundDrawnEvent(
        override val gui: GuiContainer,
        override val container: Container,
        val mouseX: Int,
        val mouseY: Int,
        val partialTicks: Float,
    ) : GuiContainerEvent(gui, container)

    @Cancelable
    data class PreDraw(
        override val gui: GuiContainer,
        override val container: Container,
        val mouseX: Int,
        val mouseY: Int,
        val partialTicks: Float,
    ) : GuiContainerEvent(gui, container) {
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

    @Cancelable
    data class CloseWindowEvent(override val gui: GuiContainer, override val container: Container) :
        GuiContainerEvent(gui, container)

    abstract class DrawSlotEvent(gui: GuiContainer, container: Container, open val slot: Slot) :
        GuiContainerEvent(gui, container) {

        @Cancelable
        data class GuiContainerDrawSlotPre(
            override val gui: GuiContainer,
            override val container: Container,
            override val slot: Slot,
        ) :
            DrawSlotEvent(gui, container, slot)

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

    @Cancelable
    data class SlotClickEvent(
        override val gui: GuiContainer,
        override val container: Container,
        val item: ItemStack?,
        val slot: Slot?,
        val slotId: Int,
        val clickedButton: Int,
        @Deprecated("old", ReplaceWith("clickTypeEnum"))
        val clickType: Int,
        val clickTypeEnum: ClickType? = ClickType.getTypeById(clickType),
    ) : GuiContainerEvent(gui, container) {

        fun makePickblock() {
            if (this.clickedButton == 2 && this.clickTypeEnum == ClickType.MIDDLE) return
            slot?.slotNumber?.let { slotNumber ->
                InventoryUtils.clickSlot(slotNumber, container.windowId, 2, 3)
                isCanceled = true
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
