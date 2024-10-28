package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.maxwell.MaxwellAPI
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryOpenEvent
import at.hannibal2.skyhanni.events.InventoryUpdatedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.KeyboardManager
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object FavoritePowerStone {

    private val config get() = SkyHanniMod.feature.inventory

    private var highlightedSlots = setOf<Int>()
    private var inInventory = false

    @SubscribeEvent
    fun onBackgroundDrawn(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!isEnabled() || !inInventory) return

        highlightedSlots.forEach { event.gui.inventorySlots.inventorySlots[it] highlight LorenzColor.AQUA }
    }

    @SubscribeEvent
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (!isEnabled() || !KeyboardManager.isShiftKeyDown() || !inInventory) return

        val displayName = event.item?.name?.removeColor()?.trim() ?: return
        val power = MaxwellAPI.getPowerByNameOrNull(displayName) ?: return

        if (power in MaxwellAPI.favoritePowers) {
            MaxwellAPI.favoritePowers -= power
            highlightedSlots -= event.slotId
        } else {
            MaxwellAPI.favoritePowers += power
            highlightedSlots += event.slotId
        }

        event.cancel()
    }

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryOpenEvent) {
        if (!isEnabled() || !MaxwellAPI.inInventory) return

        inInventory = true
    }

    @SubscribeEvent
    fun onInventoryUpdated(event: InventoryUpdatedEvent) {
        if (!isEnabled() || !inInventory) return

        highlightedSlots = event.inventoryItems
            .filterValues { item ->
                val power = MaxwellAPI.getPowerByNameOrNull(item.name.removeColor().trim())
                power in MaxwellAPI.favoritePowers
            }
            .keys
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inInventory = false
        highlightedSlots = setOf()
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.favoritePowerStone
}
