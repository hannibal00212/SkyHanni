package at.hannibal2.skyhanni.events

import net.minecraft.entity.EntityLivingBase

class EntityHealthUpdateEvent(val entity: EntityLivingBase, val health: Int) : LorenzEvent()
