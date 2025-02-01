package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils.highlightAll
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.NeuItems
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.inventory.Slot

@SkyHanniModule
object HighlightMissingRepoItems {

    @HandleEvent(priority = HandleEvent.LOWEST, onlyOnSkyblock = true)
    fun onBackgroundDrawn(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!SkyHanniMod.feature.dev.debug.highlightMissingRepo) return

        val gui = event.gui

        if (gui is GuiChest) {
            highlightItems(gui.inventorySlots.inventorySlots)
        } else if (gui is GuiInventory) {
            val player = Minecraft.getMinecraft().thePlayer
            highlightItems(player.inventoryContainer.inventorySlots)
        }
    }

    private fun highlightItems(slots: Iterable<Slot>) {
        if (NeuItems.allInternalNames.isEmpty()) return
        val filteredSlots = slots.filter {
            val internalName = it.stack?.getInternalNameOrNull() ?: return@filter false
            it.stack != null && NeuItems.allInternalNames.contains(internalName) &&
                !NeuItems.ignoreItemsFilter.match(internalName.asString())
        }
        filteredSlots.highlightAll(LorenzColor.RED)
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "dev.highlightMissingRepo", "dev.debug.highlightMissingRepo")
    }
}
