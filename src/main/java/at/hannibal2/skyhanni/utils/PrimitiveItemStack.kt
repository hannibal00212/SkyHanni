package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.NeuItems.getItemStack
import net.minecraft.item.ItemStack

data class PrimitiveItemStack(val internalName: NeuInternalName, val amount: Int) {

    fun createItem(): ItemStack = internalName.getItemStack().apply { stackSize = amount }

    operator fun times(multiplier: Int): PrimitiveItemStack = PrimitiveItemStack(internalName, amount * multiplier)

    operator fun plus(amount: Int): PrimitiveItemStack = PrimitiveItemStack(internalName, this.amount + amount)

    val itemName by lazy { internalName.itemName }

    fun toPair() = Pair(internalName, amount)

    companion object {

        fun NeuInternalName.makePrimitiveStack(amount: Int = 1) = PrimitiveItemStack(this, amount)
        fun ItemStack.toPrimitiveStackOrNull() = getInternalNameOrNull()?.let { PrimitiveItemStack(it, stackSize) }
    }
}
