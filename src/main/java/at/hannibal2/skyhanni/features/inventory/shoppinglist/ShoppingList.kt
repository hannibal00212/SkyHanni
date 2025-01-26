package at.hannibal2.skyhanni.features.inventory.shoppinglist

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.addString
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.renderables.Renderable

@SkyHanniModule
object ShoppingList {
    private val config get() = SkyHanniMod.feature.inventory.shoppingList

    private val categories = mutableListOf<ShoppingListCategory>()
    private val items = ShoppingListCategory("Items")

    // TODO: add a kind of summary over all items needed in recipes

    // TODO: somehow also make it searchable?
    private var display = listOf<Renderable>()

    // all the functions for interacting with the shopping list come here
    fun add(itemName: NeuInternalName, amount: Int = 1, categoryName: String? = null) {
        // TODO: shouldn't happen @Thunderblade73
        if (!isEnabled()) return

        val category: ShoppingListCategory
        if (categoryName != null) {
            if (!categoryName.isCategory()) {
                category = ShoppingListCategory(categoryName)
                categories.add(category)
            } else {
                category = categories.firstOrNull { it.name == categoryName } ?: return
            }
        } else {
            category = items
        }
        category.add(itemName, amount)
    }

    fun removeCategory(categoryName: String) {
        if (!isEnabled()) return

        val category = categories.firstOrNull { it.name == categoryName } ?: return
        categories.remove(category)
    }

    // removeCommand ???
    fun remove(name: String, amount: Int? = null, categoryName: String? = null) {
        if (!isEnabled()) return

        var itemName: NeuInternalName? = name.toInternalName()
        if (itemName == null || !itemName.isKnownItem()) {
            itemName = null
            if (!name.isCategory()) {
                ChatUtils.userError("Item $name not found")
            } else {
                removeCategory(name)
            }
            return
        }

        if (categoryName != null) {
            if (!categoryName.isCategory()) {
                ChatUtils.userError("Category $categoryName not found")
            } else {
                val category = categories.firstOrNull { it.name == categoryName } ?: return

                category.remove(itemName, amount)
            }
            return
        }

        if (items.contains(itemName)) {
            items.remove(itemName, amount)
            return
        }

        var category: ShoppingListCategory? = null
        for (cat in categories) {
            if (cat.contains(itemName)) {
                if (category != null) {
                    ChatUtils.userError(
                        "Item $itemName found in multiple categories, " +
                            "please specify the category to remove from",
                    )
                    return
                }
                category = cat
            }
        }
        if (category == null) {
            ChatUtils.userError("Item $itemName not found")
        } else {
            category.remove(itemName, amount)
        }
    }

    fun clear() {
        categories.clear()
        items.clear()

        createDisplay()
    }

    // logic
    fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled

    fun String.isCategory() = categories.any { it.name == this }

    // all display related functions come here
    fun createDisplay() {
        display = buildList {
            addString("Shopping List")
            categories.forEach {

                addString(it.toString())
            }
        }
    }

    // other functions etc.
    fun update() {
        if (!isEnabled()) return

        createDisplay()
    }

    fun test() {
        ChatUtils.chat("test triggered")

        add("Carrot".toInternalName(), 49)

        ChatUtils.chat(categories.toString() + items.toString())

        createDisplay()

        ChatUtils.chat("test done")
    }


    // all events come here
    @HandleEvent
    fun onRender(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        config.position.renderRenderables(display, posLabel = "Shopping List")
    }

    @HandleEvent
    fun onRender(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        config.position.renderRenderables(display, posLabel = "Shopping List")
    }

    // this event should be last
    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.register("shshoppinglistclear") {
            description = "Clear the shopping list"
            category = CommandCategory.USERS_ACTIVE
            aliases = listOf("shslclear")
            callback { clear() }
        }
        event.register("shshoppinglistadd") {
            description = "Add an item to the shopping list"
            category = CommandCategory.USERS_ACTIVE
            aliases = listOf("shsladd")
            autoComplete { listOf("Carrot", "Potato", "Wheat") }
            callback { add(it[0].toInternalName(), it.getOrNull(1)?.toIntOrNull() ?: 1, it.getOrNull(2)) }
        }
        event.register("shshoppinglistremove") {
            description = "Remove an item from the shopping list"
            category = CommandCategory.USERS_ACTIVE
            aliases = listOf("shslremove")
            autoComplete { listOf("Carrot", "Potato", "Wheat") }
            callback { remove(it[0], it.getOrNull(1)?.toIntOrNull(), it.getOrNull(2)) }
        }
        event.register("shshoppinglisttest") {
            description = "Test the shopping list feature"
            category = CommandCategory.DEVELOPER_TEST
            aliases = listOf("shsltest")
            callback { test() }
        }

    }

}
