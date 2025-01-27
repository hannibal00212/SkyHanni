package at.hannibal2.skyhanni.utils

import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.util.AxisAlignedBB
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration

object LocationUtils {

    fun canSee(a: LorenzVec, b: LorenzVec, offset: Double? = null): Boolean {
        return canSee0(a, b) && offset?.let { canSee0(a.add(y = it), b.add(y = it)) } ?: true
    }

    private fun canSee0(a: LorenzVec, b: LorenzVec) =
        Minecraft.getMinecraft().theWorld.rayTraceBlocks(a.toVec3(), b.toVec3(), false, true, false) == null

    fun playerLocation() = Minecraft.getMinecraft().thePlayer.getLorenzVec()

    fun LorenzVec.distanceToPlayer() = distance(playerLocation())

    fun LorenzVec.distanceToPlayerIgnoreY() = distanceIgnoreY(playerLocation())

    fun LorenzVec.distanceSqToPlayer() = distanceSq(playerLocation())

    fun LorenzVec.distanceToPlayerSqIgnoreY() = distanceSqIgnoreY(playerLocation())

    fun Entity.distanceToPlayer() = getLorenzVec().distanceToPlayer()

    fun Entity.distanceTo(location: LorenzVec) = getLorenzVec().distance(location)
    fun Entity.distanceTo(other: Entity) = getLorenzVec().distance(other.getLorenzVec())

    fun Entity.distanceToIgnoreY(location: LorenzVec) = getLorenzVec().distanceIgnoreY(location)

    fun playerEyeLocation(): LorenzVec {
        val player = Minecraft.getMinecraft().thePlayer
        val vec = player.getLorenzVec()
        return vec.up(player.getEyeHeight().toDouble())
    }

    fun AxisAlignedBB.isInside(vec: LorenzVec) = isVecInside(vec.toVec3())

    fun AxisAlignedBB.isPlayerInside() = isInside(playerLocation())

    fun LorenzVec.canBeSeen(viewDistance: Number = 150.0, offset: Double? = null): Boolean {
        val a = playerEyeLocation()
        val b = this
        val noBlocks = canSee(a, b, offset)
        val notTooFar = a.distance(b) < viewDistance.toDouble()
        val inFov = true // TODO add Frustum "Frustum().isBoundingBoxInFrustum(entity.entityBoundingBox)"
        return noBlocks && notTooFar && inFov
    }

    fun LorenzVec.canBeSeen(yOffsetRange: IntRange, radius: Double = 150.0): Boolean =
        yOffsetRange.any { offset ->
            up(offset).canBeSeen(radius)
        }

    fun AxisAlignedBB.minBox() = LorenzVec(minX, minY, minZ)

    fun AxisAlignedBB.maxBox() = LorenzVec(maxX, maxY, maxZ)

    fun AxisAlignedBB.rayIntersects(origin: LorenzVec, direction: LorenzVec): Boolean {
        // Reference for Algorithm https://tavianator.com/2011/ray_box.html
        val rayDirectionInverse = direction.inverse()
        val t1 = (this.minBox() - origin) * rayDirectionInverse
        val t2 = (this.maxBox() - origin) * rayDirectionInverse

        val tMin = max(t1.minOfEachElement(t2).max(), Double.NEGATIVE_INFINITY)
        val tMax = min(t1.maxOfEachElement(t2).min(), Double.POSITIVE_INFINITY)
        return tMax >= tMin && tMax >= 0.0
    }

    fun AxisAlignedBB.union(aabbs: List<AxisAlignedBB>?): AxisAlignedBB? {
        if (aabbs.isNullOrEmpty()) {
            return null
        }

        var minX = this.minX
        var minY = this.minY
        var minZ = this.minZ
        var maxX = this.maxX
        var maxY = this.maxY
        var maxZ = this.maxZ

        for (aabb in aabbs) {
            if (aabb.minX < minX) minX = aabb.minX
            if (aabb.minY < minY) minY = aabb.minY
            if (aabb.minZ < minZ) minZ = aabb.minZ
            if (aabb.maxX > maxX) maxX = aabb.maxX
            if (aabb.maxY > maxY) maxY = aabb.maxY
            if (aabb.maxZ > maxZ) maxZ = aabb.maxZ
        }

        return AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ)
    }

    fun AxisAlignedBB.getEdgeLengths() = maxBox() - minBox()

    fun AxisAlignedBB.getBoxCenter() = getEdgeLengths() * 0.5 + minBox()

    fun AxisAlignedBB.getTopCenter() = getBoxCenter().up((maxY - minY) / 2)

    fun AxisAlignedBB.clampTo(other: AxisAlignedBB): AxisAlignedBB {
        val minX = max(this.minX, other.minX)
        val minY = max(this.minY, other.minY)
        val minZ = max(this.minZ, other.minZ)
        val maxX = min(this.maxX, other.maxX)
        val maxY = min(this.maxY, other.maxY)
        val maxZ = min(this.maxZ, other.maxZ)
        return AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ)
    }

    fun calculatePlayerYaw(): Float {
        val player = Minecraft.getMinecraft().thePlayer
        var yaw = player.rotationYaw % 360
        if (yaw < 0) yaw += 360
        if (yaw > 180) yaw -= 360

        return yaw
    }

    fun calculatePlayerFacingDirection(): LorenzVec {
        val yaw = calculatePlayerYaw() + 180
        return when {
            yaw < 45 -> LorenzVec(0, 0, -1)
            yaw < 135 -> LorenzVec(1, 0, 0)
            yaw < 225 -> LorenzVec(0, 0, 1)
            yaw < 315 -> LorenzVec(-1, 0, 0)
            else -> LorenzVec(0, 0, -1)
        }
    }

    fun slopeOverTime(
        startTime: SimpleTimeMark,
        maxTime: Duration,
        from: LorenzVec,
        to: LorenzVec,
    ): LorenzVec {
        if (startTime == SimpleTimeMark.farPast()) return from
        val diff = startTime.toMillis() + maxTime.inWholeMilliseconds - System.currentTimeMillis()
        val location = if (diff > 0) {
            val percentage = diff.toDouble() / maxTime.inWholeMilliseconds
            from.slope(to, 1 - percentage)
        } else to
        return location
    }

    fun generateCubeEdges(delta: Double): Set<Pair<LorenzVec, LorenzVec>> {
        // Define the center of the cube
        val center = LorenzVec(0.5, 0.5, 0.5)

        // List of all original vertices (corners) of the cube
        val originalVertices = listOf(
            LorenzVec(0.0, 0.0, 0.0), LorenzVec(0.0, 0.0, 1.0),
            LorenzVec(0.0, 1.0, 0.0), LorenzVec(0.0, 1.0, 1.0),
            LorenzVec(1.0, 0.0, 0.0), LorenzVec(1.0, 0.0, 1.0),
            LorenzVec(1.0, 1.0, 0.0), LorenzVec(1.0, 1.0, 1.0),
        )

        // Function to shift a vertex outward from the center by delta
        fun shiftOutward(vertex: LorenzVec): LorenzVec {
            val direction = vertex - center
            val magnitude = direction.length()
            // Avoid division by zero for the center vertex (if applicable)
            return if (magnitude != 0.0) {
                val unitDirection = direction * (1.0 / magnitude)
                vertex + unitDirection * delta
            } else {
                vertex // If the vertex is at the center, no shift
            }
        }

        // Shift all vertices outward
        val shiftedVertices = originalVertices.map { shiftOutward(it) }

        // Predefined edges by their connecting shifted vertices
        val edges = setOf(
            Pair(shiftedVertices[0], shiftedVertices[1]), // Edge along z-axis
            Pair(shiftedVertices[0], shiftedVertices[2]), // Edge along y-axis
            Pair(shiftedVertices[0], shiftedVertices[4]), // Edge along x-axis

            Pair(shiftedVertices[1], shiftedVertices[3]), // Edge along y-axis
            Pair(shiftedVertices[1], shiftedVertices[5]), // Edge along x-axis

            Pair(shiftedVertices[2], shiftedVertices[3]), // Edge along z-axis
            Pair(shiftedVertices[2], shiftedVertices[6]), // Edge along x-axis

            Pair(shiftedVertices[3], shiftedVertices[7]), // Edge along x-axis
            Pair(shiftedVertices[4], shiftedVertices[5]), // Edge along z-axis

            Pair(shiftedVertices[4], shiftedVertices[6]), // Edge along y-axis
            Pair(shiftedVertices[5], shiftedVertices[7]), // Edge along y-axis
            Pair(shiftedVertices[6], shiftedVertices[7]),  // Edge along z-axis
        )

        return edges
    }
}
