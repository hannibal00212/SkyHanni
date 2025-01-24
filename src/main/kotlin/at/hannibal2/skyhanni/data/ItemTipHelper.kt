package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.DrawScreenAfterEvent
import at.hannibal2.skyhanni.events.GuiRenderItemEvent
import at.hannibal2.skyhanni.events.RenderInventoryItemTipEvent
import at.hannibal2.skyhanni.events.RenderItemTipEvent
import at.hannibal2.skyhanni.mixins.transformers.gui.AccessorGuiContainer
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.SkyHanniDebugsAndTests
import at.hannibal2.skyhanni.utils.InventoryUtils.getInventoryName
import at.hannibal2.skyhanni.utils.RenderUtils.drawSlotText
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.inventory.ContainerChest

@SkyHanniModule
object ItemTipHelper {

    @HandleEvent(onlyOnSkyblock = true)
    fun onRenderItemOverlayPost(event: GuiRenderItemEvent.RenderOverlayEvent.GuiRenderItemPost) {
        val stack = event.stack?.takeIf { it.stackSize == 1 } ?: return

        val itemTipEvent = RenderItemTipEvent(stack, mutableListOf())
        itemTipEvent.post()

        if (itemTipEvent.renderObjects.isEmpty()) return

        for (renderObject in itemTipEvent.renderObjects) {
            val text = renderObject.text
            val x = event.x + 17 + renderObject.offsetX
            val y = event.y + 9 + renderObject.offsetY

            event.drawSlotText(x, y, text, 1f)
        }
    }

    @HandleEvent(priority = HandleEvent.HIGHEST, onlyOnSkyblock = true)
    fun onRenderInventoryItemOverlayPost(event: DrawScreenAfterEvent) {
        if (!SkyHanniDebugsAndTests.globalRender) return

        val gui = Minecraft.getMinecraft().currentScreen
        if (gui !is GuiChest) return
        val chest = gui.inventorySlots as ContainerChest
        val inventoryName = chest.getInventoryName()

        val guiLeft = (gui as AccessorGuiContainer).guiLeft
        val guiTop = (gui as AccessorGuiContainer).guiTop
        val fontRenderer = Minecraft.getMinecraft().fontRendererObj

        GlStateManager.disableLighting()
        GlStateManager.disableDepth()
        GlStateManager.disableBlend()
        for (slot in gui.inventorySlots.inventorySlots) {
            val stack = slot.stack ?: continue

            val itemTipEvent = RenderInventoryItemTipEvent(inventoryName, slot, stack)
            itemTipEvent.post()
            val stackTip = itemTipEvent.stackTip
            if (stackTip.isEmpty()) continue

            val xDisplayPosition = slot.xDisplayPosition
            val yDisplayPosition = slot.yDisplayPosition

            val x = guiLeft + xDisplayPosition + 17 + itemTipEvent.offsetX - if (itemTipEvent.alignLeft) {
                fontRenderer.getStringWidth(stackTip)
            } else 0
            val y = guiTop + yDisplayPosition + 9 + itemTipEvent.offsetY

            fontRenderer.drawStringWithShadow(stackTip, x.toFloat(), y.toFloat(), 16777215)
        }
        GlStateManager.enableLighting()
        GlStateManager.enableDepth()
    }
}
