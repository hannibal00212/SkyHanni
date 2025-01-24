package at.hannibal2.skyhanni.features.inventory.shoppinglist

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.addString
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.renderables.Renderable

@SkyHanniModule
object ShoppingList {
    private val config get() = SkyHanniMod.feature.inventory.shoppingList

    private val itemList = mutableListOf<ShoppingListElement>()

    private var display = listOf<Renderable>()

    // all the functions for interacting with the shopping list come here
    fun add(itemName: NEUInternalName, amount: Int = 1, categoryName: String? = null) {
        // TODO: shouldn't happen @Thunderblade73
        if (!isEnabled()) return

        val maybeItem = ShoppingListItem(itemName, amount)

        if (categoryName != null) {
            val category = itemList.firstOrNull { it is ShoppingListCategory && it.name == categoryName } as ShoppingListCategory?
            if (category == null) {
                val newCategory = ShoppingListCategory(categoryName)
                newCategory.items.add(maybeItem)
                itemList.add(newCategory)
            } else {
                val item = category.items.firstOrNull { it.name == itemName } as ShoppingListItem?

                if (item == null) {
                    category.items.add(maybeItem)
                } else {
                    item.changeAmountBy(amount)
                }
            }
        } else {
            val item = itemList.firstOrNull {
                it is ShoppingListItem && it.name == itemName
            } as? ShoppingListItem

            if (item == null) {
                itemList.add(maybeItem)
            } else {
                item.changeAmountBy(amount)
            }
        }
    }

//     fun remove(element: ShoppingListElement) {
//         TODO
//     }

    fun clear() {
        itemList.clear()
    }

    // from here on only other functions
    fun test() {
        ChatUtils.chat("test triggered")

        add("Carrot".toInternalName(), 49)

        ChatUtils.chat(itemList.toString())

        createDisplay()

        ChatUtils.chat("test done")
    }

    fun createDisplay() {
        display = buildList {
            addString("Shopping List")
            itemList.forEach { addString(it.toString()) }
        }
    }

    @HandleEvent
    fun onRender(event: GuiRenderEvent.GuiOverlayRenderEvent){
        config.position.renderRenderables(display, posLabel = "Shopping List")
    }

    @HandleEvent
    fun onRender(event: GuiRenderEvent.ChestGuiOverlayRenderEvent){
        config.position.renderRenderables(display, posLabel = "Shopping List")
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled
}
