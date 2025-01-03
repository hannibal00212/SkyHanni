package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class InventoryDetector(
    val onInventoryOpen: () -> Unit = {},
    val checkInventoryName: (String) -> Boolean,
) {

    init {
        detectors.add(this)
    }

    private var inInventory = false

    fun isInside() = inInventory

    @SkyHanniModule
    companion object {

        val detectors = mutableListOf<InventoryDetector>()

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
