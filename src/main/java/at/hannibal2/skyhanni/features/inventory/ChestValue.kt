package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.features.inventory.ChestValueConfig.NumberFormatEntry
import at.hannibal2.skyhanni.config.features.inventory.ChestValueConfig.SortingTypeEntry
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryOpenEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.features.dungeon.DungeonAPI
import at.hannibal2.skyhanni.features.inventory.bazaar.BazaarApi
import at.hannibal2.skyhanni.features.minion.MinionFeatures
import at.hannibal2.skyhanni.features.misc.items.EstimatedItemValue
import at.hannibal2.skyhanni.features.misc.items.EstimatedItemValueCalculator
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.addAsSingletonList
import at.hannibal2.skyhanni.utils.ConfigUtils
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.addButton
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.NEUItems.getItemStackOrNull
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RenderUtils.renderStringsAndItems
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object ChestValue {

    private val config get() = SkyHanniMod.feature.inventory.chestValueConfig
    private var display = emptyList<List<Any>>()
    private val chestItems = mutableMapOf<String, Item>()
    private val inInventory get() = isValidStorage()
    private var inOwnInventory = false
    private var compactInventory = true

    @SubscribeEvent
    fun onBackgroundDraw(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (DungeonAPI.inDungeon() && !config.enableInDungeons) return
        if (!inOwnInventory) {
            if (InventoryUtils.openInventoryName() == "") return
        }

        if (!config.showDuringEstimatedItemValue && EstimatedItemValue.isCurrentlyShowing()) return

        if (inInventory) {
            config.position.renderStringsAndItems(
                display,
                extraSpace = -1,
                itemScale = 0.7,
                posLabel = featureName(),
            )
        }
    }

    fun featureName() = if (inOwnInventory) "Estimated Inventory Value" else "Estimated Chest Value"

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return
        if (!event.isMod(5)) return
        val inInv = Minecraft.getMinecraft().currentScreen is GuiInventory
        inOwnInventory = inInv && config.enableInOwnInventory
        if (!inInventory) return
        update()
    }

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryOpenEvent) {
        if (!isEnabled()) return
        if (inInventory) {
            update()
        }
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        chestItems.clear()
    }

    private fun update() {
        display = drawDisplay()
    }

    private fun drawDisplay(): List<List<Any>> {
        val newDisplay = mutableListOf<List<Any>>()

        init()

        if (chestItems.isEmpty()) return newDisplay

        addList(newDisplay)
        addButton(newDisplay)

        return newDisplay
    }

    private fun addList(newDisplay: MutableList<List<Any>>) {
        val sortedList = sortedList()
        var totalPrice = 0.0
        var rendered = 0

        val amountShowing = if (config.itemToShow > sortedList.size) sortedList.size else config.itemToShow
        newDisplay.addAsSingletonList("§7${featureName()}: §o(Showing $amountShowing of ${sortedList.size} items)")
        for ((index, amount, stack, total, tips) in sortedList) {
            totalPrice += total
            if (rendered >= config.itemToShow) continue
            if (total < config.hideBelow) continue
            val textAmount = " §7x${amount.addSeparators()}:"
            val width = Minecraft.getMinecraft().fontRendererObj.getStringWidth(textAmount)
            val name = "${stack.itemName.reduceStringLength((config.nameLength - width), ' ')} $textAmount"
            val price = "§6${(total).formatPrice()}"
            val text = if (config.alignedDisplay)
                "$name $price"
            else
                "${stack.itemName} §7x$amount: §6${total.formatPrice()}"
            newDisplay.add(
                buildList {
                    val renderable = Renderable.hoverTips(
                        text,
                        tips,
                        stack = stack,
                        highlightsOnHoverSlots = if (config.enableHighlight) index else emptyList(),
                    )
                    add(" §7- ")
                    if (config.showStacks) add(stack)
                    add(renderable)
                },
            )
            rendered++
        }
        newDisplay.addAsSingletonList("§aTotal value: §6${totalPrice.formatPrice()} coins")
    }

    private fun sortedList() = when (config.sortingType) {
        SortingTypeEntry.DESCENDING -> chestItems.values.sortedByDescending { it.total }
        SortingTypeEntry.ASCENDING -> chestItems.values.sortedBy { it.total }
        else -> chestItems.values.sortedByDescending { it.total }
    }

    private fun addButton(newDisplay: MutableList<List<Any>>) {
        newDisplay.addButton(
            "§7Sorted By: ",
            getName = SortType.entries[config.sortingType.ordinal].longName, // todo avoid ordinal
            onChange = {
                // todo avoid ordinals
                config.sortingType = SortingTypeEntry.entries[(config.sortingType.ordinal + 1) % 2]
                update()
            },
        )

        newDisplay.addButton(
            "§7Value format: ",
            getName = FormatType.entries[config.formatType.ordinal].type, // todo avoid ordinal
            onChange = {
                // todo avoid ordinal
                config.formatType = NumberFormatEntry.entries[(config.formatType.ordinal + 1) % 2]
                update()
            },
        )

        newDisplay.addButton(
            "§7Display Type: ",
            getName = DisplayType.entries[if (config.alignedDisplay) 1 else 0].type,
            onChange = {
                config.alignedDisplay = !config.alignedDisplay
                update()
            },
        )
    }

    private fun init() {
        if (!inInventory) return
        val slots = if (inOwnInventory) {
            InventoryUtils.getSlotsInOwnInventory()
        } else {
            val isMinion = InventoryUtils.openInventoryName().contains(" Minion ")
            InventoryUtils.getItemsInOpenChest().filter {
                it.hasStack && it.inventory != Minecraft.getMinecraft().thePlayer.inventory && (!isMinion || it.slotNumber % 9 != 1)
            }
        }
        val stacks = buildMap {
            slots.forEach {
                put(it.slotIndex, it.stack)
            }
        }
        chestItems.clear()
        for ((i, stack) in stacks) {
            val internalName = stack.getInternalNameOrNull() ?: continue
            if (internalName.getItemStackOrNull() == null) continue
            val list = mutableListOf<String>()
            var total = EstimatedItemValueCalculator.calculate(stack, list).first
            val key = "$internalName+$total"
            if (stack.item == Items.enchanted_book)
                total /= 2
            list.add("§aTotal: §6§l${total.formatPrice()} coins")
            if (total == 0.0) continue
            val item = chestItems.getOrPut(key) {
                Item(mutableListOf(), 0, stack, 0.0, list)
            }
            item.index.add(i)
            item.amount += stack.stackSize
            item.total += total * stack.stackSize
        }
    }

    private fun Double.formatPrice(): String {
        return when (config.formatType) {
            NumberFormatEntry.SHORT -> if (this > 1_000_000_000) this.shortFormat(true) else this
                .shortFormat()

            NumberFormatEntry.LONG -> this.addSeparators()
            else -> "0"
        }
    }

    enum class SortType(val shortName: String, val longName: String) {
        PRICE_DESC("Price D", "Price Descending"),
        PRICE_ASC("Price A", "Price Ascending"),
    }

    enum class FormatType(val type: String) {
        SHORT("Formatted"),
        LONG("Unformatted"),
    }

    enum class DisplayType(val type: String) {
        NORMAL("Normal"),
        COMPACT("Aligned")
    }

    private fun isValidStorage(): Boolean {
        if (inOwnInventory) return true
        val name = InventoryUtils.openInventoryName().removeColor()
        if (Minecraft.getMinecraft().currentScreen !is GuiChest) return false
        if (BazaarApi.inBazaarInventory) return false
        if (MinionFeatures.minionInventoryOpen) return false
        if (MinionFeatures.minionStorageInventoryOpen) return false


        if ((name.contains("Backpack") && name.contains("Slot #") || name.startsWith("Ender Chest (")) &&
            !InventoryUtils.isNeuStorageEnabled
        ) {
            return true
        }

        val inMinion = name.contains("Minion") && !name.contains("Recipe") && IslandType.PRIVATE_ISLAND.isInIsland()
        return name == "Chest" || name == "Large Chest" || inMinion || name == "Personal Vault"
    }

    private fun String.reduceStringLength(targetLength: Int, char: Char): String {
        val mc = Minecraft.getMinecraft()
        val spaceWidth = mc.fontRendererObj.getCharWidth(char)

        var currentString = this
        var currentLength = mc.fontRendererObj.getStringWidth(currentString)

        while (currentLength > targetLength) {
            currentString = currentString.dropLast(1)
            currentLength = mc.fontRendererObj.getStringWidth(currentString)
        }

        val difference = targetLength - currentLength

        if (difference > 0) {
            val numSpacesToAdd = difference / spaceWidth
            val spaces = " ".repeat(numSpacesToAdd)
            return currentString + spaces
        }

        return currentString
    }

    data class Item(
        val index: MutableList<Int>,
        var amount: Int,
        val stack: ItemStack,
        var total: Double,
        val tips: MutableList<String>,
    )

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.transform(17, "inventory.chestValueConfig.formatType") { element ->
            ConfigUtils.migrateIntToEnum(element, NumberFormatEntry::class.java)
        }
        event.transform(15, "inventory.chestValueConfig.sortingType") { element ->
            ConfigUtils.migrateIntToEnum(element, SortingTypeEntry::class.java)
        }
    }
}
