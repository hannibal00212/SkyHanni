package at.hannibal2.skyhanni.events.entity

import at.hannibal2.skyhanni.api.event.GenericSkyHanniEvent
import net.minecraft.entity.Entity
import net.minecraft.util.DamageSource

class EntityHurtEvent<T : Entity>(val entity: T, val source: DamageSource, val amount: Float) : GenericSkyHanniEvent<T>(entity.javaClass)
