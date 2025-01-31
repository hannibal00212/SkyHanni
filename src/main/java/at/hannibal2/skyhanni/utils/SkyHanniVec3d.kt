package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.Rotations
import net.minecraft.util.Vec3
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt

data class SkyHanniVec3d(
    val x: Double,
    val y: Double,
    val z: Double,
) {
    constructor() : this(0.0, 0.0, 0.0)

    constructor(x: Int, y: Int, z: Int) : this(x.toDouble(), y.toDouble(), z.toDouble())

    constructor(x: Float, y: Float, z: Float) : this(x.toDouble(), y.toDouble(), z.toDouble())

    fun toBlockPos(): BlockPos = BlockPos(x, y, z)

    fun toVec3(): Vec3 = Vec3(x, y, z)

    fun distanceIgnoreY(other: SkyHanniVec3d): Double = distanceSqIgnoreY(other).pow(0.5)

    fun distance(other: SkyHanniVec3d): Double = distanceSq(other).pow(0.5)

    fun distanceSq(x: Double, y: Double, z: Double): Double = distanceSq(SkyHanniVec3d(x, y, z))

    fun distance(x: Double, y: Double, z: Double): Double = distance(SkyHanniVec3d(x, y, z))

    fun distanceChebyshevIgnoreY(other: SkyHanniVec3d) = max(abs(x - other.x), abs(z - other.z))

    fun distanceSq(other: SkyHanniVec3d): Double {
        val dx = other.x - x
        val dy = other.y - y
        val dz = other.z - z
        return (dx * dx + dy * dy + dz * dz)
    }

    fun distanceSqIgnoreY(other: SkyHanniVec3d): Double {
        val dx = other.x - x
        val dz = other.z - z
        return (dx * dx + dz * dz)
    }

    operator fun plus(other: SkyHanniVec3d) = SkyHanniVec3d(x + other.x, y + other.y, z + other.z)

    operator fun minus(other: SkyHanniVec3d) = SkyHanniVec3d(x - other.x, y - other.y, z - other.z)

    operator fun times(other: SkyHanniVec3d) = SkyHanniVec3d(x * other.x, y * other.y, z * other.z)
    operator fun times(other: Double) = SkyHanniVec3d(x * other, y * other, z * other)
    operator fun times(other: Int) = SkyHanniVec3d(x * other, y * other, z * other)

    operator fun div(other: SkyHanniVec3d) = SkyHanniVec3d(x / other.x, y / other.y, z / other.z)
    operator fun div(other: Double) = SkyHanniVec3d(x / other, y / other, z / other)

    fun add(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0): SkyHanniVec3d =
        SkyHanniVec3d(this.x + x, this.y + y, this.z + z)

    fun add(x: Int = 0, y: Int = 0, z: Int = 0): SkyHanniVec3d = SkyHanniVec3d(this.x + x, this.y + y, this.z + z)

    fun dotProduct(other: SkyHanniVec3d): Double = (x * other.x) + (y * other.y) + (z * other.z)

    fun angleAsCos(other: SkyHanniVec3d) = normalize().dotProduct(other.normalize())

    fun angleInRad(other: SkyHanniVec3d) = acos(angleAsCos(other))

    fun angleInDeg(other: SkyHanniVec3d) = Math.toDegrees(angleInRad(other))

    fun crossProduct(other: SkyHanniVec3d): SkyHanniVec3d = SkyHanniVec3d(
        this.y * other.z - this.z * other.y,
        this.z * other.x - this.x * other.z,
        this.x * other.y - this.y * other.x,
    )

    fun scaledTo(other: SkyHanniVec3d) = this.normalize().times(other.length())

    fun normalize() = length().let { SkyHanniVec3d(x / it, y / it, z / it) }

    fun inverse() = SkyHanniVec3d(1.0 / x, 1.0 / y, 1.0 / z)

    fun min() = min(x, min(y, z))
    fun max() = max(x, max(y, z))

    fun minOfEachElement(other: SkyHanniVec3d) = SkyHanniVec3d(min(x, other.x), min(y, other.y), min(z, other.z))
    fun maxOfEachElement(other: SkyHanniVec3d) = SkyHanniVec3d(max(x, other.x), max(y, other.y), max(z, other.z))

    fun printWithAccuracy(accuracy: Int, splitChar: String = " "): String {
        return if (accuracy == 0) {
            val x = round(x).toInt()
            val y = round(y).toInt()
            val z = round(z).toInt()
            "$x$splitChar$y$splitChar$z"
        } else {
            val x = (round(x * accuracy) / accuracy)
            val y = (round(y * accuracy) / accuracy)
            val z = (round(z * accuracy) / accuracy)
            "$x$splitChar$y$splitChar$z"
        }
    }

    fun toCleanString(separator: String = ", "): String = listOf(x, y, z).joinToString(separator)

    fun asStoredString(): String = "$x:$y:$z"

    fun lengthSquared(): Double = x * x + y * y + z * z
    fun length(): Double = sqrt(lengthSquared())

    fun isNormalized(tolerance: Double = 0.01) = (lengthSquared() - 1.0).absoluteValue < tolerance

    fun isZero(): Boolean = x == 0.0 && y == 0.0 && z == 0.0

    fun clone(): SkyHanniVec3d = SkyHanniVec3d(x, y, z)

    fun toIntList(): List<Int> = listOf(x.toInt(), y.toInt(), z.toInt())
    fun toDoubleList(): List<Double> = listOf(x, y, z)
    fun toFloatList(): List<Float> = listOf(x.toFloat(), y.toFloat(), z.toFloat())

    @Deprecated("Use toDoubleList() instead", ReplaceWith("toDoubleList()"))
    fun toDoubleArray(): Array<Double> = arrayOf(x, y, z)
    @Deprecated("Use toFloatList() instead", ReplaceWith("toFloatList()"))
    fun toFloatArray(): Array<Float> = arrayOf(x.toFloat(), y.toFloat(), z.toFloat())

    fun equalsIgnoreY(other: SkyHanniVec3d) = x == other.x && z == other.z

    fun roundTo(precision: Int) = SkyHanniVec3d(x.roundTo(precision), y.roundTo(precision), z.roundTo(precision))

    fun roundLocationToBlock(): SkyHanniVec3d {
        val x = (x - .499999).roundTo(0)
        val y = (y - .499999).roundTo(0)
        val z = (z - .499999).roundTo(0)
        return SkyHanniVec3d(x, y, z)
    }

    fun blockCenter() = roundLocationToBlock().add(0.5, 0.5, 0.5)

    fun slope(other: SkyHanniVec3d, factor: Double) = this + (other - this).scale(factor)

    // TODO better name. dont confuse with roundTo()
    fun roundLocation(): SkyHanniVec3d {
        val x = if (x < 0) x.toInt() - 1 else x.toInt()
        val y = y.toInt() - 1
        val z = if (z < 0) z.toInt() - 1 else z.toInt()
        return SkyHanniVec3d(x, y, z)
    }

    fun boundingToOffset(offX: Double, offY: Double, offZ: Double) =
        AxisAlignedBB(x, y, z, x + offX, y + offY, z + offZ)

    fun scale(scalar: Double): SkyHanniVec3d = SkyHanniVec3d(scalar * x, scalar * y, scalar * z)

    fun applyTranslationToGL() {
        GlStateManager.translate(x, y, z)
    }

    fun axisAlignedTo(other: SkyHanniVec3d) = AxisAlignedBB(x, y, z, other.x, other.y, other.z)

    fun up(offset: Number = 1): SkyHanniVec3d = copy(y = y + offset.toDouble())

    fun down(offset: Number = 1): SkyHanniVec3d = copy(y = y - offset.toDouble())

    fun interpolate(other: SkyHanniVec3d, factor: Double): SkyHanniVec3d {
        require(factor in 0.0..1.0) { "Percentage must be between 0 and 1: $factor" }

        val x = (1 - factor) * x + factor * other.x
        val y = (1 - factor) * y + factor * other.y
        val z = (1 - factor) * z + factor * other.z

        return SkyHanniVec3d(x, y, z)
    }

    fun negated() = SkyHanniVec3d(-x, -y, -z)

    fun rotateXY(theta: Double) = SkyHanniVec3d(x * cos(theta) - y * sin(theta), x * sin(theta) + y * cos(theta), z)
    fun rotateXZ(theta: Double) = SkyHanniVec3d(x * cos(theta) + z * sin(theta), y, -x * sin(theta) + z * cos(theta))
    fun rotateYZ(theta: Double) = SkyHanniVec3d(x, y * cos(theta) - z * sin(theta), y * sin(theta) + z * cos(theta))

    fun nearestPointOnLine(startPos: SkyHanniVec3d, endPos: SkyHanniVec3d): SkyHanniVec3d {
        var d = endPos - startPos
        val w = this - startPos

        val dp = d.lengthSquared()
        var dt = 0.0

        if (dp != dt) dt = (w.dotProduct(d) / dp).coerceIn(0.0, 1.0)

        d *= dt
        d += startPos
        return d
    }

    fun distanceToLine(startPos: SkyHanniVec3d, endPos: SkyHanniVec3d): Double {
        return (nearestPointOnLine(startPos, endPos) - this).lengthSquared()
    }

    fun middle(other: SkyHanniVec3d): SkyHanniVec3d = this.plus(other.minus(this) / 2)

    private operator fun div(i: Number): SkyHanniVec3d = SkyHanniVec3d(x / i.toDouble(), y / i.toDouble(), z / i.toDouble())

    companion object {

        fun getFromYawPitch(yaw: Double, pitch: Double): SkyHanniVec3d {
            val yaw: Double = (yaw + 90) * Math.PI / 180
            val pitch: Double = (pitch + 90) * Math.PI / 180

            val x = sin(pitch) * cos(yaw)
            val y = sin(pitch) * sin(yaw)
            val z = cos(pitch)
            return SkyHanniVec3d(x, z, y)
        }

        // Format: "x:y:z"
        fun decodeFromString(string: String): SkyHanniVec3d {
            val (x, y, z) = string.split(":").map { it.toDouble() }
            return SkyHanniVec3d(x, y, z)
        }

        fun getBlockBelowPlayer() = LocationUtils.playerLocation().roundLocationToBlock().down()

        val expandVector = SkyHanniVec3d(0.0020000000949949026, 0.0020000000949949026, 0.0020000000949949026)
    }
}

fun BlockPos.toLorenzVec(): SkyHanniVec3d = SkyHanniVec3d(x, y, z)

fun Entity.getLorenzVec(): SkyHanniVec3d = SkyHanniVec3d(posX, posY, posZ)
fun Entity.getPrevLorenzVec(): SkyHanniVec3d = SkyHanniVec3d(prevPosX, prevPosY, prevPosZ)
fun Entity.getMotionLorenzVec(): SkyHanniVec3d = SkyHanniVec3d(motionX, motionY, motionZ)

fun Vec3.toLorenzVec(): SkyHanniVec3d = SkyHanniVec3d(xCoord, yCoord, zCoord)

fun Rotations.toLorenzVec(): SkyHanniVec3d = SkyHanniVec3d(x, y, z)

fun S2APacketParticles.toLorenzVec() = SkyHanniVec3d(xCoordinate, yCoordinate, zCoordinate)

fun Array<Double>.toLorenzVec(): SkyHanniVec3d {
    return SkyHanniVec3d(this[0], this[1], this[2])
}

fun RenderUtils.translate(vec: SkyHanniVec3d) = GlStateManager.translate(vec.x, vec.y, vec.z)

fun AxisAlignedBB.expand(vec: SkyHanniVec3d): AxisAlignedBB = expand(vec.x, vec.y, vec.z)

fun AxisAlignedBB.expand(amount: Double): AxisAlignedBB = expand(amount, amount, amount)
