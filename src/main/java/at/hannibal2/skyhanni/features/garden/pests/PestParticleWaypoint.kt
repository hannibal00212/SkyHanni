package at.hannibal2.skyhanni.features.garden.pests

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.ClickType
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.ItemClickEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.ReceiveParticleEvent
import at.hannibal2.skyhanni.events.garden.pests.PestUpdateEvent
import at.hannibal2.skyhanni.events.minecraft.packet.PacketReceivedEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.editCopy
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayerIgnoreY
import at.hannibal2.skyhanni.utils.LocationUtils.playerLocation
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawLineToEye
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import net.minecraft.client.Minecraft
import net.minecraft.network.play.server.S0EPacketSpawnObject
import net.minecraft.util.EnumParticleTypes
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import kotlin.math.absoluteValue
import kotlin.time.Duration.Companion.seconds

// TODO delete workaround class PestParticleLine when this class works again
@SkyHanniModule
object PestParticleWaypoint {

    private val config get() = SkyHanniMod.feature.garden.pests.pestWaypoint

    private var lastPestTrackerUse = SimpleTimeMark.farPast()

    private var firstParticlePoint: LorenzVec? = null
    private var secondParticlePoint: LorenzVec? = null
    private var lastParticlePoint: LorenzVec? = null
    private var guessPoint: LorenzVec? = null
    private var locations = listOf<LorenzVec>()
    private var particles = 0
    private var lastParticles = 0
    private var isPointingToPest = false
    private var color: Color? = null

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onItemClick(event: ItemClickEvent) {
        if (!isEnabled()) return
        if (PestAPI.hasVacuumInHand()) {
            if (event.clickType == ClickType.LEFT_CLICK && !Minecraft.getMinecraft().thePlayer.isSneaking) {
                reset()
                lastPestTrackerUse = SimpleTimeMark.now()
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
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

    @SubscribeEvent(priority = EventPriority.LOW, receiveCanceled = true)
    fun onReceiveParticle(event: ReceiveParticleEvent) {
        if (!isEnabled()) return
        if (event.type != EnumParticleTypes.REDSTONE || event.speed != 1f) return

        val darkYellow = LorenzVec(0.0, 0.8, 0.0)
        val yellow = LorenzVec(0.8, 0.8, 0.0)
        val redPest = LorenzVec(0.8, 0.4, 0.0)
        val redPlot = LorenzVec(0.8, 0.0, 0.0)
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

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
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

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return
        val guessPoint = guessPoint ?: return

        if (guessPoint.distanceToPlayerIgnoreY() > 8) return
        if (isPointingToPest && lastPestTrackerUse.passedSince() !in 1.seconds..config.showForSeconds.seconds) return
        reset()
    }

    @HandleEvent
    fun onPestUpdate(event: PestUpdateEvent) {
        if (PestAPI.scoreboardPests == 0) reset()
    }

    private fun calculateWaypoint(): LorenzVec? {
        val firstParticle = firstParticlePoint ?: return null
        val list = locations.toList()
        var pos = LorenzVec(0.0, 0.0, 0.0)
        for ((i, particle) in list.withIndex()) {
            pos += (particle - firstParticle) / (i.toDouble() + 1.0)
        }
        return firstParticle + pos * (120.0 / list.size)
    }

    fun isEnabled() = GardenAPI.inGarden() && config.enabled

}
