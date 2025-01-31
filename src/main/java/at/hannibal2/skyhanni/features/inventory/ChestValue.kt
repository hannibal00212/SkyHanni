package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.features.inventory.ChestValueConfig.NumberFormatEntry
import at.hannibal2.skyhanni.config.features.inventory.ChestValueConfig.SortingTypeEntry
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryOpenEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.features.dungeon.DungeonApi
import at.hannibal2.skyhanni.features.inventory.bazaar.BazaarApi
import at.hannibal2.skyhanni.features.minion.MinionFeatures
import at.hannibal2.skyhanni.features.misc.items.EstimatedItemValue
import at.hannibal2.skyhanni.features.misc.items.EstimatedItemValueCalculator
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.addItemStack
import at.hannibal2.skyhanni.utils.CollectionUtils.addString
import at.hannibal2.skyhanni.utils.ConfigUtils
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.NeuItems.getItemStackOrNull
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.RenderableUtils.addRenderableButton
import at.hannibal2.skyhanni.utils.renderables.addLine
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.init.Items
import net.minecraft.item.ItemStack

@SkyHanniModule
object ChestValue {

    private val config get() = SkyHanniMod.feature.inventory.chestValueConfig
    private var display = emptyList<Renderable>()
    private var chestItems = mapOf<String, Item>()
    private val inInventory get() = isValidStorage()
    private var inOwnInventory = false

    @HandleEvent
    fun onBackgroundDraw(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (DungeonApi.inDungeon() && !config.enableInDungeons) return
        if (!inOwnInventory) {
            if (InventoryUtils.openInventoryName() == "") return
        }

        if (!config.showDuringEstimatedItemValue && EstimatedItemValue.isCurrentlyShowing()) return

        if (inInventory) {
            config.position.renderRenderables(
                display,
                extraSpace = -1,
                posLabel = featureName(),
            )
        }
    }

    fun featureName() = if (inOwnInventory) "Estimated Inventory Value" else "Estimated Chest Value"

    @HandleEvent
    fun onTick(event: SkyHanniTickEvent) {
        if (!isEnabled()) return
        if (!event.isMod(5)) return
        val inInv = Minecraft.getMinecraft().currentScreen is GuiInventory
        inOwnInventory = inInv && config.enableInOwnInventory
        if (!inInventory) return
        update()
    }

    @HandleEvent
    fun onInventoryOpen(event: InventoryOpenEvent) {
        if (!isEnabled()) return
        if (inInventory) {
            update()
        }
    }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        chestItems = emptyMap()
    }

    private fun update() {
        display = drawDisplay()
    }

    private fun drawDisplay() = buildList {
        init()

        if (chestItems.isEmpty()) return@buildList

        addList()
        addButton()
    }

    private fun MutableList<Renderable>.addList() {
        val sortedList = sortedList()
        var totalPrice = 0.0
        var rendered = 0

        val amountShowing = if (config.itemToShow > sortedList.size) sortedList.size else config.itemToShow
        addString("§7${featureName()}: §o(Showing $amountShowing of ${sortedList.size} items)")

        for ((index, amount, stack, total, tips) in sortedList) {
            totalPrice += total
            if (rendered >= config.itemToShow) continue
            if (total < config.hideBelow) continue
            val textAmount = " §7x${amount.addSeparators()}:"
            val width = Minecraft.getMinecraft().fontRendererObj.getStringWidth(textAmount)
            val name = "${stack.itemName.reduceStringLength((config.nameLength - width), ' ')} $textAmount"
            val price = "§6${(total).formatPrice()}"
            val text = if (config.alignedDisplay) "$name $price"
            else "${stack.itemName} §7x$amount: §6${total.formatPrice()}"

            addLine {
                val renderable = Renderable.hoverTips(
                    text,
                    tips,
                    stack = stack,
                    highlightsOnHoverSlots = if (config.enableHighlight) index else emptyList(),
                )
                addString(" §7- ")
                if (config.showStacks) addItemStack(stack)
                add(renderable)
            }
            rendered++
        }
        addString("§aTotal value: §6${totalPrice.formatPrice()} coins")
    }

    private fun sortedList() = when (config.sortingType) {
        SortingTypeEntry.DESCENDING -> chestItems.values.sortedByDescending { it.total }
        SortingTypeEntry.ASCENDING -> chestItems.values.sortedBy { it.total }
        else -> chestItems.values.sortedByDescending { it.total }
    }

    // TODO: Avoid Ordinal
    private fun MutableList<Renderable>.addButton() {

        addRenderableButton<SortingTypeEntry>(
            label = "Price Sorting",
            current = config.sortingType,
            getName = { it.displayName },
            onChange = {
                config.sortingType = it
                update()
            },
            universe = SortingTypeEntry.entries,
        )

        addRenderableButton<NumberFormatEntry>(
            label = "Value Format",
            current = config.formatType,
            getName = { it.displayName },
            onChange = {
                config.formatType = it
                update()
            },
            universe = NumberFormatEntry.entries,
        )

        // TODO boolean support
        addRenderableButton<DisplayType>(
            label = "Display Type",
            current = DisplayType.entries[if (config.alignedDisplay) 1 else 0],
            getName = { it.type },
            onChange = {
                config.alignedDisplay = !config.alignedDisplay
                update()
            },
            universe = DisplayType.entries,
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
        val items = mutableMapOf<String, Item>()
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
            val item = items.getOrPut(key) {
                Item(mutableListOf(), 0, stack, 0.0, list)
            }
            item.index.add(i)
            item.amount += stack.stackSize
            item.total += total * stack.stackSize
        }
        chestItems = items
    }

    private fun Double.formatPrice(): String {
        return when (config.formatType) {
            NumberFormatEntry.SHORT -> if (this > 1_000_000_000) this.shortFormat(true) else this
                .shortFormat()

            NumberFormatEntry.LONG -> this.addSeparators()
            else -> "0"
        }
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
        // TODO: Use repo for this
        return name == "Chest" || name == "Large Chest" || inMinion ||
            name == "Personal Vault" || name == "Chest Storage" || name == "Wood Chest+"
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

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.transform(17, "inventory.chestValueConfig.formatType") { element ->
            ConfigUtils.migrateIntToEnum(element, NumberFormatEntry::class.java)
        }
        event.transform(15, "inventory.chestValueConfig.sortingType") { element ->
            ConfigUtils.migrateIntToEnum(element, SortingTypeEntry::class.java)
        }
    }
}
