package at.hannibal2.skyhanni.utils

import net.minecraft.item.Item
import net.minecraft.item.ItemStack

object ItemBlink {

    private val offsets = mutableMapOf<Item, Long>()
    private var lastOffset = 0L
    private var endOfBlink = 0L
    private var blinkItem: ItemStack? = null

    fun setBlink(item: ItemStack?, durationMillis: Long) {
        endOfBlink = System.currentTimeMillis() + durationMillis
        blinkItem = item
    }

    fun ItemStack.checkBlinkItem(): ItemStack {
        val stack = blinkItem ?: return this
        if (System.currentTimeMillis() > endOfBlink) return this

        val offset: Long = if (!offsets.containsKey(item)) {
            lastOffset += 200
            val number = lastOffset % 1000
            offsets[item] = number
            number
        } else {
            offsets[item]!!
        }
        return if ((offset + System.currentTimeMillis()) % 1000 > 500) stack else this
    }
}
