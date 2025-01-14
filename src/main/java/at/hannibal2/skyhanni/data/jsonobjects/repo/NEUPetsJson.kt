package at.hannibal2.skyhanni.data.jsonobjects.repo

import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.NEUInternalName
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class NEUPetsJson(
    @Expose @SerializedName("pet_levels") val petLevels: List<Int>,
    @Expose @SerializedName("custom_pet_leveling") val customPetLeveling: Map<String, NEUPetData>,
    @Expose @SerializedName("id_to_display_name") val internalToDisplayName: Map<NEUInternalName, String>
)

data class NEUPetData(
    @Expose @SerializedName("type") val type: Int? = null,
    @Expose @SerializedName("pet_levels") val petLevels: List<Int>? = null,
    @Expose @SerializedName("max_level") val maxLevel: Int? = null,
    @Expose @SerializedName("rarity_offset") val rarityOffset: Map<LorenzRarity, Int>? = null,
    @Expose @SerializedName("xp_multiplier") val xpMultiplier: Double? = null,
)
