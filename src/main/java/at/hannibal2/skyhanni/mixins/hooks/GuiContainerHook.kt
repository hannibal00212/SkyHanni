package at.hannibal2.skyhanni.mixins.hooks

import at.hannibal2.skyhanni.data.GuiData
import at.hannibal2.skyhanni.events.DrawScreenAfterEvent
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.GuiContainerEvent.CloseWindowEvent
import at.hannibal2.skyhanni.events.GuiContainerEvent.SlotClickEvent
import at.hannibal2.skyhanni.test.SkyHanniDebugsAndTests
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.system.PlatformUtils
import io.github.moulberry.notenoughupdates.NEUApi
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.Slot
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

class GuiContainerHook(guiAny: Any) {

    val gui: GuiContainer

    init {
        gui = guiAny as GuiContainer
    }

    fun closeWindowPressed(ci: CallbackInfo) {
        if (CloseWindowEvent(gui, gui.inventorySlots).postAndCatch()) ci.cancel()
    }

    fun backgroundDrawn(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!SkyHanniDebugsAndTests.globalRender) return
        GuiContainerEvent.BackgroundDrawnEvent(gui, gui.inventorySlots, mouseX, mouseY, partialTicks).postAndCatch()
    }

    fun preDraw(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
        ci: CallbackInfo,
    ) {
        if (!SkyHanniDebugsAndTests.globalRender) return
        if (GuiContainerEvent.PreDraw(gui, gui.inventorySlots, mouseX, mouseY, partialTicks).postAndCatch()) {
            if (PlatformUtils.isNeuLoaded()) NEUApi.setInventoryButtonsToDisabled()
            GuiData.preDrawEventCancelled = true
            ci.cancel()
        } else {
            DelayedRun.runNextTick {
                GuiData.preDrawEventCancelled = false
            }
        }
    }

    fun postDraw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!SkyHanniDebugsAndTests.globalRender) return
        GuiContainerEvent.PostDraw(gui, gui.inventorySlots, mouseX, mouseY, partialTicks).postAndCatch()
    }

    fun foregroundDrawn(mouseX: Int, mouseY: Int, partialTicks: Float) {
        GuiContainerEvent.ForegroundDrawnEvent(gui, gui.inventorySlots, mouseX, mouseY, partialTicks).postAndCatch()
    }

    fun onDrawSlot(slot: Slot, ci: CallbackInfo) {
        val event = GuiContainerEvent.DrawSlotEvent.GuiContainerDrawSlotPre(gui, gui.inventorySlots, slot)
        if (event.postAndCatch()) ci.cancel()
    }

    fun onDrawSlotPost(slot: Slot) {
        GuiContainerEvent.DrawSlotEvent.GuiContainerDrawSlotPost(gui, gui.inventorySlots, slot).postAndCatch()
    }

    fun onMouseClick(slot: Slot?, slotId: Int, clickedButton: Int, clickType: Int, ci: CallbackInfo) {
        val item = gui.inventorySlots?.inventory?.takeIf { it.size > slotId && slotId >= 0 }?.get(slotId)
        if (SlotClickEvent(gui, gui.inventorySlots, item, slot, slotId, clickedButton, clickType).postAndCatch()
        ) ci.cancel()
    }

    fun onDrawScreenAfter(
        mouseX: Int,
        mouseY: Int,
        ci: CallbackInfo,
    ) {
        if (DrawScreenAfterEvent(mouseX, mouseY, ci).postAndCatch()) ci.cancel()
    }

}
