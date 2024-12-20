package at.hannibal2.skyhanni.data.mob

import at.hannibal2.skyhanni.data.mob.Mob.Type
import at.hannibal2.skyhanni.data.mob.MobFilter.summonOwnerPattern
import at.hannibal2.skyhanni.events.MobEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.utils.CollectionUtils.toSingletonListOrEmpty
import at.hannibal2.skyhanni.utils.ColorUtils.addAlpha
import at.hannibal2.skyhanni.utils.EntityUtils.canBeSeen
import at.hannibal2.skyhanni.utils.EntityUtils.cleanName
import at.hannibal2.skyhanni.utils.EntityUtils.isCorrupted
import at.hannibal2.skyhanni.utils.EntityUtils.isRunic
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LocationUtils.getBoxCenter
import at.hannibal2.skyhanni.utils.LocationUtils.union
import at.hannibal2.skyhanni.utils.LorenzUtils.baseMaxHealth
import at.hannibal2.skyhanni.utils.MobUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.compat.getWholeInventory
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityZombie
import net.minecraft.util.AxisAlignedBB
import java.awt.Color
import java.util.UUID

/**
 * Represents a Mob in Hypixel Skyblock.
 *
 * @property baseEntity The main entity representing the Mob.
 *
 * Avoid caching, as it may change without notice.
 * @property mobType The type of the Mob.
 * @property armorStand The armor stand entity associated with the Mob, if it has one.
 *
 * Avoid caching, as it may change without notice.
 * @property name The name of the Mob.
 * @property extraEntities Additional entities associated with the Mob.
 *
 * Avoid caching, as they may change without notice.
 * @property owner Valid for: [Type.SUMMON], [Type.SLAYER]
 *
 * The owner of the Mob.
 * @property hasStar Valid for: [Type.DUNGEON]
 *
 * Indicates whether the Mob has a star.
 * @property attribute Valid for: [Type.DUNGEON]
 *
 * The attribute of the Mob.
 * @property levelOrTier Valid for: [Type.BASIC], [Type.SLAYER]
 *
 * The level or tier of the Mob.
 * @property hologram1 Valid for: [Type.BASIC], [Type.SLAYER]
 *
 * Gives back the first additional armor stand.
 *
 *   (should be called in the [MobEvent.Spawn] since it is a lazy)
 * @property hologram2 Valid for: [Type.BASIC], [Type.SLAYER]
 *
 * Gives back the second additional armor stand.
 *
 *   (should be called in the [MobEvent.Spawn] since it is a lazy)
 * @property id Unique identifier for each Mob instance
 */
class Mob(
    var baseEntity: EntityLivingBase,
    val mobType: Type,
    var armorStand: EntityArmorStand? = null,
    val name: String = "",
    additionalEntities: List<EntityLivingBase>? = null,
    ownerName: String? = null,
    val hasStar: Boolean = false,
    val attribute: MobFilter.DungeonAttribute? = null,
    val levelOrTier: Int = -1,
) {

    private val id: UUID = UUID.randomUUID()

    val owner: MobUtils.OwnerShip?

    val hologram1Delegate = lazy { MobUtils.getArmorStand(armorStand ?: baseEntity, 1) }
    val hologram2Delegate = lazy { MobUtils.getArmorStand(armorStand ?: baseEntity, 2) }

    val hologram1 by hologram1Delegate
    val hologram2 by hologram2Delegate

    private val extraEntitiesList = additionalEntities?.toMutableList() ?: mutableListOf()
    private var relativeBoundingBox: AxisAlignedBB?

    val extraEntities: List<EntityLivingBase> = extraEntitiesList

    enum class Type {
        DISPLAY_NPC,
        SUMMON,
        BASIC,
        DUNGEON,
        BOSS,
        SLAYER,
        PLAYER,
        PROJECTILE,
        SPECIAL,
        ;

        fun isSkyblockMob() = when (this) {
            BASIC, DUNGEON, BOSS, SLAYER -> true
            else -> false
        }
    }

    val isCorrupted get() = !RiftAPI.inRift() && baseEntity.isCorrupted() // Can change
    val isRunic = !RiftAPI.inRift() && baseEntity.isRunic() // Does not Change

    fun isInRender() = baseEntity.distanceToPlayer() < MobData.ENTITY_RENDER_RANGE_IN_BLOCKS

    fun canBeSeen(viewDistance: Number = 150) = baseEntity.canBeSeen(viewDistance)

    fun isInvisible() = baseEntity !is EntityZombie && baseEntity.isInvisible && baseEntity.getWholeInventory().isNullOrEmpty()

    private var highlightColor: Color? = null

    /** If [color] has no alpha or alpha is set to 255 it will set the alpha to 127
     * If [color] is set to null it removes a highlight*/
    fun highlight(color: Color?) {
        if (color == highlightColor) return
        if (color == null) {
            internalRemoveColor()
            highlightColor = null
        } else {
            highlightColor = color.takeIf { it.alpha == 255 }?.addAlpha(127) ?: color
            internalHighlight()
        }
    }

    private fun internalHighlight() {
        highlightColor?.let { color ->
            RenderLivingEntityHelper.setEntityColorWithNoHurtTime(baseEntity, color.rgb) { !this.isInvisible() }
            extraEntities.forEach {
                RenderLivingEntityHelper.setEntityColorWithNoHurtTime(it, color.rgb) { !this.isInvisible() }
            }
        }
    }

    private fun internalRemoveColor() {
        if (highlightColor == null) return
        RenderLivingEntityHelper.removeCustomRender(baseEntity)
        extraEntities.forEach {
            RenderLivingEntityHelper.removeCustomRender(it)
        }
    }

    val boundingBox: AxisAlignedBB
        get() = relativeBoundingBox?.offset(baseEntity.posX, baseEntity.posY, baseEntity.posZ)
            ?: baseEntity.entityBoundingBox

    val health: Float get() = baseEntity.health
    val maxHealth: Int get() = baseEntity.baseMaxHealth

    init {
        removeExtraEntitiesFromChecking()
        relativeBoundingBox =
            if (extraEntities.isNotEmpty()) makeRelativeBoundingBox() else null // Inlined updateBoundingBox()

        owner = (
            ownerName ?: if (mobType == Type.SLAYER) hologram2?.let {
                summonOwnerPattern.matchMatcher(it.cleanName()) { group("name") }
            } else null
            )?.let { MobUtils.OwnerShip(it) }
    }

    private fun removeExtraEntitiesFromChecking() =
        extraEntities.count { MobData.retries[it.entityId] != null }.also {
            MobData.externRemoveOfRetryAmount += it
        }

    private fun updateBoundingBox() {
        relativeBoundingBox = if (extraEntities.isNotEmpty()) makeRelativeBoundingBox() else null
    }

    private fun makeRelativeBoundingBox() = (
        baseEntity.entityBoundingBox.union(
            extraEntities.filter { it !is EntityArmorStand }
                .mapNotNull { it.entityBoundingBox },
        )
        )?.offset(-baseEntity.posX, -baseEntity.posY, -baseEntity.posZ)

    fun fullEntityList() =
        baseEntity.toSingletonListOrEmpty() +
            armorStand.toSingletonListOrEmpty() +
            extraEntities

    fun makeEntityToMobAssociation() =
        fullEntityList().associateWith { this }

    internal fun internalAddEntity(entity: EntityLivingBase) {
        internalRemoveColor()
        if (baseEntity.entityId > entity.entityId) {
            extraEntitiesList.add(0, baseEntity)
            baseEntity = entity
        } else {
            extraEntitiesList.add(extraEntitiesList.lastIndex + 1, entity)
        }
        internalHighlight()
        updateBoundingBox()
        MobData.entityToMob[entity] = this
    }

    internal fun internalAddEntity(entities: Collection<EntityLivingBase>) {
        val list = entities.drop(1).toMutableList().apply { add(baseEntity) }
        internalRemoveColor()
        extraEntitiesList.addAll(0, list)
        baseEntity = entities.first()
        internalHighlight()
        updateBoundingBox()
        removeExtraEntitiesFromChecking()
        MobData.entityToMob.putAll(entities.associateWith { this })
    }

    internal fun internalUpdateOfEntity(entity: EntityLivingBase) {
        internalRemoveColor()
        when (entity.entityId) {
            baseEntity.entityId -> {
                baseEntity = entity
            }

            armorStand?.entityId ?: Int.MIN_VALUE -> armorStand = entity as EntityArmorStand
            else -> {
                extraEntitiesList.remove(entity)
                extraEntitiesList.add(entity)
                Unit // To make return type of this branch Unit
            }
        }
        internalHighlight()
    }

    val centerCords get() = boundingBox.getBoxCenter()

    override fun hashCode() = id.hashCode()

    override fun toString(): String = "$name - ${baseEntity.entityId}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Mob) return false

        return id == other.id
    }

    // TODO add max distance
    fun lineToPlayer(color: Color, lineWidth: Int = 2, depth: Boolean = true) = LineToMobHandler.register(this, color, lineWidth, depth)
    fun distanceToPlayer(): Double = baseEntity.distanceToPlayer()
}
