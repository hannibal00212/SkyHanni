package at.hannibal2.skyhanni.utils.compat

import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntitySkeleton
import net.minecraft.item.ItemStack
import net.minecraft.world.World
//#if MC > 1.12
//$$ import net.minecraft.entity.monster.EntityWitherSkeleton
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

fun Entity.getEntityLevel(): World =
//#if MC < 1.16
    this.entityWorld
//#else
//$$ this.level
//#endif

fun createWitherSkeleton(world: World?): EntityLivingBase =
//#if MC < 1.12
    EntitySkeleton(world).also { it.skeletonType = 1 }
//#else
//$$ EntityWitherSkeleton(world)
//#endif
