package at.hannibal2.skyhanni.data.mob

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.mob.MobFilter.isDisplayNPC
import at.hannibal2.skyhanni.data.mob.MobFilter.isRealPlayer
import at.hannibal2.skyhanni.data.mob.MobFilter.isSkyBlockMob
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.EntityHealthUpdateEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.MobEvent
import at.hannibal2.skyhanni.events.minecraft.ClientDisconnectEvent
import at.hannibal2.skyhanni.events.minecraft.packet.PacketReceivedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.drainForEach
import at.hannibal2.skyhanni.utils.CollectionUtils.drainTo
import at.hannibal2.skyhanni.utils.CollectionUtils.put
import at.hannibal2.skyhanni.utils.CollectionUtils.refreshReference
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.getLorenzVec
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityCreeper
import net.minecraft.entity.passive.EntityBat
import net.minecraft.entity.passive.EntityVillager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.S01PacketJoinGame
import net.minecraft.network.play.server.S0CPacketSpawnPlayer
import net.minecraft.network.play.server.S0FPacketSpawnMob
import net.minecraft.world.World
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

@SkyHanniModule
object MobDetection {

    /* Unsupported Entities
        Nicked Players
        Odonata
        Silk Worm
        Fairy (in Dungeon)
        Totem of Corruption
        Worm
        Scatha
        Butterfly
        Exe
        Wai
        Zee
     */

    private const val MAX_RETRIES = 20 * 5

    private val forceReset get() = !SkyHanniMod.feature.dev.mobDebug.enable

    private var shouldClear: AtomicBoolean = AtomicBoolean(false)

    private fun mobDetectionReset() {
        MobData.currentMobs.map {
            it.createDeSpawnEvent()
        }.forEach { it.postAndCatch() }
        MobData.retries.clear()
    }

    // TODO this is a unused debug funciton. maybe connect with a debug commmand or remove
    private fun watchdog() {
        val world = LorenzUtils.getPlayer()?.worldObj ?: return
        if (MobData.retries.any { it.value.entity.worldObj != world }) {
            ChatUtils.chat("Watchdog: Retires")
        }
        if (MobData.currentMobs.any { it.watchdogCheck(world) }) {
            ChatUtils.chat("Watchdog: Current Mobs")
        }
        if (MobData.players.any { it.watchdogCheck(world) }) {
            ChatUtils.chat("Watchdog: Players")
        }
        if (MobData.displayNPCs.any { it.watchdogCheck(world) }) {
            ChatUtils.chat("Watchdog: Display NPCs")
        }
        if (MobData.skyblockMobs.any { it.watchdogCheck(world) }) {
            ChatUtils.chat("Watchdog: SkyBlockMobs")
        }
        if (MobData.summoningMobs.any { it.watchdogCheck(world) }) {
            ChatUtils.chat("Watchdog: Summoning")
        }
        if (MobData.special.any { it.watchdogCheck(world) }) {
            ChatUtils.chat("Watchdog: Special")
        }
        if (MobData.notSeenMobs.any { it.watchdogCheck(world) }) {
            ChatUtils.chat("Watchdog: Not Seen Mobs")
        }
    }

    private fun Mob.watchdogCheck(world: World): Boolean =
        this.baseEntity.worldObj != world || (this.armorStand?.let { it.worldObj != world }
            ?: false) || this.extraEntities.any { it.worldObj != world }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (shouldClear.get()) { // Needs to work outside skyblock since it needs clearing when leaving skyblock and joining limbo
            mobDetectionReset()
            shouldClear.set(false)
        }
        if (!LorenzUtils.inSkyBlock) return

        makeEntityReferenceUpdate()

        handleMobsFromPacket()

        handleRetries()

        MobData.previousEntityLiving.clear()
        MobData.previousEntityLiving.addAll(MobData.currentEntityLiving)
        MobData.currentEntityLiving.clear()
        MobData.currentEntityLiving.addAll(
            EntityUtils.getEntities<EntityLivingBase>()
                .filter { it !is EntityArmorStand && it !is EntityPlayerSP },
        )

        if (forceReset) {
            MobData.currentEntityLiving.clear() // Naturally removing the mobs using the despawn
        }

        (MobData.currentEntityLiving - MobData.previousEntityLiving).forEach { addRetry(it) } // Spawn
        (MobData.previousEntityLiving - MobData.currentEntityLiving).forEach { entityDeSpawn(it) } // Despawn

        MobData.notSeenMobs.removeIf(::canBeSeen)

        if (forceReset) {
            mobDetectionReset() // Ensure that all mobs are cleared 100%
        }
    }

    /** Splits the entity into player, displayNPC and other */
    private fun EntityLivingBase.getRoughType() = when {
        this is EntityPlayer && this.isRealPlayer() -> Mob.Type.PLAYER
        this.isDisplayNPC() -> Mob.Type.DISPLAY_NPC
        this.isSkyBlockMob() && !islandException() -> Mob.Type.BASIC
        else -> null
    }

    private fun addRetry(entity: EntityLivingBase) = entity.getRoughType()?.let { type ->
        val re = MobData.RetryEntityInstancing(entity, 0, type)
        MobData.retries.put(re.toKeyValuePair())
    }

    private fun removeRetry(entity: EntityLivingBase) = MobData.retries.remove(entity.entityId)

    private fun getRetry(entity: EntityLivingBase) = MobData.retries[entity.entityId]

    /** @return always true */
    private fun mobDetectionError(string: String) = MobData.logger.log(string).let { true }

    private fun canBeSeen(mob: Mob): Boolean {
        val isVisible = !mob.isInvisible() && mob.canBeSeen()
        if (isVisible) when (mob.mobType) {
            Mob.Type.PLAYER -> MobEvent.FirstSeen.Player(mob)
            Mob.Type.SUMMON -> MobEvent.FirstSeen.Summon(mob)
            Mob.Type.SPECIAL -> MobEvent.FirstSeen.Special(mob)
            Mob.Type.PROJECTILE -> MobEvent.FirstSeen.Projectile(mob)
            Mob.Type.DISPLAY_NPC -> MobEvent.FirstSeen.DisplayNPC(mob)
            Mob.Type.BASIC, Mob.Type.DUNGEON, Mob.Type.BOSS, Mob.Type.SLAYER -> MobEvent.FirstSeen.SkyblockMob(mob)
        }
        return isVisible
    }

    /**@return a false means that it should try again (later)*/
    private fun entitySpawn(entity: EntityLivingBase, roughType: Mob.Type): Boolean {
        when (roughType) {
            Mob.Type.PLAYER -> MobEvent.Spawn.Player(MobFactories.player(entity)).postAndCatch()

            Mob.Type.DISPLAY_NPC -> return MobFilter.createDisplayNPC(entity)
            Mob.Type.BASIC -> {
                val (result, mob) = MobFilter.createSkyblockEntity(entity)
                when (result) {
                    MobData.Result.NotYetFound -> return false
                    MobData.Result.Illegal -> return true // Remove entity from the spawning queue
                    MobData.Result.SomethingWentWrong -> return mobDetectionError("Something Went Wrong!")
                    MobData.Result.Found -> {
                        if (mob == null) return mobDetectionError("Mob is null even though result is Found")
                        when (mob.mobType) {
                            Mob.Type.SUMMON -> MobEvent.Spawn.Summon(mob)

                            Mob.Type.BASIC, Mob.Type.DUNGEON, Mob.Type.BOSS, Mob.Type.SLAYER -> MobEvent.Spawn.SkyblockMob(
                                mob,
                            )

                            Mob.Type.SPECIAL -> MobEvent.Spawn.Special(mob)
                            Mob.Type.PROJECTILE -> MobEvent.Spawn.Projectile(mob)
                            Mob.Type.DISPLAY_NPC -> MobEvent.Spawn.DisplayNPC(mob) // Needed for some special cases
                            Mob.Type.PLAYER -> return mobDetectionError("An Player Ended Here. How?")
                        }.postAndCatch()
                    }
                }
            }

            else -> return true
        }
        return true
    }

    private val entityFromPacket = ConcurrentLinkedQueue<Pair<EntityPacketType, Int>>()

    /** For mobs that have default health of the entity */
    private enum class EntityPacketType {
        SPIRIT_BAT,
        VILLAGER,
        CREEPER_VAIL,
    }

    /** Handles some mobs that have default health of the entity, specially using the [EntityHealthUpdateEvent] */
    private fun handleMobsFromPacket() = entityFromPacket.drainForEach { (type, id) ->
        when (type) {
            EntityPacketType.SPIRIT_BAT -> {
                val entity = EntityUtils.getEntityByID(id) as? EntityBat ?: return@drainForEach
                if (MobData.entityToMob[entity] != null) return@drainForEach
                removeRetry(entity)
                MobEvent.Spawn.Projectile(MobFactories.projectile(entity, "Spirit Scepter Bat")).postAndCatch()
            }

            EntityPacketType.VILLAGER -> {
                val entity = EntityUtils.getEntityByID(id) as? EntityVillager ?: return@drainForEach
                val mob = MobData.entityToMob[entity]
                if (mob != null && mob.mobType == Mob.Type.DISPLAY_NPC) {
                    MobEvent.DeSpawn.DisplayNPC(mob)
                    addRetry(entity)
                    return@drainForEach
                }
                getRetry(entity)?.let {
                    if (it.roughType == Mob.Type.DISPLAY_NPC) {
                        removeRetry(entity)
                        addRetry(entity)
                    }
                }
            }

            EntityPacketType.CREEPER_VAIL -> {
                val entity = EntityUtils.getEntityByID(id) as? EntityCreeper ?: return@drainForEach
                if (MobData.entityToMob[entity] != null) return@drainForEach
                if (!entity.powered) return@drainForEach
                removeRetry(entity)
                MobEvent.Spawn.Special(MobFactories.special(entity, "Creeper Veil")).postAndCatch()
            }
        }
    }

    @SubscribeEvent
    fun onEntityHealthUpdateEvent(event: EntityHealthUpdateEvent) {
        when {
            event.entity is EntityBat && event.health == 6 -> {
                entityFromPacket.add(EntityPacketType.SPIRIT_BAT to event.entity.entityId)
            }

            event.entity is EntityVillager && event.health != 20 -> {
                entityFromPacket.add(EntityPacketType.VILLAGER to event.entity.entityId)
            }

            event.entity is EntityCreeper && event.health == 20 -> {
                entityFromPacket.add(EntityPacketType.CREEPER_VAIL to event.entity.entityId)
            }
        }
    }

    private fun islandException(): Boolean = when (LorenzUtils.skyBlockIsland) {
        IslandType.GARDEN_GUEST -> true
        IslandType.PRIVATE_ISLAND_GUEST -> true
        else -> false
    }

    private fun entityDeSpawn(entity: EntityLivingBase) {
        MobData.entityToMob[entity]?.createDeSpawnEvent()?.postAndCatch() ?: removeRetry(entity)
        allEntitiesViaPacketId.remove(entity.entityId)
    }

    private fun Mob.createDeSpawnEvent() = when (this.mobType) {
        Mob.Type.PLAYER -> MobEvent.DeSpawn.Player(this)
        Mob.Type.SUMMON -> MobEvent.DeSpawn.Summon(this)
        Mob.Type.SPECIAL -> MobEvent.DeSpawn.Special(this)
        Mob.Type.PROJECTILE -> MobEvent.DeSpawn.Projectile(this)
        Mob.Type.DISPLAY_NPC -> MobEvent.DeSpawn.DisplayNPC(this)
        Mob.Type.BASIC, Mob.Type.DUNGEON, Mob.Type.BOSS, Mob.Type.SLAYER -> MobEvent.DeSpawn.SkyblockMob(this)
    }

    private fun handleRetries() {
        val iterator = MobData.retries.iterator()
        while (iterator.hasNext()) {
            val (_, retry) = iterator.next()

            if (MobData.externRemoveOfRetryAmount > 0) {
                iterator.remove()
                MobData.externRemoveOfRetryAmount--
                continue
            }

            if (retry.outsideRange()) continue

            val entity = retry.entity
            if (retry.times == MAX_RETRIES) {
                MobData.logger.log(
                    "`${retry.entity.name}`${retry.entity.entityId} missed {\n " +
                        "is already Found: ${MobData.entityToMob[retry.entity] != null})." +
                        "\n Position: ${retry.entity.getLorenzVec()}\n " +
                        "DistanceC: ${
                            entity.getLorenzVec().distanceChebyshevIgnoreY(LocationUtils.playerLocation())
                        }\n" +
                        "Relative Position: ${entity.getLorenzVec() - LocationUtils.playerLocation()}\n " +
                        "}",
                )
                // Uncomment this to make it closed a loop
                // iterator.remove()
                // continue
            }
            if (!entitySpawn(entity, retry.roughType)) {
                retry.times++
                continue
            }
            iterator.remove()
        }
    }

    private val entityUpdatePackets = ConcurrentLinkedQueue<Int>()
    private val entitiesThatRequireUpdate = mutableSetOf<Int>() // needs to be distinct, therefore not using a queue

    /** Refreshes the references of the entities in entitiesThatRequireUpdate */
    private fun makeEntityReferenceUpdate() {
        entitiesThatRequireUpdate.iterator().let { iterator ->
            while (iterator.hasNext()) {
                if (handleEntityUpdate(iterator.next())) iterator.remove()
            }
        }
        entityUpdatePackets.drainTo(entitiesThatRequireUpdate)
    }

    private fun handleEntityUpdate(entityID: Int): Boolean {
        val entity = EntityUtils.getEntityByID(entityID) as? EntityLivingBase ?: return false
        getRetry(entity)?.apply { this.entity = entity }
        MobData.currentEntityLiving.refreshReference(entity)
        MobData.previousEntityLiving.refreshReference(entity)
        // update map
        MobData.entityToMob[entity]?.internalUpdateOfEntity(entity)
        return true
    }

    @HandleEvent
    fun onEntitySpawnPacket(event: PacketReceivedEvent) {
        when (val packet = event.packet) {
            is S0FPacketSpawnMob -> addEntityUpdate(packet.entityID)
            is S0CPacketSpawnPlayer -> addEntityUpdate(packet.entityID)
            // is S0EPacketSpawnObject -> addEntityUpdate(packet.entityID)
            is S01PacketJoinGame -> // one of the first packets that is sent when switching servers inside the BungeeCord Network (please some prove this, I just found it out via Testing)
            {
                shouldClear.set(true)
                allEntitiesViaPacketId.clear()
            }
        }
    }

    private val allEntitiesViaPacketId = mutableSetOf<Int>()

    private fun addEntityUpdate(id: Int) = if (allEntitiesViaPacketId.contains(id)) {
        entityUpdatePackets.add(id)
    } else {
        allEntitiesViaPacketId.add(id)
    }

    @HandleEvent
    fun onDisconnect(event: ClientDisconnectEvent) {
        shouldClear.set(true)
    }

    @SubscribeEvent
    fun onDebugDataCollect(event: DebugDataCollectEvent) {
        event.title("Mob Detection")
        if (forceReset) {
            event.addData("Mob Detection is manually disabled!")
        } else {
            event.addIrrelevant {
                add("normal enabled")
                add("Active Mobs: ${MobData.currentMobs.size}")
                val inDistanceMobs = MobData.retries.count { it.value.outsideRange() }
                add("Searching for Mobs: ${MobData.retries.size - inDistanceMobs}")
                add("Mobs over Max Search Count: ${MobData.retries.count { it.value.times > MAX_RETRIES }}")
                add("Mobs outside of Range: $inDistanceMobs")
            }
        }
    }

}
