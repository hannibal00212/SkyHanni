package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.client.player.inventory.ContainerLocalMenu
import net.minecraft.entity.IMerchant
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import net.minecraft.world.IWorldNameable
import kotlin.time.Duration.Companion.seconds

object InventoryUtils {

    var itemInHandId = NEUInternalName.NONE
    var recentItemsInHand = mutableMapOf<Long, NEUInternalName>()
    var latestItemInHand: ItemStack? = null

    fun getItemsInOpenChest(): List<Slot> {
        val guiChest = Minecraft.getMinecraft().currentScreen as? GuiChest ?: return emptyList<Slot>()
        return guiChest.inventorySlots.inventorySlots
            .filter { it.inventory !is InventoryPlayer && it.stack != null }
    }

    fun getSlotsInOwnInventory(): List<Slot> {
        val guiInventory = Minecraft.getMinecraft().currentScreen as? GuiInventory ?: return emptyList<Slot>()
        return guiInventory.inventorySlots.inventorySlots
            .filter { it.inventory is InventoryPlayer && it.stack != null }
    }

    // TODO add cache that persists until the next gui/window open/close packet is sent/received
    fun openInventoryName() = Minecraft.getMinecraft().currentScreen.let {
        if (it is GuiChest) {
            val chest = it.inventorySlots as ContainerChest
            chest.getInventoryName()
        } else ""
    }

    fun inInventory() = Minecraft.getMinecraft().currentScreen is GuiChest

    fun inContainer() = Minecraft.getMinecraft().currentScreen is GuiContainer

    fun ContainerChest.getInventoryName() = this.lowerChestInventory.displayName.unformattedText.trim()

    fun getWindowId(): Int? = (Minecraft.getMinecraft().currentScreen as? GuiChest)?.inventorySlots?.windowId

    fun getItemsInOwnInventory() =
        getItemsInOwnInventoryWithNull()?.filterNotNull().orEmpty()

    fun getItemsInOwnInventoryWithNull() = Minecraft.getMinecraft().thePlayer?.inventory?.mainInventory

    // TODO use this instead of getItemsInOwnInventory() for many cases, e.g. vermin tracker, diana spade, etc
    fun getItemsInHotbar() =
        getItemsInOwnInventoryWithNull()?.slice(0..8)?.filterNotNull().orEmpty()

    fun containsInLowerInventory(predicate: (ItemStack) -> Boolean): Boolean =
        countItemsInLowerInventory(predicate) > 0

    fun countItemsInLowerInventory(predicate: (ItemStack) -> Boolean): Int =
        getItemsInOwnInventory().filter { predicate(it) }.sumOf { it.stackSize }

    fun inStorage() = openInventoryName().let {
        (it.contains("Storage") && !it.contains("Rift Storage")) ||
            it.contains("Ender Chest") || it.contains("Backpack")
    }

    fun getItemInHand(): ItemStack? = Minecraft.getMinecraft().thePlayer.heldItem

    fun getArmor(): Array<ItemStack?> = Minecraft.getMinecraft().thePlayer.inventory.armorInventory

    fun getHelmet(): ItemStack? = getArmor()[3]
    fun getChestplate(): ItemStack? = getArmor()[2]
    fun getLeggings(): ItemStack? = getArmor()[1]
    fun getBoots(): ItemStack? = getArmor()[0]

    val isNeuStorageEnabled by RecalculatingValue(10.seconds) {
        try {
            val config = NotEnoughUpdates.INSTANCE.config

            val storageField = config.javaClass.getDeclaredField("storageGUI")
            val storage = storageField.get(config)

            val booleanField = storage.javaClass.getDeclaredField("enableStorageGUI3")
            booleanField.get(storage) as Boolean
        } catch (e: Throwable) {
            ErrorManager.logErrorWithData(e, "Could not read NEU config to determine if the neu storage is enabled.")
            false
        }
    }

    fun isSlotInPlayerInventory(itemStack: ItemStack): Boolean {
        val screen = Minecraft.getMinecraft().currentScreen as? GuiContainer ?: return false
        val slotUnderMouse = screen.slotUnderMouse ?: return false
        return slotUnderMouse.inventory is InventoryPlayer && slotUnderMouse.stack == itemStack
    }

    fun isItemInInventory(name: NEUInternalName) = name.getAmountInInventory() > 0

    fun ContainerChest.getUpperItems(): Map<Slot, ItemStack> = buildMap {
        for ((slot, stack) in getAllItems()) {
            if (slot.slotNumber != slot.slotIndex) continue
            this[slot] = stack
        }
    }

    fun ContainerChest.getLowerItems(): Map<Slot, ItemStack> = buildMap {
        for ((slot, stack) in getAllItems()) {
            if (slot.slotNumber == slot.slotIndex) continue
            this[slot] = stack
        }
    }

    fun ContainerChest.getAllItems(): Map<Slot, ItemStack> = buildMap {
        for (slot in inventorySlots) {
            if (slot == null) continue
            val stack = slot.stack ?: continue
            this[slot] = stack
        }
    }

    fun Gui.getTitle() = when (this) {
        is IWorldNameable -> {
            name!!
        }

        is IMerchant -> {
            displayName.unformattedText!!
        }

        else -> ""
    }

    fun getItemAtSlotIndex(slotIndex: Int): ItemStack? = getSlotAtIndex(slotIndex)?.stack

    fun getSlotAtIndex(slotIndex: Int): Slot? = getItemsInOpenChest().find { it.slotIndex == slotIndex }

    fun NEUInternalName.getAmountInInventory(): Int = countItemsInLowerInventory { it.getInternalNameOrNull() == this }

    fun clickSlot(slot: Int) {
        val windowId = getWindowId() ?: return
        val controller = Minecraft.getMinecraft().playerController
        controller.windowClick(windowId, slot, 0, 0, Minecraft.getMinecraft().thePlayer)
    }

    fun Slot.isTopInventory() = inventory is ContainerLocalMenu
}
