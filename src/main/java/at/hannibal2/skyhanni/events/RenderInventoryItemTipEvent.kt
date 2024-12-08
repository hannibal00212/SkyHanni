package at.hannibal2.skyhanni.events

import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack

class RenderInventoryItemTipEvent(
    val inventoryName: String,
    val slot: Slot,
    val stack: ItemStack,
    var stackTip: String = "",
    var offsetX: Int = 0,
    var offsetY: Int = 0,
    var alignLeft: Boolean = true,
) : LorenzEvent()
