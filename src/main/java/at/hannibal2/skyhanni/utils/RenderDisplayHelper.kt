package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class RenderDisplayHelper(
    private val inventory: InventoryDetector = noInventory,
    private val outsideInventory: Boolean = false,
    private val inOwnInventory: Boolean = false,
    private val condition: () -> Boolean,
    private val onRender: () -> Unit,
) {

    init {
        allDisplays.add(this)
    }

    @SkyHanniModule
    companion object {
        private val noInventory = InventoryDetector { false }

        private val allDisplays = mutableListOf<RenderDisplayHelper>()
        private var currentlyVisibleDisplays = emptyList<RenderDisplayHelper>()

        @SubscribeEvent
        fun onTick(event: LorenzTickEvent) {
            currentlyVisibleDisplays = allDisplays.filter { it.checkCondition() }
        }

        @SubscribeEvent
        fun onRenderDisplay(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
            val isInOwnInventory = Minecraft.getMinecraft().currentScreen is GuiInventory
            for (display in currentlyVisibleDisplays) {
                if ((display.inOwnInventory && isInOwnInventory) || display.inventory.isInside()) {
                    display.render()
                }
            }
        }

        @SubscribeEvent
        fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
            for (display in currentlyVisibleDisplays) {
                if (display.outsideInventory) {
                    display.render()
                }
            }
        }
    }

    private fun checkCondition(): Boolean = try {
        condition()
    } catch (e: Exception) {
        ErrorManager.logErrorWithData(e, "Failed to check render display condition")
        false
    }

    private fun render() = try {
        onRender()
    } catch (e: Exception) {
        ErrorManager.logErrorWithData(e, "Failed to render a display")
    }
}
