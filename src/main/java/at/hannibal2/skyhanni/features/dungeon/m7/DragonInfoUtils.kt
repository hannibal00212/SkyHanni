package at.hannibal2.skyhanni.features.dungeon.m7

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.mob.Mob
import at.hannibal2.skyhanni.data.mob.MobData
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.DungeonCompleteEvent
import at.hannibal2.skyhanni.events.DungeonM7Phase5Start
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.M7DragonChangeEvent
import at.hannibal2.skyhanni.events.MobEvent
import at.hannibal2.skyhanni.events.minecraft.packet.PacketReceivedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.LocationUtils.isInside
import at.hannibal2.skyhanni.utils.LorenzDebug
import at.hannibal2.skyhanni.utils.LorenzLogger
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.system.PlatformUtils
import at.hannibal2.skyhanni.utils.toLorenzVec
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.EnumParticleTypes
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object DragonInfoUtils {
    private var inPhase5 = false
    private var currentRun = 0
    private val logger = LorenzLogger("dragons")

    private var dragonSpawnCount = 0

    @SubscribeEvent
    fun onDragonSpawn(event: MobEvent.Spawn.SkyblockMob) {
        if (!isEnabled()) return
        if (event.mob.baseEntity !is EntityDragon) return
        dragonSpawnCount += 1

        if (event.mob.mobType != Mob.Type.BOSS) logLine("mobType: ${event.mob.mobType}")
        if (event.mob.name != "Withered Dragon") logLine("mobName: ${event.mob.name}")

        val location = event.mob.baseEntity.position.toLorenzVec()
        val id = event.mob.baseEntity.entityId

        val matchedDragon = WitheredDragonInfo.getClosestSpawn(location)
        if (matchedDragon == null) {
            logLine("[Spawn] dragon ${id}, '${location.toCleanString()}', no spawn matched")
            ChatUtils.debug("Unknown dragon $id spawned at ${location.toCleanString()}")
            return
        }

        logSpawn(event.mob, matchedDragon)

        matchedDragon.status = WitheredDragonSpawnedStatus.ALIVE
        matchedDragon.id = id
        M7DragonChangeEvent(matchedDragon, WitheredDragonSpawnedStatus.ALIVE).post()
    }

    private var dragonKillCount = 0


    @SubscribeEvent
    fun onDragonKill(event: LivingDeathEvent) {
        if (!isEnabled()) return
        if (event.entity !is EntityDragon) return
        dragonKillCount++

        val location = event.entity.position.toLorenzVec()
        val id = event.entity.entityId

        val matchedDragon = WitheredDragonInfo.entries.firstOrNull { it.id == id }
        if (matchedDragon == null) {
            logLine("dragon $id died, no matched dragon")
            ChatUtils.debug("Unknown dragon $id died at ${location.toCleanString()}")
            return
        }

        if (matchedDragon.deathBox.isInside(location)) matchedDragon.defeated = true
        val status = WitheredDragonSpawnedStatus.DEAD
        matchedDragon.status = status

        MobData.entityToMob[event.entityLiving]?.let { logKill(it, matchedDragon) }
        matchedDragon.id = null
        M7DragonChangeEvent(matchedDragon, status, matchedDragon.defeated).post()
    }

//     @SubscribeEvent
//     fun onDragonKill(event: MobEvent.DeSpawn.SkyblockMob) {
//         if (!isEnabled()) return
//         if (event.mob.baseEntity !is EntityDragon) return
//         dragonKillCount += 1
//
//         if (event.mob.mobType != Mob.Type.BOSS) logLine("mobType: ${event.mob.mobType}")
//         if (event.mob.name != "Withered Dragon") logLine("mobName: ${event.mob.name}")
//
//         val location = event.mob.baseEntity.position.toLorenzVec()
//         val id = event.mob.baseEntity.entityId
//         val matchedDragon = WitheredDragonInfo.entries.firstOrNull { it.id == id }
//         if (matchedDragon == null) {
//             logLine("dragon $id died, no matched dragon")
//             ChatUtils.debug("Unknown dragon $id died at ${location.toCleanString()}")
//             return
//         }
//         if (matchedDragon.deathBox.isInside(location)) matchedDragon.defeated = true
//         val status = WitheredDragonSpawnedStatus.DEAD
//         M7DragonChangeEvent(matchedDragon, status, matchedDragon.defeated)
//
//         matchedDragon.status = status
//         logKill(event.mob, matchedDragon)
//
//         matchedDragon.id = null
//     }

    @HandleEvent
    fun onParticles(event: PacketReceivedEvent) {
        if (!isEnabled()) return
        if (event.packet !is S2APacketParticles) return

        val particle = event.packet
        if (!checkParticle(particle)) return
        val location = particle.toLorenzVec()

        val matchedDragon = WitheredDragonInfo.entries.firstOrNull { it.particleBox.isInside(location) }
        logParticle(particle, matchedDragon)
        if (matchedDragon == null) return

        val status = WitheredDragonSpawnedStatus.SPAWNING
        if (matchedDragon.status == status) return
        matchedDragon.status = status
        M7DragonChangeEvent(matchedDragon, status, matchedDragon.defeated).post()
    }

    private fun checkParticle(particle: S2APacketParticles): Boolean {
        return particle.run {
            particleType == EnumParticleTypes.FLAME &&
                particleCount == 20 &&
                xOffset == 2.0f &&
                yOffset == 3.0f &&
                zOffset == 2.0f &&
                isLongDistance &&
                (xCoordinate % 1) == 0.0 &&
                (yCoordinate % 1) == 0.0 &&
                (zCoordinate % 1) == 0.0
        }
    }

    @SubscribeEvent
    fun onStart(event: DungeonM7Phase5Start) {
        if (inPhase5) return
        logLine("------ run $currentRun -------")
        logLine("Starting Phase5")
        currentRun += 1
        buildRenderable()
        inPhase5 = true
    }

    @SubscribeEvent
    fun onEnd(event: DungeonCompleteEvent) {
        WitheredDragonInfo.clearSpawned()
        if (inPhase5) inPhase5 = false
        val message = "spawned:$dragonSpawnCount | died:$dragonKillCount"
        logLine(message)
        DelayedRun.runDelayed(2.seconds) {
            ChatUtils.chat(message)
        }
        dragonKillCount = 0
        dragonSpawnCount = 0
    }

    @SubscribeEvent
    fun onLeave(event: LorenzWorldChangeEvent) {
        WitheredDragonInfo.clearSpawned()
        if (inPhase5) inPhase5 = false
    }

    @SubscribeEvent
    fun onDebug(event: DebugDataCollectEvent) {
        event.title("M7 Dragon Info")
        if (!isEnabled()) {
            event.addIrrelevant("not in phase5")
            return
        }

        event.addData {
            add("Power: ${WitheredDragonInfo.POWER.status}, ${WitheredDragonInfo.POWER.id}")
            add("Flame: ${WitheredDragonInfo.FLAME.status}, ${WitheredDragonInfo.FLAME.id}")
            add("Apex: ${WitheredDragonInfo.APEX.status}, ${WitheredDragonInfo.APEX.id}")
            add("Ice: ${WitheredDragonInfo.ICE.status}, ${WitheredDragonInfo.ICE.id}")
            add("Soul: ${WitheredDragonInfo.SOUL.status}, ${WitheredDragonInfo.SOUL.id}")
        }
    }

    private fun logParticle(particle: S2APacketParticles, matchedType: WitheredDragonInfo?) {
        val location = particle.toLorenzVec()

        var string = "[Particle] $location"
        string += if (matchedType != null) {
            ", matched $matchedType"
        } else {
            ", did not match"
        }

        logLine(string)
    }

    private fun logSpawn(mob: Mob, matchedType: WitheredDragonInfo?) {
        val location = mob.baseEntity.position.toLorenzVec()

        var string = "[Spawn] $location, ${mob.baseEntity.entityId}"
        string += if (matchedType != null) {
            ", matched $matchedType"
        } else {
            ", did not match"
        }
        logLine(string)
    }

    private fun logKill(mob: Mob, matchedType: WitheredDragonInfo?) {
        val location = mob.baseEntity.position.toLorenzVec()

        val baseEntity = mob.baseEntity
        baseEntity as EntityDragon
        var string = "[Death] $location, ${baseEntity.entityId}, ${baseEntity.animTime}"
        string += if (matchedType != null) {
            ", matched $matchedType, ${matchedType.defeated}"
        } else {
            ", did not match"
        }
        logLine(string)
    }

    private fun logLine(input: String) {
        logger.log(input)
        LorenzDebug.log(input)
    }

    fun isEnabled() = inPhase5 || PlatformUtils.isDevEnvironment

    @SubscribeEvent
    fun renderBoxes(event: LorenzRenderWorldEvent) {
//         if (!isEnabled()) return
//         if (!SkyHanniMod.feature.dev.debug.enabled) return
//         WitheredDragonInfo.entries.forEach {
//             event.drawFilledBoundingBox_nea(it.deathBox, it.color.toColor().addAlpha(100))
//             event.drawWaypointFilled(it.spawnLocation, it.color.toColor(), true)
//             event.drawString(it.spawnLocation.add(y = 1), it.colorName, true)
//         }
    }

    @HandleEvent
    fun onDragonChange(event: M7DragonChangeEvent) {
        ChatUtils.debug("${event.dragon} ${event.state} ${event.defeated}")
        buildRenderable()
    }

    private val config get() = SkyHanniMod.feature.dungeon.m7config
    private var renderable = listOf<Renderable>()

    private fun buildRenderable() {
        val list = mutableListOf<Renderable>()
        list.add(Renderable.string("§5§lWithered Dragon Info"))
        for (dragon in WitheredDragonInfo.entries) {
            list.add(
                Renderable.string("§${dragon.color.chatColorCode}${dragon.colorName}§f: ${dragon.status} ${getSymbol(dragon.defeated)}"),
            )
        }

        renderable = list.toList()
    }

    private fun getSymbol(isDefeated: Boolean): String {
        return if (isDefeated) "§a✔"
        else "§c✖"
    }

    @SubscribeEvent
    fun onRender(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!config.dragonStatusGUI) return
        if (!inPhase5) return

        config.dragonStatusPosition.renderRenderables(renderable, posLabel = "Withered Dragon info")
    }
}
