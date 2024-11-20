package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.makeShiftClick
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object ShiftClickNPCSell {

    private val config get() = SkyHanniMod.feature.inventory.shiftClickNPCSell

    private val sellSlot = -4

    /**
     * REGEX-TEST: §eClick to buyback!
     */
    private val lastLoreLineOfSellPattern by RepoPattern.pattern(
        "inventory.npc.sell.lore",
        "§7them to this Shop!|§eClick to buyback!",
    )

    var inInventory = false
        private set

    fun isEnabled() = LorenzUtils.inSkyBlock && config

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (event.inventoryItems.isEmpty()) return
        val item = event.inventoryItems[event.inventoryItems.keys.last() + sellSlot] ?: return

        inInventory = lastLoreLineOfSellPattern.matches(item.getLore().lastOrNull())
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inInventory = false
    }

    @SubscribeEvent
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (!isEnabled()) return
        if (!inInventory) return

        val slot = event.slot ?: return

        if (slot.slotNumber == slot.slotIndex) return

        event.makeShiftClick()
    }
}
