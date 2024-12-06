package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import net.minecraft.entity.EntityLivingBase

class EntityMaxHealthUpdateEvent(val entity: EntityLivingBase, val maxHealth: Int) : SkyHanniEvent()
