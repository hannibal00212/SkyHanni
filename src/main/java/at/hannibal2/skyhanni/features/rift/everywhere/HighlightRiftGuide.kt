package at.hannibal2.skyhanni.features.rift.everywhere

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object HighlightRiftGuide {

    private var inInventory = false
    private var highlightedItems = emptyList<Int>()

    @HandleEvent
    fun onInventoryFullyOpened(event: InventoryFullyOpenedEvent) {
        inInventory = false

        if (!isEnabled()) return

        val inGuide = event.inventoryItems[40]?.getLore()?.let {
            if (it.size == 1) {
                it[0].startsWith("§7To Rift Guide")
            } else false
        } ?: false
        if (!inGuide) return

        val highlightedItems = mutableListOf<Int>()
        for ((slot, stack) in event.inventoryItems) {
            val lore = stack.getLore()
            if (lore.isNotEmpty() && lore.last() == "§8✖ Not completed yet!") {
                highlightedItems.add(slot)
            }
        }
        inInventory = true
        this.highlightedItems = highlightedItems
    }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inInventory = false
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    fun onBackgroundDrawn(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!isEnabled()) return
        if (!inInventory) return

        for (slot in InventoryUtils.getItemsInOpenChest()) {
            if (slot.slotIndex in highlightedItems) {
                slot highlight LorenzColor.YELLOW
            }
        }
    }

    fun isEnabled() = RiftAPI.inRift() && RiftAPI.config.highlightGuide
}
