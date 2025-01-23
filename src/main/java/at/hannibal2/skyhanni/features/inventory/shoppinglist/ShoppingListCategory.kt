package at.hannibal2.skyhanni.features.inventory.shoppinglist

class ShoppingListCategory(public val name: String) : ShoppingListElement {
    public val items = mutableListOf<ShoppingListItem>()
}
