package at.hannibal2.skyhanni.features.nether.reputationhelper

enum class FactionType(val factionName: String) {
    BARBARIAN("Barbarian"),
    MAGE("Mage"),
    NONE("None"),
    ;

    companion object {
        fun fromName(name: String) = entries.firstOrNull { it.factionName.equals(name, ignoreCase = true) } ?: NONE
    }
}
