package at.hannibal2.skyhanni.features.anvil

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils.getInventoryName
import at.hannibal2.skyhanni.utils.InventoryUtils.getLowerItems
import at.hannibal2.skyhanni.utils.InventoryUtils.getUpperItems
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest

@SkyHanniModule
object AnvilCombineHelper {

    // TODO use InventoryUpdatedEvent and item id instead of no cache and lore comparison
    @HandleEvent(onlyOnSkyblock = true)
    fun onBackgroundDrawn(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!SkyHanniMod.feature.inventory.anvilCombineHelper) return

        if (event.gui !is GuiChest) return
        val chest = event.gui.inventorySlots as ContainerChest
        val chestName = chest.getInventoryName()

        if (chestName != "Anvil") return
        if (chest.getUpperItems().size < 52) return

        val matchLore = mutableListOf<String>()

        val leftStack = chest.getSlot(29)?.stack
        val rightStack = chest.getSlot(33)?.stack

        // don't highlight if both slots have items
        if (leftStack != null && rightStack != null) return

        if (leftStack != null) {
            matchLore.addAll(leftStack.getLore())
        } else if (rightStack != null) {
            matchLore.addAll(rightStack.getLore())
        }

        if (matchLore.isEmpty()) return

        for ((slot, stack) in chest.getLowerItems()) {
            if (matchLore == stack.getLore()) {
                slot highlight LorenzColor.GREEN
            }
        }
    }
}
