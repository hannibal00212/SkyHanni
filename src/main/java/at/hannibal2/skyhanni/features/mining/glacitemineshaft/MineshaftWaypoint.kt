package at.hannibal2.skyhanni.features.mining.glacitemineshaft

import at.hannibal2.skyhanni.utils.SkyHanniVec3d

data class MineshaftWaypoint(
    val waypointType: MineshaftWaypointType,
    val location: SkyHanniVec3d,
    var shared: Boolean = false,
    var isCorpse: Boolean = false,
)
