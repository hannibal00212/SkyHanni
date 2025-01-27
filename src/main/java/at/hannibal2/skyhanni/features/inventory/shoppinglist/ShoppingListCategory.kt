package at.hannibal2.skyhanni.features.inventory.shoppinglist

import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.renderables.Renderable

class ShoppingListCategory(val name: String) {
    val items = mutableListOf<ShoppingListItem>()
    var hidden = false
    var pinned = false // TODO: implement this

    override fun toString(): String {
        return name
    }

    fun add(itemName: NeuInternalName, amount: Int = 1) {
        if (!itemName.isKnownItem()) {
            ChatUtils.userError("Item ${itemName.itemName} not found")
            return
        }

        val item = items.firstOrNull { it.name == itemName } as ShoppingListItem?

        if (item == null) {
            items.add(ShoppingListItem(itemName, amount))
        } else {
            item.changeAmountBy(amount)
        }
    }

    fun remove(itemName: NeuInternalName, amount: Int? = null) {
        if (!itemName.isKnownItem()) {
            ChatUtils.userError("Item ${itemName.itemName} not found")
            return
        }

        val item = items.firstOrNull { it.name == itemName } as ShoppingListItem?

        if (item == null) {
            ChatUtils.userError("Item ${itemName.itemName} not found in category $name")
        } else {
            if (amount == null) {
                items.remove(item)
            } else {
                item.changeAmountBy(-amount)
                if (item.amount <= 0) {
                    items.remove(item)
                }
            }
        }
    }

    fun clear() {
        items.clear()
    }

    fun contains(itemName: NeuInternalName): Boolean {
        return items.any { it.name == itemName }
    }

    fun getRenderables(indent: Int): List<Renderable> {
        val renderables = mutableListOf<Renderable>()
        items.forEach { item ->
            renderables.addAll(item.getRenderables(indent))
        }
        return renderables
    }
}
