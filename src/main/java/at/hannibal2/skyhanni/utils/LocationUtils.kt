package at.hannibal2.skyhanni.utils

import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.util.AxisAlignedBB
import kotlin.math.max
import kotlin.math.min

object LocationUtils {

    fun canSee(a: LorenzVec, b: LorenzVec) =
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
        return vec.add(y = player.getEyeHeight().toDouble())
    }

    fun AxisAlignedBB.isInside(vec: LorenzVec) = isVecInside(vec.toVec3())

    fun AxisAlignedBB.isPlayerInside() = isInside(playerLocation())

    fun LorenzVec.canBeSeen(radius: Double = 150.0): Boolean {
        val a = playerEyeLocation()
        val b = this
        val noBlocks = canSee(a, b)
        val notTooFar = a.distance(b) < radius
        val inFov = true // TODO add Frustum "Frustum().isBoundingBoxInFrustum(entity.entityBoundingBox)"
        return noBlocks && notTooFar && inFov
    }

    fun LorenzVec.canBeSeen(yOffsetRange: IntRange, radius: Double = 150.0): Boolean =
        yOffsetRange.any { offset ->
            this.add(y = offset).canBeSeen(radius)
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

    fun AxisAlignedBB.getCenter() = getEdgeLengths() * 0.5 + minBox()

    fun AxisAlignedBB.getTopCenter() = getCenter().add(y = (maxY - minY) / 2)

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
        var yaw = LocationUtils.calculatePlayerYaw() + 180
        return when {
            yaw < 45 -> LorenzVec(0, 0, -1)
            yaw < 135 -> LorenzVec(1, 0, 0)
            yaw < 225 -> LorenzVec(0, 0, 1)
            yaw < 315 -> LorenzVec(-1, 0, 0)
            else -> LorenzVec(0, 0, -1)
        }
    }
}
