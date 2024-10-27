package at.hannibal2.skyhanni.events

class ScoreboardUpdateEvent(
    val full: List<String>,
    val old: List<String>,
) : LorenzEvent() {

    val added: List<String> = full - old.toSet()
    val removed: List<String> = old - full.toSet()
}
