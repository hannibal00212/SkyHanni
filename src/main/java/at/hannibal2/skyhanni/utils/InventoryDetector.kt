package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * The InventoryDetector tracks whether an inventory is open and provides
 * a inventory open consumer and a isInside function to handle inventory check logic.
 *
 * @property onInventoryOpen A callback triggered when the given inventory is detected to be open. Optional.
 * @property checkInventoryName Define what inventory name or names we are looking for.
 *
 * @constructor Initializes the detector and registers it to a global list of active detectors.
 */
class InventoryDetector(
    val onInventoryOpen: () -> Unit = {},
    val checkInventoryName: (String) -> Boolean,
) {

    init {
        detectors.add(this)
    }

    private var inInventory = false

    /**
     * Check if the player is currently inside this inventory.
     */
    fun isInside() = inInventory

    @SkyHanniModule
    companion object {

        private val detectors = mutableListOf<InventoryDetector>()

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        fun onInventoryClose(event: InventoryCloseEvent) {
            detectors.forEach { it.inInventory = false }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
            detectors.forEach { it.updateInventoryState(event.inventoryName) }
        }
    }

    private fun updateInventoryState(inventoryName: String) {
        inInventory = try {
            checkInventoryName(inventoryName)
        } catch (e: Exception) {
            ErrorManager.logErrorWithData(e, "Failed checking inventory state")
            false
        }

        if (inInventory) {
            try {
                onInventoryOpen()
            } catch (e: Exception) {
                ErrorManager.logErrorWithData(e, "Failed to run inventory open in InventoryDetector")
            }
        }
    }
}
