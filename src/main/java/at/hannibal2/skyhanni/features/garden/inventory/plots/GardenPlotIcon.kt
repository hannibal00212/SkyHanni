package at.hannibal2.skyhanni.features.garden.inventory.plots

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.minecraft.ToolTipEvent
import at.hannibal2.skyhanni.events.render.gui.ReplaceItemEvent
import at.hannibal2.skyhanni.features.garden.GardenApi
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ItemUtils.editItemInfo
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.NeuItems.getItemStack
import net.minecraft.client.player.inventory.ContainerLocalMenu
import net.minecraft.init.Items
import net.minecraft.item.ItemStack

@SkyHanniModule
object GardenPlotIcon {

    private val config get() = GardenApi.config.plotIcon
    private val plotList get() = GardenApi.storage?.plotIcon?.plotList
    private var inInventory = false
    private var copyStack: ItemStack? = null

    // TODO replace with enum
    private var editMode = 0 // 0 = off, 1 = on, 2 = reset
    private var lastClickedSlotId = -1
    private val originalStack = mutableMapOf<Int, ItemStack>()
    private val cachedStack = mutableMapOf<Int, ItemStack>()
    private val editStack = ItemStack(Items.wooden_axe)
    private val whitelistedSlot =
        listOf(2, 3, 4, 5, 6, 11, 12, 13, 14, 15, 20, 21, 23, 24, 29, 30, 31, 32, 33, 38, 39, 40, 41, 42)

    var hardReset = false

    fun isEnabled() = GardenApi.inGarden() && config.enabled && inInventory

    @HandleEvent(priority = HandleEvent.HIGHEST)
    fun onInventoryFullyOpened(event: InventoryFullyOpenedEvent) {
        inInventory = event.inventoryName == "Configure Plots"
        if (!isEnabled()) return

        for ((index, stack) in event.inventoryItems) {
            originalStack[index] = stack
        }
        val plotList = plotList ?: return
        for ((index, internalName) in plotList) {
            val old = originalStack[index]!!
            val new = internalName.getItemStack()
            cachedStack[index] = new.editItemInfo(old.displayName, true, old.getLore())
        }
    }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inInventory = false
        editMode = 0
    }

    @HandleEvent(priority = HandleEvent.HIGH)
    fun replaceItem(event: ReplaceItemEvent) {
        if (!isEnabled()) return
        val plotList = plotList ?: return
        if (hardReset) {
            plotList.clear()
            hardReset = false
            return
        }

        if (event.inventory is ContainerLocalMenu) {
            if (event.slot == 53) {
                event.replace(editStack)
            }
            if (plotList.isNotEmpty() && plotList.contains(event.slot)) {
                if (lastClickedSlotId == event.slot) {
                    lastClickedSlotId = -1
                    return
                }
                cachedStack[event.slot]?.let { event.replace(it) }
            }
        }
    }

    @HandleEvent(priority = HandleEvent.HIGH)
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (!isEnabled()) return
        lastClickedSlotId = event.slotId
        if (event.slotId == 53) {
            event.cancel()
            if (event.clickedButton == 0) {
                if (editMode == 2)
                    editMode = 0
                else
                    editMode++
            } else if (event.clickedButton == 1) {
                if (editMode == 0)
                    editMode = 2
                else
                    editMode--
            }
            return
        }
        if (editMode != 0) {
            if (event.slotId in 54..89) {
                event.cancel()
                copyStack = event.slot?.stack?.copy()?.also {
                    it.stackSize = 1
                } ?: return
                // TODO different format, not bold or show not in chat at all.
                ChatUtils.chat("§6§lClick an item in the desk menu to replace it with that item!")
                return
            }
            if (event.slotId != 53) {
                val plotList = plotList ?: return
                if (!whitelistedSlot.contains(event.slotId)) return
                event.cancel()
                if (editMode == 2) {
                    plotList.remove(event.slotId)
                    return
                }
                val copyStack = copyStack ?: return
                plotList[event.slotId] = copyStack.getInternalName()
                cachedStack[event.slotId] = copyStack
            }
        }
    }

    @HandleEvent
    fun onToolTip(event: ToolTipEvent) {
        if (!isEnabled()) return
        val plotList = plotList ?: return
        val list = event.toolTip
        val index = event.slot.slotNumber
        if (index == 53) {
            list.clear()
            list.add("§6Edit Mode")
            list.add("")
            list.add("${if (editMode == 0) "§6► " else ""}§cOFF§7: §bNothing change, behave like normal.")
            list.add("${if (editMode == 1) "§6► " else ""}§aON§7: §bClick an item in your inventory then click again")
            list.add("${if (editMode == 1) "§6► " else ""}§bin the plot menu to change it to that item.")
            list.add("${if (editMode == 2) "§6► " else ""}§9RESET§7: §bClick an item in the menu to reset it to default.")
            list.add("")
            list.add("§eClick to switch Edit Mode !")
            list.add("")
        }
        if (plotList.contains(index)) {
            val stack = originalStack[index] ?: return
            val lore = stack.getLore()
            list.clear()
            list.add(0, stack.displayName)
            for (i in lore.indices) {
                list.add(i + 1, stack.getLore()[i])
            }
        }
    }
}
