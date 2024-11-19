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

data class LorenzVec(
    val x: Double,
    val y: Double,
    val z: Double,
) {
    constructor() : this(0.0, 0.0, 0.0)

    constructor(x: Int, y: Int, z: Int) : this(x.toDouble(), y.toDouble(), z.toDouble())

    constructor(x: Float, y: Float, z: Float) : this(x.toDouble(), y.toDouble(), z.toDouble())

    fun toBlockPos(): BlockPos = BlockPos(x, y, z)

    fun toVec3(): Vec3 = Vec3(x, y, z)

    fun distanceIgnoreY(other: LorenzVec): Double = distanceSqIgnoreY(other).pow(0.5)

    fun distance(other: LorenzVec): Double = distanceSq(other).pow(0.5)

    fun distanceSq(x: Double, y: Double, z: Double): Double = distanceSq(LorenzVec(x, y, z))

    fun distance(x: Double, y: Double, z: Double): Double = distance(LorenzVec(x, y, z))

    fun distanceChebyshevIgnoreY(other: LorenzVec) = max(abs(x - other.x), abs(z - other.z))

    fun distanceSq(other: LorenzVec): Double {
        val dx = other.x - x
        val dy = other.y - y
        val dz = other.z - z
        return (dx * dx + dy * dy + dz * dz)
    }

    fun distanceSqIgnoreY(other: LorenzVec): Double {
        val dx = other.x - x
        val dz = other.z - z
        return (dx * dx + dz * dz)
    }

    operator fun plus(other: LorenzVec) = LorenzVec(x + other.x, y + other.y, z + other.z)

    operator fun minus(other: LorenzVec) = LorenzVec(x - other.x, y - other.y, z - other.z)

    operator fun times(other: LorenzVec) = LorenzVec(x * other.x, y * other.y, z * other.z)
    operator fun times(other: Double) = LorenzVec(x * other, y * other, z * other)
    operator fun times(other: Int) = LorenzVec(x * other, y * other, z * other)

    operator fun div(other: LorenzVec) = LorenzVec(x / other.x, y / other.y, z / other.z)
    operator fun div(other: Double) = LorenzVec(x / other, y / other, z / other)

    fun add(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0): LorenzVec =
        LorenzVec(this.x + x, this.y + y, this.z + z)

    fun add(x: Int = 0, y: Int = 0, z: Int = 0): LorenzVec = LorenzVec(this.x + x, this.y + y, this.z + z)

    override fun toString() = "LorenzVec{x=$x, y=$y, z=$z}"

    fun dotProduct(other: LorenzVec): Double = (x * other.x) + (y * other.y) + (z * other.z)

    fun angleAsCos(other: LorenzVec) = normalize().dotProduct(other.normalize())

    fun angleInRad(other: LorenzVec) = acos(angleAsCos(other))

    fun angleInDeg(other: LorenzVec) = Math.toDegrees(angleInRad(other))

    fun normalize() = length().let { LorenzVec(x / it, y / it, z / it) }

    fun inverse() = LorenzVec(1.0 / x, 1.0 / y, 1.0 / z)

    fun min() = min(x, min(y, z))
    fun max() = max(x, max(y, z))

    fun minOfEachElement(other: LorenzVec) = LorenzVec(min(x, other.x), min(y, other.y), min(z, other.z))
    fun maxOfEachElement(other: LorenzVec) = LorenzVec(max(x, other.x), max(y, other.y), max(z, other.z))

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

    fun lengthSquared(): Double = x * x + y * y + z * z
    fun length(): Double = sqrt(lengthSquared())

    fun isNormalized(tolerance: Double = 0.01) = (lengthSquared() - 1.0).absoluteValue < tolerance

    fun isZero(): Boolean = x == 0.0 && y == 0.0 && z == 0.0

    fun clone(): LorenzVec = LorenzVec(x, y, z)

    fun toDoubleArray(): Array<Double> = arrayOf(x, y, z)
    fun toFloatArray(): Array<Float> = arrayOf(x.toFloat(), y.toFloat(), z.toFloat())

    fun equalsIgnoreY(other: LorenzVec) = x == other.x && z == other.z

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        return (other as? LorenzVec)?.let {
            x == it.x && y == it.y && z == it.z
        } ?: super.equals(other)
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + z.hashCode()
        return result
    }

    fun roundTo(precision: Int) = LorenzVec(x.roundTo(precision), y.roundTo(precision), z.roundTo(precision))

    fun roundLocationToBlock(): LorenzVec {
        val x = (x - .499999).roundTo(0)
        val y = (y - .499999).roundTo(0)
        val z = (z - .499999).roundTo(0)
        return LorenzVec(x, y, z)
    }

    fun blockCenter() = roundLocationToBlock().add(0.5, 0.5, 0.5)

    fun slope(other: LorenzVec, factor: Double) = this + (other - this).scale(factor)

    // TODO better name. dont confuse with roundTo()
    fun roundLocation(): LorenzVec {
        val x = if (x < 0) x.toInt() - 1 else x.toInt()
        val y = y.toInt() - 1
        val z = if (z < 0) z.toInt() - 1 else z.toInt()
        return LorenzVec(x, y, z)
    }

    fun boundingToOffset(offX: Double, offY: Double, offZ: Double) =
        AxisAlignedBB(x, y, z, x + offX, y + offY, z + offZ)

    fun scale(scalar: Double): LorenzVec = LorenzVec(scalar * x, scalar * y, scalar * z)

    fun applyTranslationToGL() {
        GlStateManager.translate(x, y, z)
    }

    fun axisAlignedTo(other: LorenzVec) = AxisAlignedBB(x, y, z, other.x, other.y, other.z)

    fun up(offset: Number = 1): LorenzVec = copy(y = y + offset.toDouble())

    fun down(offset: Number = 1): LorenzVec = copy(y = y - offset.toDouble())

    fun interpolate(other: LorenzVec, factor: Double): LorenzVec {
        require(factor in 0.0..1.0) { "Percentage must be between 0 and 1: $factor" }

        val x = (1 - factor) * x + factor * other.x
        val y = (1 - factor) * y + factor * other.y
        val z = (1 - factor) * z + factor * other.z

        return LorenzVec(x, y, z)
    }

    fun negated() = LorenzVec(-x, -y, -z)

    fun rotateXY(theta: Double) = LorenzVec(x * cos(theta) - y * sin(theta), x * sin(theta) + y * cos(theta), z)
    fun rotateXZ(theta: Double) = LorenzVec(x * cos(theta) + z * sin(theta), y, -x * sin(theta) + z * cos(theta))
    fun rotateYZ(theta: Double) = LorenzVec(x, y * cos(theta) - z * sin(theta), y * sin(theta) + z * cos(theta))

    fun nearestPointOnLine(startPos: LorenzVec, endPos: LorenzVec): LorenzVec {
        var d = endPos - startPos
        val w = this - startPos

        val dp = d.lengthSquared()
        var dt = 0.0

        if (dp != dt) dt = (w.dotProduct(d) / dp).coerceIn(0.0, 1.0)

        d *= dt
        d += startPos
        return d
    }

    fun distanceToLine(startPos: LorenzVec, endPos: LorenzVec): Double {
        return (nearestPointOnLine(startPos, endPos) - this).lengthSquared()
    }

    fun middle(other: LorenzVec): LorenzVec = this.plus(other.minus(this) / 2)

    private operator fun div(i: Number): LorenzVec = LorenzVec(x / i.toDouble(), y / i.toDouble(), z / i.toDouble())

    companion object {

        fun getFromYawPitch(yaw: Double, pitch: Double): LorenzVec {
            val yaw: Double = (yaw + 90) * Math.PI / 180
            val pitch: Double = (pitch + 90) * Math.PI / 180

            val x = sin(pitch) * cos(yaw)
            val y = sin(pitch) * sin(yaw)
            val z = cos(pitch)
            return LorenzVec(x, z, y)
        }

        // Format: "x:y:z"
        fun decodeFromString(string: String): LorenzVec {
            val (x, y, z) = string.split(":").map { it.toDouble() }
            return LorenzVec(x, y, z)
        }

        fun getBlockBelowPlayer() = LocationUtils.playerLocation().roundLocationToBlock().down()

        val expandVector = LorenzVec(0.0020000000949949026, 0.0020000000949949026, 0.0020000000949949026)
    }
}

fun BlockPos.toLorenzVec(): LorenzVec = LorenzVec(x, y, z)

fun Entity.getLorenzVec(): LorenzVec = LorenzVec(posX, posY, posZ)
fun Entity.getPrevLorenzVec(): LorenzVec = LorenzVec(prevPosX, prevPosY, prevPosZ)
fun Entity.getMotionLorenzVec(): LorenzVec = LorenzVec(motionX, motionY, motionZ)

fun Vec3.toLorenzVec(): LorenzVec = LorenzVec(xCoord, yCoord, zCoord)

fun Rotations.toLorenzVec(): LorenzVec = LorenzVec(x, y, z)

fun S2APacketParticles.toLorenzVec() = LorenzVec(xCoordinate, yCoordinate, zCoordinate)

fun Array<Double>.toLorenzVec(): LorenzVec {
    return LorenzVec(this[0], this[1], this[2])
}

fun RenderUtils.translate(vec: LorenzVec) = GlStateManager.translate(vec.x, vec.y, vec.z)

fun AxisAlignedBB.expand(vec: LorenzVec): AxisAlignedBB = expand(vec.x, vec.y, vec.z)

fun AxisAlignedBB.expand(amount: Double): AxisAlignedBB = expand(amount, amount, amount)
