package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryOpenEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.InventoryUtils.highlightAll
import at.hannibal2.skyhanni.utils.LorenzColor

@SkyHanniModule
object HighlightOnHoverSlot {
    val currentSlots = mutableMapOf<Pair<Int, Int>, List<Int>>()

    @HandleEvent
    fun onInventoryOpen(event: InventoryOpenEvent) {
        currentSlots.clear()
    }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        currentSlots.clear()
    }

    @HandleEvent(priority = HandleEvent.LOW, onlyOnSkyblock = true)
    fun onBackgroundDrawn(event: GuiContainerEvent.BackgroundDrawnEvent) {
        val list = currentSlots.flatMapTo(mutableSetOf()) { it.value }

        InventoryUtils.getItemsInOpenChest().filter {
            it.slotNumber in list
        } highlightAll LorenzColor.GREEN
    }
}
