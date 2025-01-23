package at.hannibal2.skyhanni.features.inventory.shoppinglist

import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUItems

class ShoppingListItem(
    val name: NEUInternalName,
    var amount: Int = 1,
) : ShoppingListElement {

    private val subItems = mutableListOf<ShoppingListItem>()

    fun showRecipe() {
        NEUItems.getRecipes(name)
    }

    fun changeAmountBy(amount: Int) {
        this.amount += amount
    }

    fun changeAmountTo(amount: Int) {
        this.amount = amount
    }
}
