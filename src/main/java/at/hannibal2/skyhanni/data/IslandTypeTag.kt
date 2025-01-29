package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import com.google.gson.reflect.TypeToken
import java.util.EnumSet

class IslandTypeTag internal constructor(name: String, private val types: EnumSet<IslandType>) {

    internal constructor(name: String, vararg types: Any) : this(
        name,
        EnumSet.copyOf(
            types.flatMap {
                when (it) {
                    is IslandTypeTag -> it.types
                    is IslandType -> listOf(it)
                    is String -> listOf(IslandType.getByName(it))
                    else -> error("Invalid type: $it")
                }
            },
        ),
    )

    init {
        tags[name] = this
    }

    private fun update(newValues: List<String>) {
        types.clear()
        newValues.forEach { island ->
            LorenzUtils.enumValueOfOrNull<IslandType>(island.uppercase())?.let {
                types.add(it)
            }
        }
    }

    fun inAny() = LorenzUtils.inSkyBlock && LorenzUtils.skyBlockIsland in types

    @SkyHanniModule
    companion object {

        private val type = object : TypeToken<Map<String, List<String>>>() {}.type
        private val tags = mutableMapOf<String, IslandTypeTag>()

        @HandleEvent
        fun onRepoReload(event: RepositoryReloadEvent) {
            IslandTypeTags // Make sure the object is initialized
            event.getConstant<Map<String, List<String>>>("IslandTypeTags", type).forEach { (name, values) ->
                tags[name]?.update(values)
            }
        }
    }
}

object IslandTypeTags {

    val PRIVATE_ISLAND = IslandTypeTag(
        "private_island",
        IslandType.PRIVATE_ISLAND,
        IslandType.PRIVATE_ISLAND_GUEST,
    )

    val GARDEN_ISLAND = IslandTypeTag(
        "garden_island",
        IslandType.GARDEN,
        IslandType.GARDEN_GUEST,
    )

    // Mining
    val IS_COLD = IslandTypeTag(
        "is_cold",
        IslandType.DWARVEN_MINES,
        IslandType.MINESHAFT,
    )
    val NORMAL_MINING = IslandTypeTag(
        "normal_mining",
        IslandType.GOLD_MINES,
        IslandType.DEEP_CAVERNS,
    )
    val ADVANCED_MINING = IslandTypeTag(
        "advanced_mining",
        IS_COLD,
        IslandType.CRYSTAL_HOLLOWS,
    )
    val MINING = IslandTypeTag(
        "mining",
        NORMAL_MINING,
        ADVANCED_MINING,
    )
    val CUSTOM_MINING = IslandTypeTag(
        "custom_mining",
        ADVANCED_MINING,
        IslandType.THE_END,
        IslandType.CRIMSON_ISLE,
        IslandType.SPIDER_DEN,
    )

    // Misc
    val HOPPITY_DISALLOWED = IslandTypeTag(
        "hoppity_disallowed",
        IslandType.THE_RIFT,
        IslandType.KUUDRA_ARENA,
        IslandType.CATACOMBS,
        IslandType.MINESHAFT,
    )
    val HAS_SHOWCASES = IslandTypeTag(
        "has_showcases",
        IslandType.HUB,
        IslandType.PRIVATE_ISLAND,
        IslandType.PRIVATE_ISLAND_GUEST,
        IslandType.CRIMSON_ISLE,
    )
    val CONTESTS_SHOWN = IslandTypeTag(
        "contests_shown",
        IslandType.GARDEN,
        IslandType.HUB,
        IslandType.THE_FARMING_ISLANDS,
    )

    /** Busy islands are islands where a player is doing something considered 'important'. */
    val BUSY = IslandTypeTag(
        "busy",
        IslandType.DARK_AUCTION,
        IslandType.MINESHAFT,
        IslandType.THE_RIFT,
        IslandType.NONE,
        IslandType.UNKNOWN,
    )

}
