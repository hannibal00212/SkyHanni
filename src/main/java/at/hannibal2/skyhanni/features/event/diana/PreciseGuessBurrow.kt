package at.hannibal2.skyhanni.features.event.diana

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.features.event.diana.DianaConfig.BurrowGuessType
import at.hannibal2.skyhanni.data.ClickType
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.ItemClickEvent
import at.hannibal2.skyhanni.events.diana.BurrowGuessEvent
import at.hannibal2.skyhanni.events.minecraft.packet.PacketReceivedEvent
import at.hannibal2.skyhanni.features.event.diana.DianaAPI.isDianaSpade
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.LorenzVec.Companion.toLorenzVec
import at.hannibal2.skyhanni.utils.PolynomialFitter
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.toLorenzVec
import net.minecraft.client.Minecraft
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.EnumParticleTypes
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object PreciseGuessBurrow {
    private val config get() = SkyHanniMod.feature.event.diana

    private val particleLocations = mutableListOf<LorenzVec>()
    private var guessPoint: LorenzVec? = null

    @HandleEvent(onlyOnIsland = IslandType.HUB)
    fun onWorldChange(event: IslandChangeEvent) {
        if (!isEnabled()) return
        guessPoint = null
        particleLocations.clear()
    }

    @HandleEvent(onlyOnIsland = IslandType.HUB)
    fun onReceiveParticle(event: PacketReceivedEvent) {
        if (!isEnabled()) return
        if (event.packet !is S2APacketParticles) return
        val packet: S2APacketParticles = event.packet
        val type = packet.particleType
        if (type != EnumParticleTypes.DRIP_LAVA) return
        val currLoc = packet.toLorenzVec()
        if (lastDianaSpade.passedSince() > 3.seconds) return
        if (particleLocations.isEmpty()) {
            // First particle spawns exactly 1 block away from the player
            if ((spadeUsePosition?.distance(currLoc) ?: 10.0) > 1.1555) return
            particleLocations.add(currLoc)
            return
        }
        val distToLast = particleLocations.last().distance(currLoc)
        if (distToLast == 0.0 || distToLast > 3.0) return
        particleLocations.add(currLoc)
        // A Degree n polynomial can be solved with n+1 unique points
        // The Bézier curve used is a degree 3, so four points are needed to solve
        if (particleLocations.size <= 3) return
        val fitters = arrayOf(PolynomialFitter(3), PolynomialFitter(3), PolynomialFitter(3))
        for ((index, location) in particleLocations.withIndex()) {
            val x = index.toDouble()
            val locationArray = location.toDoubleArray()
            for ((i, fitter) in fitters.withIndex()) {
                fitter.addPoint(x, locationArray[i])
            }
        }

        val coefficients = fitters.map { it.fit() }
        val startPointDerivative = coefficients.map { it[1] }.toLorenzVec()

        // How far away from the first point the control point is
        val controlPointDistance = sqrt(24 * sin(getPitchFromDerivative(startPointDerivative) - PI) + 25)

        val t = 3 * controlPointDistance / startPointDerivative.length()

        val guessPosition = coefficients.map { it[0] + it[1] * t + it[2] * t.pow(2) + it[3] * t.pow(3) }.toLorenzVec()

        BurrowGuessEvent(guessPosition.down(0.5).roundLocationToBlock()).post()
    }

    private fun getPitchFromDerivative(derivative: LorenzVec): Double {
        val xzLength = sqrt(derivative.x.pow(2) + derivative.z.pow(2))
        val pitchRadians = -atan2(derivative.y, xzLength)
        // Solve y = atan2(sin(x) - 0.75, cos(x)) for x from y
        var guessPitch = pitchRadians
        var resultPitch = atan2(sin(guessPitch) - 0.75, cos(guessPitch))
        var windowMax = PI / 2
        var windowMin = -PI / 2
        repeat(100) {
            if (resultPitch < pitchRadians) {
                windowMin = guessPitch
                guessPitch = (windowMin + windowMax) / 2
            } else {
                windowMax = guessPitch
                guessPitch = (windowMin + windowMax) / 2
            }
            resultPitch = atan2(sin(guessPitch) - 0.75, cos(guessPitch))
            if (resultPitch == pitchRadians) return guessPitch
        }
        return guessPitch
    }

    private var lastDianaSpade = SimpleTimeMark.farPast()
    private var spadeUsePosition: LorenzVec? = null

    @HandleEvent(onlyOnIsland = IslandType.HUB)
    fun onUseAbility(event: ItemClickEvent) {
        if (!isEnabled()) return
        if (event.clickType != ClickType.RIGHT_CLICK) return
        val item = event.itemInHand ?: return
        if (!item.isDianaSpade) return
        particleLocations.clear()
        lastDianaSpade = SimpleTimeMark.now()
        spadeUsePosition = Minecraft.getMinecraft().thePlayer.getPositionEyes(1.0F).toLorenzVec()
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(71, "event.diana.burrowsSoopyGuess", "event.diana.burrowsGuess")
    }

    private fun isEnabled() = DianaAPI.isDoingDiana() && config.burrowsGuess && config.burrowsGuessType == BurrowGuessType.PRECISE_GUESS
}
