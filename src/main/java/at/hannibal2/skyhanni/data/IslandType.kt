package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.data.IslandType.entries
import at.hannibal2.skyhanni.data.jsonobjects.repo.IslandTypeJson
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

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

        fun getByNameOrUnknown(name: String) = getByNameOrNull(name) ?: UNKNOWN
        fun getByName(name: String) = getByNameOrNull(name) ?: error("IslandType not found: '$name'")

        fun getByNameOrNull(name: String) = entries.firstOrNull { it.islandData?.name == name }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        fun onRepoReload(event: RepositoryReloadEvent) {
            val data = event.getConstant<IslandTypeJson>("IslandType")

            val islandDataMap = data.islands.mapValues {
                IslandData(it.value.name, it.value.apiName, it.value.maxPlayers ?: data.maxPlayers)
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
