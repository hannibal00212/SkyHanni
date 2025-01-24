package at.hannibal2.skyhanni.events.entity

import at.hannibal2.skyhanni.api.event.GenericSkyHanniEvent
import net.minecraft.entity.Entity
import net.minecraft.util.ChatComponentText

class EntityDisplayNameEvent<T : Entity>(val entity: T, var chatComponent: ChatComponentText) : GenericSkyHanniEvent<T>(entity.javaClass)
