package at.hannibal2.skyhanni.events.render.gui

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.InventoryBasic
import net.minecraft.item.ItemStack
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

class ReplaceItemEvent(val inventory: IInventory, val originalItem: ItemStack, val slot: Int) : SkyHanniEvent() {
    var replacement: ItemStack? = null
        private set

    fun replace(replacement: ItemStack?) {
        this.replacement = replacement
    }

    companion object {
        @JvmStatic
        fun postEvent(
            inventory: InventoryBasic,
            inventoryContents: Array<ItemStack?>,
            slot: Int,
            cir: CallbackInfoReturnable<ItemStack>,
        ) {
            val originalItem = inventoryContents.getOrNull(slot) ?: return
            val event = ReplaceItemEvent(inventory, originalItem, slot)
            event.post()
            event.replacement?.let { cir.returnValue = it }
        }
    }
}
