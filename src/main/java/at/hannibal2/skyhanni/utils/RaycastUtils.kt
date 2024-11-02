package at.hannibal2.skyhanni.utils

import net.minecraft.client.Minecraft

object RaycastUtils {

    data class Ray(
        val origin: LorenzVec,
        val direction: LorenzVec,
    ) {
        init {
            require(direction.isNormalized())
        }
    }

    data class Plane(
        val origin: LorenzVec,
        val normal: LorenzVec,
    ) {
        init {
            require(normal.isNormalized())
        }
    }

    fun createPlayerLookDirectionRay(): Ray {
        return Ray(
            LocationUtils.playerEyeLocation(),
            Minecraft.getMinecraft().thePlayer.lookVec.toLorenzVec()
        )
    }

    /**
     * Create a plane that contains [point] and is orthogonal to [ray].
     */
    fun createOrthogonalPlaneToRayAtPoint(
        ray: Ray,
        point: LorenzVec,
    ): Plane {
        return Plane(point, ray.direction)
    }

    /**
     * Intersect a plane (of any orientation) with a ray. The ray and plane may not be parallel to each other.
     */
    fun intersectPlaneWithRay(plane: Plane, ray: Ray): LorenzVec {
//         require(plane.normal.dotProduct(ray.direction).absoluteValue != 0.0)
        val intersectionPointDistanceAlongRay =
            (plane.normal.dotProduct(plane.origin) - plane.normal.dotProduct(ray.origin)) / plane.normal.dotProduct(ray.direction)
        return ray.origin + ray.direction.scale(intersectionPointDistanceAlongRay)
    }

    /**
     * Finds the distance between the given ray and the point. If the point is behind the ray origin (according to the ray's direction),
     * returns [Double.MAX_VALUE] instead.
     */
    fun findDistanceToRay(ray: Ray, point: LorenzVec): Double {
        val plane = createOrthogonalPlaneToRayAtPoint(ray, point)
        val intersectionPoint = intersectPlaneWithRay(plane, ray)
        if ((intersectionPoint - ray.origin).dotProduct(ray.direction) < 0) return Double.MAX_VALUE
        return intersectionPoint.distance(point)
    }

    inline fun <T> createDistanceToRayEstimator(ray: Ray, crossinline position: (T) -> LorenzVec): (T) -> Double {
        return {
            findDistanceToRay(ray, position(it))
        }
    }

    fun <T : Any> List<T>.findClosestPointToRay(ray: Ray, positionExtractor: (T) -> LorenzVec): T? {
        return minByOrNull(createDistanceToRayEstimator(ray, positionExtractor))
    }

}
