package at.hannibal2.skyhanni.data.mob

import at.hannibal2.skyhanni.events.MobEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.takeIfAllNotNull
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LorenzLogger
import at.hannibal2.skyhanni.utils.getLorenzVec
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.TreeMap
import at.hannibal2.skyhanni.data.mob.Mob.Type as MobType

@SkyHanniModule
object MobData {

    class MobSet : HashSet<Mob>() {
        val entityList get() = this.flatMap { listOf(it.baseEntity) + (it.extraEntities) }
    }

    val players = MobSet()
    val displayNPCs = MobSet()
    val skyblockMobs = MobSet()
    val summoningMobs = MobSet()
    val special = MobSet()
    val currentMobs = MobSet()

    val entityToMob = mutableMapOf<EntityLivingBase, Mob>()

    internal val notSeenMobs = MobSet()

    internal val currentEntityLiving = mutableSetOf<EntityLivingBase>()
    internal val previousEntityLiving = mutableSetOf<EntityLivingBase>()

    internal val retries = TreeMap<Int, RetryEntityInstancing>()

    const val ENTITY_RENDER_RANGE_IN_BLOCKS = 80.0 // Entity DeRender after ~5 Chunks
    const val DETECTION_RANGE = 22.0
    const val DISPLAY_NPC_DETECTION_RANGE = 24.0 // 24.0

    var externRemoveOfRetryAmount = 0

    val logger = LorenzLogger("mob/detection")

    internal enum class Result {
        Found,
        NotYetFound,
        Illegal,
        SomethingWentWrong,
    }

    internal class MobResult(val result: Result, val mob: Mob?) {
        operator fun component1() = result
        operator fun component2() = mob

        companion object {
            val illegal = MobResult(Result.Illegal, null)
            val notYetFound = MobResult(Result.NotYetFound, null)
            val somethingWentWrong = MobResult(Result.SomethingWentWrong, null)
            fun found(mob: Mob) = MobResult(Result.Found, mob)

            fun EntityArmorStand?.makeMobResult(mob: (EntityArmorStand) -> Mob?) =
                this?.let { armor ->
                    mob.invoke(armor)?.let { found(it) } ?: somethingWentWrong
                } ?: notYetFound

            fun List<EntityArmorStand?>.makeMobResult(mob: (List<EntityArmorStand>) -> Mob?) =
                this.takeIfAllNotNull()?.let { armor ->
                    mob.invoke(armor)?.let { found(it) } ?: somethingWentWrong
                } ?: notYetFound
        }
    }

    internal class RetryEntityInstancing(
        var entity: EntityLivingBase,
        var times: Int,
        val roughType: MobType,
    ) {
        override fun hashCode() = entity.entityId
        override fun equals(other: Any?) = (other as? RetryEntityInstancing).hashCode() == this.hashCode()
        fun toKeyValuePair() = entity.entityId to this

        fun outsideRange() =
            entity.getLorenzVec().distanceChebyshevIgnoreY(LocationUtils.playerLocation()) > when (roughType) {
                MobType.DISPLAY_NPC -> DISPLAY_NPC_DETECTION_RANGE
                MobType.PLAYER -> Double.POSITIVE_INFINITY
                else -> DETECTION_RANGE
            }
    }

    @SubscribeEvent
    fun onMobEventSpawn(event: MobEvent.Spawn) {
        entityToMob.putAll(event.mob.makeEntityToMobAssociation())
        currentMobs.add(event.mob)
        notSeenMobs.add(event.mob)
    }

    @SubscribeEvent
    fun onMobEventDeSpawn(event: MobEvent.DeSpawn) {
        event.mob.fullEntityList().forEach { entityToMob.remove(it) }
        currentMobs.remove(event.mob)
        notSeenMobs.remove(event.mob)
    }

    @SubscribeEvent
    fun onSkyblockMobSpawnEvent(event: MobEvent.Spawn.SkyblockMob) {
        skyblockMobs.add(event.mob)
    }

    @SubscribeEvent
    fun onSkyblockMobDeSpawnEvent(event: MobEvent.DeSpawn.SkyblockMob) {
        skyblockMobs.remove(event.mob)
    }

    @SubscribeEvent
    fun onSummonSpawnEvent(event: MobEvent.Spawn.Summon) {
        summoningMobs.add(event.mob)
    }

    @SubscribeEvent
    fun onSummonDeSpawnEvent(event: MobEvent.DeSpawn.Summon) {
        summoningMobs.remove(event.mob)
    }

    @SubscribeEvent
    fun onSpecialSpawnEvent(event: MobEvent.Spawn.Special) {
        special.add(event.mob)
    }

    @SubscribeEvent
    fun onSpecialDeSpawnEvent(event: MobEvent.DeSpawn.Special) {
        special.remove(event.mob)
    }

    @SubscribeEvent
    fun onDisplayNPCSpawnEvent(event: MobEvent.Spawn.DisplayNPC) {
        displayNPCs.add(event.mob)
    }

    @SubscribeEvent
    fun onDisplayNPCDeSpawnEvent(event: MobEvent.DeSpawn.DisplayNPC) {
        displayNPCs.remove(event.mob)
    }

    @SubscribeEvent
    fun onRealPlayerSpawnEvent(event: MobEvent.Spawn.Player) {
        players.add(event.mob)
    }

    @SubscribeEvent
    fun onRealPlayerDeSpawnEvent(event: MobEvent.DeSpawn.Player) {
        players.remove(event.mob)
    }
}
