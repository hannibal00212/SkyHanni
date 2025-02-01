package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.event.HandleEvent.Companion.HIGHEST
import at.hannibal2.skyhanni.data.IslandType.entries
import at.hannibal2.skyhanni.data.jsonobjects.repo.IslandTypeJson
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule

enum class IslandType(private val nameFallback: String) {
    PRIVATE_ISLAND("Private Island"),
    PRIVATE_ISLAND_GUEST("Private Island Guest"),
    THE_END("The End"),
    KUUDRA_ARENA("Kuudra"),
    CRIMSON_ISLE("Crimson Isle"),
    DWARVEN_MINES("Dwarven Mines"),
    DUNGEON_HUB("Dungeon Hub"),
    CATACOMBS("Catacombs"),

    HUB("Hub"),
    DARK_AUCTION("Dark Auction"),
    THE_FARMING_ISLANDS("The Farming Islands"),
    CRYSTAL_HOLLOWS("Crystal Hollows"),
    THE_PARK("The Park"),
    DEEP_CAVERNS("Deep Caverns"),
    GOLD_MINES("Gold Mine"),
    GARDEN("Garden"),
    GARDEN_GUEST("Garden Guest"),
    SPIDER_DEN("Spider's Den"),
    WINTER("Jerry's Workshop"),
    THE_RIFT("The Rift"),
    MINESHAFT("Mineshaft"),

    NONE(""),
    ANY(""),
    UNKNOWN("???"),
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

    var islandData: IslandData? = null
        private set

    val displayName: String get() = islandData?.name ?: nameFallback

    @SkyHanniModule
    companion object {
        /**
         * The maximum amount of players that can be on an island.
         */
        var maxPlayers = 24
            private set

        /**
         * The maximum amount of players that can be on a mega hub.
         */
        var maxPlayersMega = 80
            private set

        fun getByName(name: String): IslandType = getByNameOrNull(name) ?: error("IslandType not found: '$name'")
        fun getByNameOrUnknown(name: String): IslandType = getByNameOrNull(name) ?: UNKNOWN
        fun getByNameOrNull(name: String): IslandType? = entries.find { it.displayName == name }

        fun getByIdOrNull(id: String): IslandType? = entries.find { it.islandData?.apiName == id }
        fun getByIdOrUnknown(id: String): IslandType = getByIdOrNull(id) ?: UNKNOWN

        @HandleEvent(priority = HIGHEST)
        fun onRepoReload(event: RepositoryReloadEvent) {
            val data = event.getConstant<IslandTypeJson>("misc/IslandType")

            val islandDataMap = data.islands.mapValues {
                val island = it.value
                IslandData(island.name, island.apiName, island.maxPlayers ?: data.maxPlayers)
            }

            entries.forEach { islandType ->
                islandType.islandData = islandDataMap[islandType.name]
            }

            maxPlayers = data.maxPlayers
            maxPlayersMega = data.maxPlayersMega
        }
    }
}

data class IslandData(
    val name: String,
    val apiName: String?,
    val maxPlayers: Int,
)
