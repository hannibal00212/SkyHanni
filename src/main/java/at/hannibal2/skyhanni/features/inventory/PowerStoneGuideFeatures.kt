package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.minecraft.ToolTipEvent
import at.hannibal2.skyhanni.features.inventory.bazaar.BazaarApi
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.nextAfter
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getPrice
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RenderUtils.highlight

@SkyHanniModule
object PowerStoneGuideFeatures {

    private val missing = mutableMapOf<Int, NeuInternalName>()
    private var inInventory = false

    @HandleEvent
    fun onInventoryFullyOpened(event: InventoryFullyOpenedEvent) {
        if (!isEnabled()) return
        if (event.inventoryName != "Power Stones Guide") return

        inInventory = true

        for ((slot, item) in event.inventoryItems) {
            val lore = item.getLore()
            if (lore.contains("§7Learned: §cNot Yet ✖")) {
                val rawName = lore.nextAfter("§7Power stone:") ?: continue
                val name = NeuInternalName.fromItemName(rawName)
                missing[slot] = name
            }
        }
    }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inInventory = false
    }

    @HandleEvent
    fun onBackgroundDrawn(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!isEnabled()) return
        if (!inInventory) return

        event.gui.inventorySlots.inventorySlots
            .filter { missing.containsKey(it.slotNumber) }
            .forEach { it highlight LorenzColor.RED }
    }

    @HandleEvent
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (!isEnabled()) return
        if (!inInventory) return
        val internalName = missing[event.slotId] ?: return

        BazaarApi.searchForBazaarItem(internalName, 9)
    }

    @HandleEvent
    fun onToolTip(event: ToolTipEvent) {
        if (!isEnabled()) return
        if (!inInventory) return

        val internalName = missing[event.slot.slotNumber] ?: return
        val totalPrice = internalName.getPrice() * 9
        event.toolTip.add(5, "9x from Bazaar: §6${totalPrice.shortFormat()}")
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && SkyHanniMod.feature.inventory.powerStoneGuide
}
