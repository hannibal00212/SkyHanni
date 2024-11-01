package at.hannibal2.skyhanni.utils.compat


import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.item.ItemStack
//#if MC >= 1.12
//$$ import net.minecraft.inventory.EntityEquipmentSlot
//#endif

fun Entity.getNameAsString(): String =
    this.name
//#if MC >= 1.14
//$$ .string
//#endif

fun EntityArmorStand.getStandHelmet(): ItemStack? =
//#if MC < 1.12
    this.getEquipmentInSlot(4)
//#else
//$$ this.getItemStackFromSlot(EntityEquipmentSlot.HEAD)
//#endif

fun EntityLiving.getEntityHelmet(): ItemStack? =
//#if MC < 1.12
    this.getEquipmentInSlot(4)
//#else
//$$ this.getItemStackFromSlot(EntityEquipmentSlot.HEAD)
//#endif

fun EntityLivingBase.getWholeInventory() =
//#if MC < 1.12
    this.inventory
//#else
//$$ this.equipmentAndArmor.toList()
//#endif

fun Entity.getFirstPassenger(): Entity? =
//#if MC < 1.12
    this.riddenByEntity
//#else
//$$ this.passengers.firstOrNull()
//#endif

fun EntityArmorStand.getHandItem(): ItemStack? =
//#if MC < 1.12
    this.getEquipmentInSlot(0)
//#else
//$$ this.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND)
//#endif
