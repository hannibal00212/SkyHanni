package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiInventory

/**
 * RenderDisplayHelper determines when to render displays based on
 * conditions and context, such as whether the player is in their inventory or
 * outside of an inventory GUI, or in a inventory defined by InventoryDetector.
 *
 * @property inventory set a InventoryDetector the display should be rendered in.
 * @property outsideInventory Specifies if the display should render when not inside any inventory.
 * @property inOwnInventory Specifies if the display should render when the player is in their own inventory.
 * @property condition Should the display be rendered at all? Insert the isEnabled() function here.
 * @property onRender This is getting called when the render should happen.
 */
class RenderDisplayHelper(
    private val inventory: InventoryDetector = noInventory,
    private val outsideInventory: Boolean = false,
    private val inOwnInventory: Boolean = false,
    private val condition: () -> Boolean,
    private val onRender: () -> Unit,
) {

    init {
        // Registers the instance to the list of all display helpers.
        allDisplays.add(this)
    }

    private fun renderIn(inOwnInventory: Boolean): Boolean {
        return (this.inOwnInventory && inOwnInventory) || inventory.isInside()
    }

    @SkyHanniModule
    companion object {
        private val noInventory = InventoryDetector { false }
        private val allDisplays = mutableListOf<RenderDisplayHelper>()
        private var currentlyVisibleDisplays = emptyList<RenderDisplayHelper>()

        @HandleEvent
        fun onTick(event: SkyHanniTickEvent) {
            currentlyVisibleDisplays = allDisplays.filter { it.checkCondition() }
        }

        @HandleEvent
        fun onBackgroundDraw(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
            val isInOwnInventory = Minecraft.getMinecraft().currentScreen is GuiInventory
            for (display in currentlyVisibleDisplays) {
                if (display.renderIn(isInOwnInventory)) {
                    display.render()
                }
            }
        }

        @HandleEvent
        fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
            val isInOwnInventory = Minecraft.getMinecraft().currentScreen is GuiInventory
            for (display in currentlyVisibleDisplays) {
                if (display.outsideInventory && !display.renderIn(isInOwnInventory)) {
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
