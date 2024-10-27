package at.hannibal2.skyhanni.events

class ScoreboardUpdateEvent(
    val old: List<String>,
    val scoreboard: List<String>,
) : LorenzEvent() {

    val added by lazy { scoreboard - old.toSet() }
    val removed by lazy { old - scoreboard.toSet() }
}
