package at.hannibal2.skyhanni.features.garden.pests

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.ClickType
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.ItemClickEvent
import at.hannibal2.skyhanni.events.ReceiveParticleEvent
import at.hannibal2.skyhanni.events.garden.pests.PestUpdateEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.events.minecraft.packet.PacketReceivedEvent
import at.hannibal2.skyhanni.features.garden.GardenApi
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.editCopy
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayerIgnoreY
import at.hannibal2.skyhanni.utils.LocationUtils.playerLocation
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawLineToEye
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SkyHanniVec3d
import net.minecraft.client.Minecraft
import net.minecraft.network.play.server.S0EPacketSpawnObject
import net.minecraft.util.EnumParticleTypes
import java.awt.Color
import kotlin.math.absoluteValue
import kotlin.time.Duration.Companion.seconds

// TODO delete workaround class PestParticleLine when this class works again
@SkyHanniModule
object PestParticleWaypoint {

    private val config get() = SkyHanniMod.feature.garden.pests.pestWaypoint

    private var lastPestTrackerUse = SimpleTimeMark.farPast()

    private var firstParticlePoint: SkyHanniVec3d? = null
    private var secondParticlePoint: SkyHanniVec3d? = null
    private var lastParticlePoint: SkyHanniVec3d? = null
    private var guessPoint: SkyHanniVec3d? = null
    private var locations = listOf<SkyHanniVec3d>()
    private var particles = 0
    private var lastParticles = 0
    private var isPointingToPest = false
    private var color: Color? = null

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onItemClick(event: ItemClickEvent) {
        if (!isEnabled()) return
        if (PestApi.hasVacuumInHand()) {
            if (event.clickType == ClickType.LEFT_CLICK && !Minecraft.getMinecraft().thePlayer.isSneaking) {
                reset()
                lastPestTrackerUse = SimpleTimeMark.now()
            }
        }
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        reset()
    }

    private fun reset() {
        lastPestTrackerUse = SimpleTimeMark.farPast()
        locations = emptyList()
        guessPoint = null
        lastParticlePoint = null
        firstParticlePoint = null
        secondParticlePoint = null
        particles = 0
        lastParticles = 0
        isPointingToPest = false
    }

    @HandleEvent(priority = HandleEvent.LOW, receiveCancelled = true, onlyOnIsland = IslandType.GARDEN)
    fun onReceiveParticle(event: ReceiveParticleEvent) {
        if (!isEnabled()) return
        if (event.type != EnumParticleTypes.REDSTONE || event.speed != 1f) return

        val darkYellow = SkyHanniVec3d(0.0, 0.8, 0.0)
        val yellow = SkyHanniVec3d(0.8, 0.8, 0.0)
        val redPest = SkyHanniVec3d(0.8, 0.4, 0.0)
        val redPlot = SkyHanniVec3d(0.8, 0.0, 0.0)
        isPointingToPest = when (event.offset.roundTo(5)) {
            redPlot -> false
            redPest, yellow, darkYellow -> true
            else -> return
        }

        val location = event.location

        if (config.hideParticles) event.cancel()
        if (lastPestTrackerUse.passedSince() > 3.seconds) return

        if (particles > 5) return
        if (firstParticlePoint == null) {
            if (playerLocation().distance(location) > 5) return
            firstParticlePoint = location
            val (r, g, b) = event.offset.toDoubleArray().map { it.toFloat() }
            color = Color(r, g, b)
        } else if (secondParticlePoint == null) {
            secondParticlePoint = location
            lastParticlePoint = location
            locations = locations.editCopy {
                add(location)
            }
        } else {
            val firstDistance = secondParticlePoint?.let { firstParticlePoint?.distance(it) } ?: return
            val distance = lastParticlePoint?.distance(location) ?: return
            if ((distance - firstDistance).absoluteValue > 0.1) return
            lastParticlePoint = location
            locations = locations.editCopy {
                add(location)
            }
        }
        ++particles
    }

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onFireWorkSpawn(event: PacketReceivedEvent) {
        if (event.packet !is S0EPacketSpawnObject) return
        if (!config.hideParticles) return
        val fireworkId = 76
        if (event.packet.type == fireworkId) event.cancel()
    }

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!isEnabled()) return
        if (locations.isEmpty()) return
        if (lastPestTrackerUse.passedSince() > config.showForSeconds.seconds) {
            reset()
            return
        }

        val waypoint = getWaypoint() ?: return

        val text = if (isPointingToPest) "§aPest Guess" else "§cInfested Plot Guess"
        val color = color ?: error("color is null")

        event.drawWaypointFilled(waypoint, color, beacon = true)
        event.drawDynamicText(waypoint, text, 1.3)
        if (config.drawLine) event.drawLineToEye(
            waypoint,
            color,
            3,
            false,
        )
    }

    private fun getWaypoint() = if (lastParticles != particles || guessPoint == null) {
        calculateWaypoint()?.also {
            guessPoint = it
            lastParticles = particles
        }
    } else guessPoint

    @HandleEvent
    fun onTick(event: SkyHanniTickEvent) {
        if (!isEnabled()) return
        val guessPoint = guessPoint ?: return

        if (guessPoint.distanceToPlayerIgnoreY() > 8) return
        if (isPointingToPest && lastPestTrackerUse.passedSince() !in 1.seconds..config.showForSeconds.seconds) return
        reset()
    }

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onPestUpdate(event: PestUpdateEvent) {
        if (PestApi.scoreboardPests == 0) reset()
    }

    private fun calculateWaypoint(): SkyHanniVec3d? {
        val firstParticle = firstParticlePoint ?: return null
        val list = locations.toList()
        var pos = SkyHanniVec3d(0.0, 0.0, 0.0)
        for ((i, particle) in list.withIndex()) {
            pos += (particle - firstParticle) / (i.toDouble() + 1.0)
        }
        return firstParticle + pos * (120.0 / list.size)
    }

    fun isEnabled() = GardenApi.inGarden() && config.enabled

}
