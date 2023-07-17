package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.*
import at.hannibal2.skyhanni.features.misc.items.EstimatedItemValue
import at.hannibal2.skyhanni.utils.*
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.LorenzUtils.addAsSingletonList
import at.hannibal2.skyhanni.utils.LorenzUtils.addSelector
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import at.hannibal2.skyhanni.utils.RenderUtils.renderStringsAndItems
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class ChestValue {

    private val config get() = SkyHanniMod.feature.misc.chestValueConfig
    private var display = emptyList<List<Any>>()
    private val stacksList = mutableMapOf<Int, ItemStack>()
    private val chestItems = mutableMapOf<Int, Item>()
    private val slotList = mutableListOf<Int>()
    private var inInventory = false

    @SubscribeEvent
    fun onBackgroundDraw(event: GuiRenderEvent.ChestBackgroundRenderEvent) {
        if (inInventory) {
            config.position.renderStringsAndItems(
                display,
                extraSpace = -1,
                itemScale = 1.3,
                posLabel = "Estimated Chest Value"
            )
        }
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent){
        if (!isEnabled()) return
        if (event.isMod(5)){
            update()
        }
    }

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryOpenEvent) {
        if (!isEnabled()) return
        val inventoryName = event.inventoryName
        inInventory = inventoryName == "Chest" || inventoryName == "Large Chest"
        if (inInventory) {
            val stacks = event.inventoryItems
            stacksList.putAll(stacks)
            update()
        }
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        stacksList.clear()
        chestItems.clear()
        slotList.clear()
        inInventory = false
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    fun onDrawBackground(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!isEnabled()) return
        val name = InventoryUtils.openInventoryName()
        if (name == "Chest" || name == "Large Chest") {
            for (slot in InventoryUtils.getItemsInOpenChest()) {
                if (slotList.contains(slot.slotIndex)) {
                    slot highlight LorenzColor.GREEN
                }
            }
        }
    }

    private fun update() {
        display = drawDisplay()
    }

    fun drawDisplay(): List<List<Any>> {
        val newDisplay = mutableListOf<List<Any>>()
        var totalPrice = 0.0
        var rendered = 0
        init()

        if (chestItems.isNotEmpty()) {
            val sortedList = when (config.sortingType) {
                0 -> chestItems.values.sortedByDescending { it.total }.toMutableList()
                1 -> chestItems.values.sortedBy { it.total }.toMutableList()
                else -> chestItems.values.sortedByDescending { it.total }.toMutableList()
            }
            val amountShowing = if (config.itemToShow > sortedList.size) sortedList.size else config.itemToShow

            newDisplay.addAsSingletonList("§7Estimated Chest Value: §o(Rendering $amountShowing of ${sortedList.size} items)")
            for ((index, stack, _, total, tips) in sortedList) {
                totalPrice += total
                if (rendered >= config.itemToShow) continue
                if (total < config.hideBelow)  continue
                newDisplay.add(buildList {
                    val renderable = Renderable.clickAndHover(
                        "${stack.displayName}: §b${total.formatPrice()}",
                        tips
                    ) {
                        for (slot in InventoryUtils.getItemsInOpenChest()) {
                            if (index == slot.slotIndex) {
                                if (slotList.contains(slot.slotIndex)) {
                                    slotList.remove(slot.slotIndex)
                                } else {
                                    slotList.add(slot.slotIndex)
                                }
                            }
                        }
                    }
                    val dashColor = if (slotList.contains(index)) "§a" else "§7"
                    add(" $dashColor- ")
                    add(stack)
                    add(renderable)
                })
                rendered++
            }

            val sortingType = SortType.values()[config.sortingType].longName
            newDisplay.addAsSingletonList("§7Sorted By: §c$sortingType")
            newDisplay.addSelector(" ", SortType.values(),
                getName = { type -> type.shortName },
                isCurrent = { it.ordinal == config.sortingType },
                onChange = {
                    config.sortingType = it.ordinal
                    update()
                })
            newDisplay.addAsSingletonList("§6Total value : §b${totalPrice.formatPrice()}")
            newDisplay.addSelector(" ", FormatType.values(),
                getName = { type -> type.type },
                isCurrent = { it.ordinal == config.formatType },
                onChange = {
                    config.formatType = it.ordinal
                    update()
                })
        }
        return newDisplay
    }

    private fun init() {
        for ((i, stack) in stacksList) {
            val internalName = stack.getInternalName()
            if (internalName != "") {
                if (NEUItems.getItemStackOrNull(internalName) != null) {
                    val list = mutableListOf<String>()
                    val pair = EstimatedItemValue.getEstimatedItemPrice(stack, list)
                    var (total, base) = pair
                    if (stack.item == Items.enchanted_book)
                        total /= 2
                    if (total != 0.0)
                        chestItems[i] = Item(i, stack, base, total, list)
                }
            }
        }
    }

    private fun Double.formatPrice(): String {
        return when (config.formatType) {
            0 -> if (this > 1_000_000_000) format(this) else NumberUtil.format(this)
            1 -> this.addSeparators()
            else -> "0"
        }
    }
    private fun format(d: Double): String {
        val suffix = arrayOf("", "K", "M", "B", "T")
        var rep = 0
        var num = d
        while (num >= 1000) {
            num /= 1000.0
            rep++
        }
        return String.format("%.3f%s", num, suffix[rep]).replace(",", ".")
    }

    enum class SortType(val shortName: String, val longName: String) {
        PRICE_DESC("Price D", "Price Descending"),
        PRICE_ASC("Price A", "Price Ascending")
        ;
    }

    enum class FormatType(val type: String) {
        SHORT("Formatted"),
        LONG("Unformatted")
        ;
    }

    data class Item(
        val index: Int,
        val stack: ItemStack,
        val base: Double,
        val total: Double,
        val tips: MutableList<String>
    )


    fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled
}