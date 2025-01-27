package at.hannibal2.skyhanni.features.inventory.shoppinglist

import at.hannibal2.skyhanni.utils.ItemUtils.getItemRarityOrCommon
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.ItemUtils.itemNameWithoutColor
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.NeuItems
import at.hannibal2.skyhanni.utils.NeuItems.getItemStackOrNull
import at.hannibal2.skyhanni.utils.renderables.Renderable

class ShoppingListItem(
    val name: NeuInternalName,
    var amount: Int = 1,
    val isToplevelItem: Boolean = true,
) {

    var hidden = false

    private val subItems = mutableListOf<ShoppingListItem>()

    override fun toString(): String {
        return "${name.itemName} x$amount" + if (subItems.isNotEmpty()) {
            " (${subItems.joinToString(", ")})"
        } else {
            ""
        }
    }

    fun getRecipe() {
        println("getting the Recipe")

        val allRecipes = NeuItems.getRecipes(name)
    }

    fun changeAmountBy(amount: Int) {
        this.amount += amount
    }

    fun changeAmountTo(amount: Int) {
        this.amount = amount
    }

    fun getIndent(amount: Int): String {
        var result = ""
        (0 until amount).forEach {
            result += "- "
        }
        return result
    }

    fun getRenderables(indent: Int): List<Renderable> {
        val renderables = mutableListOf<Renderable>()
        if (!hidden) {
            println(name.itemName)
            val rarity: LorenzRarity? = name.getItemStackOrNull()?.getItemRarityOrCommon()
            val displayName: String = if (rarity == null || rarity == LorenzRarity.COMMON || rarity == LorenzRarity.UNCOMMON) {
                "§e" + name.itemNameWithoutColor
            } else {
                name.itemName
            }
            println("Adding $displayName x$amount to renderables, rarity: $rarity")
            renderables.add(
                Renderable.link(
                    getIndent(indent) + "$displayName§e x$amount" + " §7Click to view recipe", true,
                ) {
                    getRecipe()
                },
            )
            subItems.forEach {
                renderables.addAll(it.getRenderables(indent + 1))
            }
        }
        return renderables
    }
}
