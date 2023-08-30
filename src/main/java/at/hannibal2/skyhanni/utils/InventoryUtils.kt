package at.hannibal2.skyhanni.utils

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import kotlin.time.Duration.Companion.milliseconds

object InventoryUtils {
    var itemInHandId = NEUInternalName.NONE
    var recentItemsInHand = mutableMapOf<Long, NEUInternalName>()
    var latestItemInHand: ItemStack? = null
    private var lastArmorCheck = SimpleTimeMark.farPast()
    private var wornArmorNames = emptyArray<ItemStack?>()

    fun getItemsInOpenChest() = buildList<Slot> {
        val guiChest = Minecraft.getMinecraft().currentScreen as? GuiChest ?: return emptyList<Slot>()
        val inventorySlots = guiChest.inventorySlots.inventorySlots
        val skipAt = inventorySlots.size - 9 * 4
        var i = 0
        for (slot in inventorySlots) {
            val stack = slot.stack
            if (stack != null) {
                add(slot)
            }
            i++
            if (i == skipAt) break
        }
    }

    fun openInventoryName() = Minecraft.getMinecraft().currentScreen.let {
        if (it is GuiChest) {
            val chest = it.inventorySlots as ContainerChest
            chest.getInventoryName()
        } else ""
    }

    fun ContainerChest.getInventoryName() = this.lowerChestInventory.displayName.unformattedText.trim()

    fun getItemsInOwnInventory() = Minecraft.getMinecraft().thePlayer.inventory.mainInventory.filterNotNull()

    fun countItemsInLowerInventory(predicate: (ItemStack) -> Boolean) =
        getItemsInOwnInventory().filter { predicate(it) }.sumOf { it.stackSize }

    fun getArmor(dataAgeLimit: Int): Array<ItemStack?> {
        if (lastArmorCheck.passedSince() > dataAgeLimit.milliseconds) {
            wornArmorNames = Minecraft.getMinecraft().thePlayer.inventory.armorInventory
            lastArmorCheck = SimpleTimeMark.now()
        }

        return wornArmorNames
    }

    fun inStorage() =
        openInventoryName().let {
            (it.contains("Storage") && !it.contains("Rift Storage")) || it.contains("Ender Chest") || it.contains(
                "Backpack"
            )
        }

    fun getItemInHand(): ItemStack? = Minecraft.getMinecraft().thePlayer.heldItem
}