package at.hannibal2.skyhanni.events.entity

import at.hannibal2.skyhanni.api.event.GenericSkyHanniEvent
import net.minecraft.entity.Entity

data class EntityCustomNameUpdateEvent<T : Entity>(
    val entity: T,
    val newName: String?,
) : GenericSkyHanniEvent<T>(entity.javaClass)
