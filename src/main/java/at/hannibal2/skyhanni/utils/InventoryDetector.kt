package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager

/**
 * The InventoryDetector tracks whether an inventory is open and provides
 * an inventory open consumer and a isInside function to handle inventory check logic.
 *
 * @property openInventory A callback triggered when the given inventory is detected to be open. Contains the name of the inventory. Optional.
 * @property checkInventoryName Define what inventory name or names we are looking for.
 */
class InventoryDetector(
    val openInventory: (String) -> Unit = {},
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

        @HandleEvent(priority = HandleEvent.HIGHEST)
        fun onInventoryClose(event: InventoryCloseEvent) {
            detectors.forEach { it.inInventory = false }
        }

        @HandleEvent(priority = HandleEvent.HIGHEST)
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
                openInventory(inventoryName)
            } catch (e: Exception) {
                ErrorManager.logErrorWithData(e, "Failed to run inventory open in InventoryDetector")
            }
        }
    }
}
