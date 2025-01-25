package at.hannibal2.skyhanni.features.anvil

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils.getInventoryName
import at.hannibal2.skyhanni.utils.InventoryUtils.getLowerItems
import at.hannibal2.skyhanni.utils.InventoryUtils.getUpperItems
import at.hannibal2.skyhanni.utils.InventoryUtils.highlightAll
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.LorenzColor
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest

@SkyHanniModule
object AnvilCombineHelper {

    private val enabled get() = SkyHanniMod.feature.inventory.anvilCombineHelper
    private var lastInventoryHash = 0
    private var highlightSlots = setOf<Int>()
    private const val LEFT_STACK_INDEX = 29
    private const val RIGHT_STACK_INDEX = 33

    @HandleEvent
    fun onBackgroundDrawn(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!enabled) return
        val inventory = event.extractInventoryOrNull() ?: return
        inventory.checkInventory()

        inventory.getLowerItems().filter {
            it.key.slotIndex in highlightSlots
        }.keys highlightAll LorenzColor.GREEN
    }

    private fun ContainerChest.compHash(): Int =
        getLowerItems().hashCode() + getUpperItems().hashCode()

    private fun ContainerChest.checkInventory() {
        lastInventoryHash = this.compHash().takeIf { it != lastInventoryHash } ?: return

        // Reset highlight cache
        highlightSlots = emptySet()

        val (leftStack, rightStack) = getSlot(LEFT_STACK_INDEX)?.stack to getSlot(RIGHT_STACK_INDEX)?.stack
        val itemInternalName = when {
            leftStack != null && rightStack == null -> leftStack.getInternalNameOrNull()
            rightStack != null && leftStack == null -> rightStack.getInternalNameOrNull()
            else -> return
        }

        highlightSlots = getLowerItems().mapNotNull { (slot, stack) ->
            slot.slotIndex.takeIf {
                stack.getInternalNameOrNull() == itemInternalName
            }
        }.toSet()
    }

    private fun GuiContainerEvent.BackgroundDrawnEvent.extractInventoryOrNull(): ContainerChest? =
        ((gui as? GuiChest)?.inventorySlots as? ContainerChest).takeIf {
            it?.getInventoryName() == "Anvil" && it.getUpperItems().size >= 52
        }
}
