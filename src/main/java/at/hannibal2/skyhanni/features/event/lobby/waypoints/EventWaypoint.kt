package at.hannibal2.skyhanni.features.event.lobby.waypoints

import at.hannibal2.skyhanni.data.jsonobjects.repo.EventWaypointData
import at.hannibal2.skyhanni.utils.LorenzVec

data class EventWaypoint(
    val name: String = "",
    val position: LorenzVec,
    var isFound: Boolean = false,
)

fun loadEventWaypoints(waypoints: Map<String, List<EventWaypointData>>): Map<String, MutableSet<EventWaypoint>> {
    return buildMap {
        for (lobby in waypoints) {
            val set = mutableSetOf<EventWaypoint>()
            lobby.value.forEach { waypoint -> set.add(EventWaypoint(waypoint.name, waypoint.position)) }
            this[lobby.key] = set
        }
    }
}
