package at.hannibal2.skyhanni.features.inventory.shoppinglist

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.features.inventory.bazaar.BazaarApi
import at.hannibal2.skyhanni.features.inventory.bazaar.BazaarApi.isBazaarItem
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.addAsSingletonList
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.ItemPriceUtils.isAuctionHouseItem
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NEUItems
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiEditSign

@SkyHanniModule
object ShoppingList {
    private val config get() = SkyHanniMod.feature.inventory.shoppingList

    private val itemList = mutableListOf<ShoppingListElement>()

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

    fun update() {
        add("Carrot".toInternalName(), 49)

        mutableListOf<List<Any>>().drawShoppingList()
    }

    private fun MutableList<List<Any>>.drawShoppingList() {
        if (itemList.isEmpty()) return

        var totalPrice = 0.0
        addAsSingletonList("§Shopping List:")

        itemList.forEach { element ->
            when (element) {
                is ShoppingListItem -> {
                    val internalName = element.name
                    val name = internalName.itemName
                    val amount = element.amount

                    val list = mutableListOf<Any>()
                    list.add(" §7- ")
                    list.add(element.amount)

                    list.add(
                        Renderable.optionalLink(
                            "$name §ex${amount.addSeparators()}",
                            {
                                if (Minecraft.getMinecraft().currentScreen is GuiEditSign) {
                                    LorenzUtils.setTextIntoSign("$amount")
                                } else {
                                    if (internalName.isBazaarItem()) {
                                        BazaarApi.searchForBazaarItem(name, amount)
                                    } else if (internalName.isAuctionHouseItem()) {
                                        HypixelCommands.auctionSearch(name.removeColor())
                                    } else {
                                        val itemName = internalName.itemName
                                        ChatUtils.chat("Could not find $itemName§e on AH or BZ!", replaceSameMessage = true)
                                    }
                                }
                            },
                        ) { GardenAPI.inGarden() && !NEUItems.neuHasFocus() },
                    )

//                     if (config.showPrice) {
//                         val price = internalName.getPrice() * amount
//                         totalPrice += price
//                         val format = price.shortFormat()
//                         list.add(" §7(§6$format§7)")
//                     }

                    //             addSackData(internalName, amount, list)

                    add(list)
                }

                is ShoppingListCategory -> {
                    TODO()
                }
            }
        }
//         if (totalPrice > 0) {
//             val format = totalPrice.shortFormat()
//             this[0] = listOf("§7Visitor Shopping List: §7(§6$format§7)")
//         }
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled
}
