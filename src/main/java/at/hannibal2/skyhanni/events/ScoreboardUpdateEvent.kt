package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent

class ScoreboardUpdateEvent(
    val old: List<String>,
    val scoreboard: List<String>,
) : SkyHanniEvent() {

    val added by lazy { scoreboard - old.toSet() }
    val removed by lazy { old - scoreboard.toSet() }
}
