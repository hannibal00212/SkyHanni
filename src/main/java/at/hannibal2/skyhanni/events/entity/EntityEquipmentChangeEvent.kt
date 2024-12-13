package at.hannibal2.skyhanni.events.entity

import at.hannibal2.skyhanni.api.event.GenericSkyHanniEvent
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack

data class EntityEquipmentChangeEvent<T : Entity>(
    val entity: T,
    val equipmentSlot: Int,
    val newItemStack: ItemStack?,
) : GenericSkyHanniEvent<T>(entity.javaClass) {

    val isHead get() = equipmentSlot == EQUIPMENT_SLOT_HEAD
    val isChest get() = equipmentSlot == EQUIPMENT_SLOT_CHEST
    val isLeggings get() = equipmentSlot == EQUIPMENT_SLOT_LEGGINGS
    val isFeet get() = equipmentSlot == EQUIPMENT_SLOT_FEET
    val isHand get() = equipmentSlot == EQUIPMENT_SLOT_HAND

    companion object {

        const val EQUIPMENT_SLOT_HEAD = 4
        const val EQUIPMENT_SLOT_CHEST = 3
        const val EQUIPMENT_SLOT_LEGGINGS = 2
        const val EQUIPMENT_SLOT_FEET = 1
        const val EQUIPMENT_SLOT_HAND = 0
    }
}
