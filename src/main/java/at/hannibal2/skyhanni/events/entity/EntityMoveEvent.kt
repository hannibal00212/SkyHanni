package at.hannibal2.skyhanni.events.entity

import at.hannibal2.skyhanni.api.event.GenericSkyHanniEvent
import at.hannibal2.skyhanni.utils.SkyHanniVec3d
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity

class EntityMoveEvent<T : Entity>(
    val entity: T,
    val oldLocation: SkyHanniVec3d,
    val newLocation: SkyHanniVec3d,
    val distance: Double,
) : GenericSkyHanniEvent<T>(entity.javaClass) {
    val isLocalPlayer get() = entity == Minecraft.getMinecraft().thePlayer
}
