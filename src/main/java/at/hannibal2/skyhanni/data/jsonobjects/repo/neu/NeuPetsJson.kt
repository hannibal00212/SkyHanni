package at.hannibal2.skyhanni.data.jsonobjects.repo.neu
import com.google.gson.JsonElement
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class NeuPetsJson(
    @Expose @SerializedName("pet_rarity_offset") val petRarityOffset: Map<String, Int>,
    @Expose @SerializedName("pet_levels") val petLevels: List<Int>,
    @Expose @SerializedName("custom_pet_leveling") val customPetLeveling: Map<String, JsonElement>,
    @Expose @SerializedName("pet_types") val petTypes: Map<String, String>,
)
