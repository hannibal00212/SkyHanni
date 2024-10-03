package at.hannibal2.skyhanni.features.nether.reputationhelper.dailyquest.quest

enum class QuestState(val displayName: String, val color: String) {
    NOT_ACCEPTED("Not Accepted", "§c"),
    ACCEPTED("Accepted", "§b"),
    READY_TO_COLLECT("Ready to collect", "§a"),
    COLLECTED("Collected", "§7"),
}
