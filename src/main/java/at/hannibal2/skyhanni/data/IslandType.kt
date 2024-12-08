package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.data.IslandType.entries
import at.hannibal2.skyhanni.data.jsonobjects.repo.IslandTypeJson
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

enum class IslandType {
    PRIVATE_ISLAND,
    PRIVATE_ISLAND_GUEST,
    THE_END,
    KUUDRA_ARENA,
    CRIMSON_ISLE,
    DWARVEN_MINES,
    DUNGEON_HUB,
    CATACOMBS,

    HUB,
    DARK_AUCTION,
    THE_FARMING_ISLANDS,
    CRYSTAL_HOLLOWS,
    THE_PARK,
    DEEP_CAVERNS,
    GOLD_MINES,
    GARDEN,
    GARDEN_GUEST,
    SPIDER_DEN,
    WINTER,
    THE_RIFT,
    MINESHAFT,

    NONE,
    ANY,
    UNKNOWN,
    ;

    var islandData: IslandData? = null

    val displayName: String get() = islandData?.name ?: name

    @SkyHanniModule
    companion object {
        var islandTypesData: IslandTypeJson? = null
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

            islandTypesData = data
        }
    }
}

data class IslandData(
    val name: String,
    val apiName: String?,
    val maxPlayers: Int,
)
