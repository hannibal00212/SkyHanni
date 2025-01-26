package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.SkyHanniWarpEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.entity.EntityMoveEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.getLorenzVec
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.Entity
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object EntityMovementData {

    /**
     * REGEX-TEST: §7Sending a visit request...
     * REGEX-TEST: §7Finding player...
     * REGEX-TEST: §7Warping you to your SkyBlock island...
     */
    private val warpingPattern by RepoPattern.pattern(
        "data.entity.warping",
        "§7(?:Warping|Warping you to your SkyBlock island|Warping using transfer token|Finding player|Sending a visit request)\\.\\.\\.",
    )

    private var nextTeleport: OnNextTeleport? = null

    fun onNextTeleport(island: IslandType, action: () -> Unit) {
        nextTeleport = OnNextTeleport(island, action)
    }

    class OnNextTeleport(val island: IslandType, val action: () -> Unit) {
        val startTime: SimpleTimeMark = SimpleTimeMark.now()
    }

    private val entityLocation = mutableMapOf<Entity, LorenzVec>()

    fun addToTrack(entity: Entity) {
        if (entity !in entityLocation) {
            entityLocation[entity] = entity.getLorenzVec()
        }
    }

    @HandleEvent
    fun onIslandChange(event: IslandChangeEvent) {
        val nextData = nextTeleport ?: return
        if (nextData.island != event.newIsland) return
        val passedSince = nextData.startTime.passedSince()
        if (passedSince > 5.seconds) {
            nextTeleport = null
            return
        }

        DelayedRun.runDelayed(100.milliseconds) {
            nextData.action()
        }
        nextTeleport = null
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onPlayerMove(event: EntityMoveEvent<EntityPlayerSP>) {
        if (!event.isLocalPlayer) return

        val nextData = nextTeleport ?: return

        val passedSince = nextData.startTime.passedSince()
        if (passedSince > 5.seconds) {
            nextTeleport = null
            return
        }
        if (passedSince > 50.milliseconds && nextData.island.isInIsland()) {
            nextData.action()
            nextTeleport = null
            return
        }
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onTick(event: SkyHanniTickEvent) {
        addToTrack(Minecraft.getMinecraft().thePlayer)

        for (entity in entityLocation.keys) {
            if (entity.isDead) continue

            val newLocation = entity.getLorenzVec()
            val oldLocation = entityLocation[entity]!!
            val distance = newLocation.distance(oldLocation)
            if (distance > 0.01) {
                entityLocation[entity] = newLocation
                EntityMoveEvent(entity, oldLocation, newLocation, distance).post()
            }
        }
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onChat(event: SkyHanniChatEvent) {
        if (!warpingPattern.matches(event.message)) return
        DelayedRun.runNextTick {
            SkyHanniWarpEvent.post()
        }
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        entityLocation.clear()
    }
}
