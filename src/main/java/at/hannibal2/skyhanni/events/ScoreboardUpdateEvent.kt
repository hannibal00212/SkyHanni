package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent

class ScoreboardUpdateEvent(
    val full: List<String>,
    val old: List<String>,
) : SkyHanniEvent() {

    val added: List<String> = full - old.toSet()
    val removed: List<String> = old - full.toSet()
}
