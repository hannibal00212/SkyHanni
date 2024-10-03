package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.LorenzToolTipEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.InventoryUtils.getAllItems
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.KeyboardManager
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object QuickCraftFeatures {

    private val config get() = SkyHanniMod.feature.inventory
    private val quickCraftSlots = listOf(16, 25, 34)
    private var quickCraftableItems = emptyList<String>()

    enum class InventoryType(val inventoryName: String) {
        CRAFT_ITEM("Craft Item"),
        MORE_QUICK_CRAFT_OPTIONS("Quick Crafting"),
    }

    private fun InventoryType.ignoreSlot(slotNumber: Int?): Boolean = when (this) {
        InventoryType.CRAFT_ITEM -> slotNumber !in quickCraftSlots
        InventoryType.MORE_QUICK_CRAFT_OPTIONS -> slotNumber !in 10..44
    }

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        quickCraftableItems = event.getConstant<List<String>>("QuickCraftableItems")
    }

    @SubscribeEvent
    fun onToolTip(event: LorenzToolTipEvent) {
        val inventoryType = getInventoryType() ?: return
        if (inventoryType.ignoreSlot(event.slot.slotNumber)) return

        if (needsQuickCraftConfirmation(event.itemStack)) {
            event.toolTip.replaceAll {
                it.replace(
                    "Click to craft!",
                    "§c${KeyboardManager.getModifierKeyName()} + Click to craft!"
                )
            }
        }
    }

    @SubscribeEvent
    fun onForegroundDrawn(event: GuiContainerEvent.ForegroundDrawnEvent) {
        val inventoryType = getInventoryType() ?: return
        if (KeyboardManager.isModifierKeyDown()) return
        if (event.gui !is GuiChest) return
        val chest = event.gui.inventorySlots as ContainerChest

        for ((slot, stack) in chest.getAllItems()) {
            if (inventoryType.ignoreSlot(slot.slotNumber)) continue
            if (stack.name == "§cQuick Crafting Slot") continue
            if (needsQuickCraftConfirmation(stack)) {
                slot highlight LorenzColor.DARK_GRAY.addOpacity(180)
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        val inventoryType = getInventoryType() ?: return
        if (inventoryType.ignoreSlot(event.slot?.slotNumber)) return

        val clickedItem = event.slot?.stack ?: return
        if (!KeyboardManager.isModifierKeyDown() && needsQuickCraftConfirmation(clickedItem)) {
            event.cancel()
        }
    }

    private fun needsQuickCraftConfirmation(item: ItemStack): Boolean {
        return !quickCraftableItems.contains(item.displayName.removeColor())
    }

    private fun getInventoryType(): InventoryType? {
        if (!LorenzUtils.inSkyBlock || !config.quickCraftingConfirmation) return null

        val inventoryName = InventoryUtils.openInventoryName()
        return InventoryType.entries.firstOrNull { it.inventoryName == inventoryName }
    }
}
