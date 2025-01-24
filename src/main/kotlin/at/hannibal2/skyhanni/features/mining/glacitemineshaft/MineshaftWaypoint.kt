package at.hannibal2.skyhanni.features.mining.glacitemineshaft

import at.hannibal2.skyhanni.utils.LorenzVec

data class MineshaftWaypoint(
    val waypointType: MineshaftWaypointType,
    val location: LorenzVec,
    var shared: Boolean = false,
    var isCorpse: Boolean = false
)
