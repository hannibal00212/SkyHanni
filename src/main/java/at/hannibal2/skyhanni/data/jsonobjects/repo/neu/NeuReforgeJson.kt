package at.hannibal2.skyhanni.data.jsonobjects.repo.neu

import at.hannibal2.skyhanni.data.model.SkyblockStatList
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NEUItems
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import net.minecraft.item.Item

data class NeuReforgeJson(
    @Expose val internalName: NEUInternalName?,
    @Expose val reforgeName: String,
    @Expose @SerializedName("itemTypes") val rawItemTypes: Any,
    @Expose val requiredRarities: List<LorenzRarity>,
    @Expose val reforgeCosts: Map<LorenzRarity, Long>?,
    @Expose val reforgeStats: Map<LorenzRarity, SkyblockStatList>?,
    @Expose @SerializedName("reforgeAbility") val rawReforgeAbility: Any?,
) {

    private lateinit var reforgeAbilityField: Map<LorenzRarity, String>
    private lateinit var itemTypeField: Pair<String, List<NEUInternalName>>

    val reforgeAbility
        get() = if (this::reforgeAbilityField.isInitialized) reforgeAbilityField
        else {
            reforgeAbilityField = when (this.rawReforgeAbility) {
                is String -> {
                    this.requiredRarities.associateWith { this.rawReforgeAbility }
                }

                is Map<*, *> -> (this.rawReforgeAbility as? Map<String, String>)?.mapKeys {
                    LorenzRarity.valueOf(
                        it.key.uppercase().replace(" ", "_"),
                    )
                }.orEmpty()

                else -> emptyMap()
            }
            reforgeAbilityField
        }

    val itemType: Pair<String, List<NEUInternalName>>
        get() = if (this::itemTypeField.isInitialized) itemTypeField
        else run {
            val any = this.rawItemTypes
            return when (any) {
                is String -> {
                    any.replace("/", "_AND_").uppercase() to emptyList()
                }

                is Map<*, *> -> {
                    val type = "SPECIAL_ITEMS"
                    val map = any as? Map<String, List<String>> ?: return type to emptyList()
                    val internalNames = map["internalName"]?.map { it.toInternalName() }.orEmpty()
                    val itemType = map["itemid"]?.map {
                        NEUItems.getInternalNamesForItemId(Item.getByNameOrId(it))
                    }?.flatten().orEmpty()
                    type to (internalNames + itemType)
                }

                else -> throw IllegalStateException()
            }
        }
}

