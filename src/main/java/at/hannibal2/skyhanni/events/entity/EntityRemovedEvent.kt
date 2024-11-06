package at.hannibal2.skyhanni.events.entity

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import net.minecraft.entity.Entity

class EntityRemovedEvent(val entity: Entity) : SkyHanniEvent()
