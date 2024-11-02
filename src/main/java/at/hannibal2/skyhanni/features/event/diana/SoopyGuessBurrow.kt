package at.hannibal2.skyhanni.features.event.diana

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.BurrowGuessEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.PlaySoundEvent
import at.hannibal2.skyhanni.events.ReceiveParticleEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.toLorenzVec
import net.minecraft.util.EnumParticleTypes
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sin

/**
 * Taken and ported from Soopyboo32's javascript module SoopyV2
 */
@SkyHanniModule
object SoopyGuessBurrow {

    private var dingIndex = 0
    private var hasDinged = false
    private var lastDingPitch = 0f
    private var firstPitch = 0f
    private var lastParticlePoint: LorenzVec? = null
    private var lastParticlePoint2: LorenzVec? = null
    private var firstParticlePoint: LorenzVec? = null
    private var particlePoint: LorenzVec? = null
    private var guessPoint: LorenzVec? = null

    private var lastSoundPoint: LorenzVec? = null
    private var locations = mutableListOf<LorenzVec>()

    private var dingSlope = mutableListOf<Float>()

    var distance: Double? = null
    private var distance2: Double? = null

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        hasDinged = false
        lastDingPitch = 0f
        firstPitch = 0f
        lastParticlePoint = null
        lastParticlePoint2 = null
        lastSoundPoint = null
        firstParticlePoint = null
        particlePoint = null
        guessPoint = null
        distance = null
        dingIndex = 0
        dingSlope.clear()
    }

    @SubscribeEvent
    fun onPlaySound(event: PlaySoundEvent) {
        if (!isEnabled()) return
        if (event.soundName != "note.harp") return

        val pitch = event.pitch
        if (!hasDinged) {
            firstPitch = pitch
        }

        hasDinged = true

        if (pitch < lastDingPitch) {
            firstPitch = pitch
            dingIndex = 0
            dingSlope.clear()
            lastDingPitch = pitch
            lastParticlePoint = null
            lastParticlePoint2 = null
            lastSoundPoint = null
            firstParticlePoint = null
            distance = null
            locations.clear()
        }

        if (lastDingPitch == 0f) {
            lastDingPitch = pitch
            distance = null
            lastParticlePoint = null
            lastParticlePoint2 = null
            lastSoundPoint = null
            firstParticlePoint = null
            locations.clear()
            return
        }

        dingIndex++

        if (dingIndex > 1) dingSlope.add(pitch - lastDingPitch)
        if (dingSlope.size > 20) dingSlope.removeFirst()
        val slope = if (dingSlope.isNotEmpty()) dingSlope.reduce { a, b -> a + b }.toDouble() / dingSlope.size else 0.0
        val pos = event.location
        lastSoundPoint = pos
        lastDingPitch = pitch

        if (lastParticlePoint2 == null || particlePoint == null || firstParticlePoint == null) {
            return
        }

        distance2 = (Math.E / slope) - firstParticlePoint?.distance(pos)!!

        if (distance2!! > 1000) {
            ChatUtils.debug("Soopy distance2 is $distance2")
            distance2 = null
            guessPoint = null

            // workaround: returning if the distance is too big
            return
        }

        val lineDist = lastParticlePoint2?.distance(particlePoint!!)!!

        distance = distance2!!
        val changesHelp = particlePoint?.subtract(lastParticlePoint2!!)!!
        var changes = listOf(changesHelp.x, changesHelp.y, changesHelp.z)
        changes = changes.map { o -> o / lineDist }

        lastSoundPoint?.let {
            guessPoint =
                LorenzVec(
                    it.x + changes[0] * distance!!,
                    it.y + changes[1] * distance!!,
                    it.z + changes[2] * distance!!
                )
        }
    }

    @Suppress("MaxLineLength")
    private fun solveEquationThing(x: LorenzVec, y: LorenzVec): LorenzVec {
        val a =
            (-y.x * x.y * x.x - y.y * x.y * x.z + y.y * x.y * x.x + x.y * x.z * y.z + x.x * x.z * y.x - x.x * x.z * y.z) / (x.y * y.x - x.y * y.z + x.x * y.z - y.x * x.z + y.y * x.z - y.y * x.x)
        val b = (y.x - y.y) * (x.x + a) * (x.y + a) / (x.y - x.x)
        val c = y.x - b / (x.x + a)
        return LorenzVec(a, b, c)
    }

    @SubscribeEvent
    fun onReceiveParticle(event: ReceiveParticleEvent) {
        if (!isEnabled()) return
        val type = event.type
        if (type != EnumParticleTypes.DRIP_LAVA) return
        val currLoc = event.location

        var run = false
        lastSoundPoint?.let {
            if (abs(currLoc.x - it.x) < 2 && abs(currLoc.y - it.y) < 0.5 && abs(currLoc.z - it.z) < 2) {
                run = true
            }
        }
        if (run) {
            if (locations.size < 100 && locations.isEmpty() || locations.last().distance(currLoc) != 0.0) {
                var distMultiplier = 1.0
                if (locations.size > 2) {
                    val predictedDist = 0.06507 * locations.size + 0.259
                    val lastPos = locations.last()
                    val actualDist = currLoc.distance(lastPos)
                    distMultiplier = actualDist / predictedDist
                }
                locations.add(currLoc)

                if (locations.size > 5 && guessPoint != null) {

                    val slopeThing = locations.zipWithNext { a, b ->
                        atan((a.x - b.x) / (a.z - b.z))
                    }

                    val (a, b, c) = solveEquationThing(
                        LorenzVec(slopeThing.size - 5, slopeThing.size - 3, slopeThing.size - 1),
                        LorenzVec(
                            slopeThing[slopeThing.size - 5],
                            slopeThing[slopeThing.size - 3],
                            slopeThing[slopeThing.size - 1]
                        )
                    )

                    val pr1 = mutableListOf<LorenzVec>()
                    val pr2 = mutableListOf<LorenzVec>()

                    val start = slopeThing.size - 1
                    val lastPos = locations[start].toDoubleArray()
                    val lastPos2 = locations[start].toDoubleArray()

                    var distCovered = 0.0

                    val ySpeed = locations[locations.size - 1].x - locations[locations.size - 2].x / hypot(
                        locations[locations.size - 1].x - locations[locations.size - 2].x,
                        locations[locations.size - 1].z - locations[locations.size - 2].x
                    )

                    var i = start + 1
                    while (distCovered < distance2!! && i < 10000) {
                        val y = b / (i + a) + c
                        val dist = distMultiplier * (0.06507 * i + 0.259)

                        val xOff = dist * sin(y)
                        val zOff = dist * cos(y)

                        val density = 5

                        for (o in 0..density) {
                            lastPos[0] += xOff / density
                            lastPos[2] += zOff / density

                            lastPos[1] += ySpeed * dist / density
                            lastPos2[1] += ySpeed * dist / density

                            lastPos2[0] -= xOff / density
                            lastPos2[2] -= zOff / density

                            pr1.add(lastPos.toLorenzVec())
                            pr2.add(lastPos2.toLorenzVec())


                            lastSoundPoint?.let {
                                distCovered = hypot(lastPos[0] - it.x, lastPos[2] - it.z)
                            }

                            if (distCovered > distance2!!) break
                        }
                        i++
                    }

                    // Why does this happen?
                    if (pr1.isEmpty()) return

                    val p1 = pr1.last()
                    val p2 = pr2.last()


                    guessPoint?.let {
                        val d1 = ((p1.x - it.x).times(2 + (p1.z - it.z))).pow(2)
                        val d2 = ((p2.x - it.x).times(2 + (p2.z - it.z))).pow(2)

                        val finalLocation = if (d1 < d2) {
                            LorenzVec(floor(p1.x), 255.0, floor(p1.z))
                        } else {
                            LorenzVec(floor(p2.x), 255.0, floor(p2.z))
                        }
                        BurrowGuessEvent(finalLocation).postAndCatch()
                    }
                }
            }

            if (lastParticlePoint == null) {
                firstParticlePoint = currLoc.clone()
            }

            lastParticlePoint2 = lastParticlePoint
            lastParticlePoint = particlePoint

            particlePoint = currLoc.clone()

            if (lastParticlePoint2 == null || firstParticlePoint == null || distance2 == null || lastSoundPoint == null) return

            val lineDist = lastParticlePoint2?.distance(particlePoint!!)!!

            distance = distance2!!

            val changesHelp = particlePoint?.subtract(lastParticlePoint2!!)!!

            var changes = listOf(changesHelp.x, changesHelp.y, changesHelp.z)
            changes = changes.map { o -> o / lineDist }

            lastParticlePoint?.let {
                guessPoint =
                    LorenzVec(
                        it.x + changes[0] * distance!!,
                        it.y + changes[1] * distance!!,
                        it.z + changes[2] * distance!!
                    )
            }
        }
    }

    private fun isEnabled() = DianaAPI.isDoingDiana() && SkyHanniMod.feature.event.diana.burrowsSoopyGuess
}
