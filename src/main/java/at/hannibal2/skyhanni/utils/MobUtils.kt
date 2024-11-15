package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.data.mob.Mob
import at.hannibal2.skyhanni.data.mob.MobData
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.EntityUtils.cleanName
import at.hannibal2.skyhanni.utils.LocationUtils.distanceTo
import at.hannibal2.skyhanni.utils.LocationUtils.rayIntersects
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.player.EntityPlayer

@SkyHanniModule
object MobUtils {
    val defaultArmorStandName by RepoPattern.pattern("armorstand.default", "Armou?r Stand")

    // The corresponding ArmorStand for a mob has always the ID + 1 (with some exceptions)
    fun getArmorStand(entity: Entity, offset: Int = 1) = getNextEntity(entity, offset) as? EntityArmorStand

    fun getNextEntity(entity: Entity, offset: Int) = EntityUtils.getEntityByID(entity.entityId + offset)

    fun getArmorStandByRangeAll(entity: Entity, range: Double) =
        EntityUtils.getEntitiesNearby<EntityArmorStand>(entity.getLorenzVec(), range)

    fun getClosestArmorStand(entity: Entity, range: Double) =
        getArmorStandByRangeAll(entity, range).sortedBy { it.distanceTo(entity) }.firstOrNull()

    fun getClosestArmorStandWithName(entity: Entity, range: Double, name: String) =
        getArmorStandByRangeAll(entity, range).filter { it.cleanName().startsWith(name) }
            .sortedBy { it.distanceTo(entity) }.firstOrNull()

    fun EntityArmorStand.isDefaultValue() = defaultArmorStandName.matches(this.name)

    fun EntityArmorStand?.takeNonDefault() = this?.takeIf { !it.isDefaultValue() }

    class OwnerShip(val ownerName: String) {
        val ownerPlayer = MobData.players.firstOrNull { it.name == ownerName }
        override fun equals(other: Any?): Boolean {
            if (other is EntityPlayer) return ownerPlayer == other || ownerName == other.name
            if (other is String) return ownerName == other
            return false
        }

        override fun hashCode(): Int {
            return ownerName.hashCode()
        }
    }

    fun rayTraceForMob(entity: Entity, distance: Double, partialTicks: Float, offset: LorenzVec = LorenzVec()) =
        rayTraceForMob(entity, partialTicks, offset)?.takeIf {
            it.baseEntity.distanceTo(entity.getLorenzVec()) <= distance
        }

    fun rayTraceForMobs(
        entity: Entity,
        distance: Double,
        partialTicks: Float,
        offset: LorenzVec = LorenzVec(),
    ) =
        rayTraceForMobs(entity, partialTicks, offset)?.filter {
            it.baseEntity.distanceTo(entity.getLorenzVec()) <= distance
        }.takeIf { it?.isNotEmpty() ?: false }

    fun rayTraceForMob(entity: Entity, partialTicks: Float, offset: LorenzVec = LorenzVec()) =
        rayTraceForMobs(entity, partialTicks, offset)?.firstOrNull()

    fun rayTraceForMobs(entity: Entity, partialTicks: Float, offset: LorenzVec = LorenzVec()): List<Mob>? {
        val pos = entity.getPositionEyes(partialTicks).toLorenzVec() + offset
        val look = entity.getLook(partialTicks).toLorenzVec().normalize()
        val possibleEntities = MobData.entityToMob.filterKeys {
            it !is EntityArmorStand &&
                it.entityBoundingBox.rayIntersects(
                    pos, look
                )
        }.values
        if (possibleEntities.isEmpty()) return null
        return possibleEntities.distinct().sortedBy { it.baseEntity.distanceTo(pos) }
    }

    val EntityLivingBase.mob: Mob? get() = MobData.entityToMob[this]

    val Entity.mob: Mob? get() = (this as? EntityLivingBase)?.mob

}
