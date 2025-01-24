package at.hannibal2.skyhanni.features.inventory.shoppinglist

import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUItems

class ShoppingListItem(
    val name: NEUInternalName,
    var amount: Int = 1,
) : ShoppingListElement {

    private val subItems = mutableListOf<ShoppingListItem>()

    override fun toString(): String {
        return "$name x$amount" + if (subItems.isNotEmpty()) {
            " (${subItems.joinToString(", ")})"
        } else {
            ""
        }
    }

    fun showRecipe() {
        val allRecipes = NEUItems.getRecipes(name)
    }

    fun changeAmountBy(amount: Int) {
        this.amount += amount
    }

    fun changeAmountTo(amount: Int) {
        this.amount = amount
    }
}
