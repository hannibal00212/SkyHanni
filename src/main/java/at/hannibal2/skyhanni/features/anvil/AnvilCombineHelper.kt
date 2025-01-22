package at.hannibal2.skyhanni.features.anvil

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryUpdatedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.takeIfNotEmpty
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RenderUtils.highlight

@SkyHanniModule
object AnvilCombineHelper {

    private var lastInventoryHash = 0
    private var highlightSlots = mutableListOf<Int>()

    @HandleEvent
    fun onInventoryUpdate(event: InventoryUpdatedEvent) {
        if (!isEnabled() || !event.eventMatches()) return

        val inventoryHash = event.inventoryItems.hashCode()
        if (inventoryHash == lastInventoryHash) return
        lastInventoryHash = inventoryHash

        val leftStack = event.inventoryItems[29]
        val rightStack = event.inventoryItems[33]
        val stackLore = leftStack?.getLore()?.takeIfNotEmpty() ?: rightStack?.getLore()?.takeIfNotEmpty() ?: return

        highlightSlots = event.inventoryItems.filterKeys { it in 27..54 }.filter {
            it.value.getLore() == stackLore
        }.keys.toMutableList()
    }

    private fun isEnabled() = SkyHanniMod.feature.inventory.anvilCombineHelper && LorenzUtils.inSkyBlock
    private fun InventoryUpdatedEvent.eventMatches() = inventoryName == "Anvil" && inventorySize >= 52

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        lastInventoryHash = 0
        highlightSlots.clear()
    }

    @HandleEvent
    fun onBackgroundDrawn(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!isEnabled()) return

        val filteredSlots = InventoryUtils.getItemsInOpenChest().filter {
            it.slotNumber in highlightSlots
        }
        for (slot in filteredSlots) {
            slot highlight LorenzColor.GREEN
        }
    }
}
