package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.features.combat.damageindicator.EntityData
import net.minecraft.entity.EntityLivingBase

class DamageIndicatorDeathEvent(val entity: EntityLivingBase, val data: EntityData) : LorenzEvent()

