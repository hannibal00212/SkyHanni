package at.hannibal2.skyhanni.features.anvil

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryUpdatedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.InventoryUtils.highlightAll
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.LorenzColor

@SkyHanniModule
object AnvilCombineHelper {

    private var lastInventoryHash = 0
    private var highlightSlots = setOf<Int>()

    @HandleEvent(onlyOnSkyblock = true)
    fun onInventoryUpdate(event: InventoryUpdatedEvent) {
        if (!isEnabled() || !event.eventMatches()) return

        val inventoryHash = event.inventoryItems.hashCode()
        if (inventoryHash == lastInventoryHash) return
        lastInventoryHash = inventoryHash

        val leftStack = event.inventoryItems[29]
        val rightStack = event.inventoryItems[33]
        val stackInternalName = leftStack?.getInternalNameOrNull() ?: rightStack?.getInternalNameOrNull() ?: return

        highlightSlots = event.inventoryItems.filterKeys { it in 27..54 }.filter {
            it.value.getInternalNameOrNull() == stackInternalName
        }.keys
    }

    private fun isEnabled() = SkyHanniMod.feature.inventory.anvilCombineHelper
    private fun InventoryUpdatedEvent.eventMatches() = inventoryName == "Anvil" && inventorySize >= 52

    @HandleEvent(onlyOnSkyblock = true)
    fun onInventoryClose(event: InventoryCloseEvent) {
        lastInventoryHash = 0
        highlightSlots = emptySet()
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onBackgroundDrawn(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!isEnabled()) return

        InventoryUtils.getItemsInOpenChest().filter {
            it.slotNumber in highlightSlots
        } highlightAll LorenzColor.GREEN
    }
}
