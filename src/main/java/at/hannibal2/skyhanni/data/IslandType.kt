package at.hannibal2.skyhanni.data

enum class IslandType(val displayName: String, private val id: String?) {
    // TODO
    //  USE SH-REPO (for displayName only)
    PRIVATE_ISLAND("Private Island", "dynamic"),
    PRIVATE_ISLAND_GUEST("Private Island Guest", null),
    THE_END("The End", "combat_3"),
    KUUDRA_ARENA("Kuudra", "kuudra"),
    CRIMSON_ISLE("Crimson Isle", "crimson_isle"),
    DWARVEN_MINES("Dwarven Mines", "mining_3"),
    DUNGEON_HUB("Dungeon Hub", "dungeon_hub"),
    CATACOMBS("Catacombs", "dungeon"),

    HUB("Hub", "hub"),
    DARK_AUCTION("Dark Auction", "dark_auction"),
    THE_FARMING_ISLANDS("The Farming Islands", "farming_1"),
    CRYSTAL_HOLLOWS("Crystal Hollows", "crystal_hollows"),
    THE_PARK("The Park", "foraging_1"),
    DEEP_CAVERNS("Deep Caverns", "mining_2"),
    GOLD_MINES("Gold Mine", "mining_1"),
    GARDEN("Garden", "garden"),
    GARDEN_GUEST("Garden Guest", null),
    SPIDER_DEN("Spider's Den", "combat_1"),
    WINTER("Jerry's Workshop", "winter"),
    THE_RIFT("The Rift", "rift"),
    MINESHAFT("Mineshaft", "mineshaft"),

    NONE("", null),
    ANY("", null),
    UNKNOWN("???", null),
    ;

    fun guestVariant(): IslandType = when (this) {
        PRIVATE_ISLAND -> PRIVATE_ISLAND_GUEST
        GARDEN -> GARDEN_GUEST
        else -> this
    }

    // TODO: IslandTags
    fun hasGuestVariant(): Boolean = when (this) {
        PRIVATE_ISLAND, GARDEN -> true
        else -> false
    }

    companion object {

        fun getByNameOrUnknown(name: String): IslandType = getByNameOrNull(name) ?: UNKNOWN
        fun getByName(name: String): IslandType = getByNameOrNull(name) ?: error("IslandType not found: '$name'")

        fun getByNameOrNull(name: String): IslandType? = entries.find { it.displayName == name }

        fun getByIdOrNull(id: String): IslandType? = entries.find { it.id == id }
        fun getByIdOrUnknown(id: String): IslandType = getByIdOrNull(id) ?: UNKNOWN
    }
}
