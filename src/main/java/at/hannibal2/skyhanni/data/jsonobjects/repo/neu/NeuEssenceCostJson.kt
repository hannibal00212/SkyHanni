package at.hannibal2.skyhanni.data.jsonobjects.repo.neu

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

data class NeuEssenceCostJson(
    @Expose @SerializedName("type") val essenceType: String,
    @Expose @SerializedName("dungeonize") val essenceForDungeonize: Int?,
    @Expose @SerializedName("1") val essenceFor1: Int?,
    @Expose @SerializedName("2") val essenceFor2: Int?,
    @Expose @SerializedName("3") val essenceFor3: Int?,
    @Expose @SerializedName("4") val essenceFor4: Int?,
    @Expose @SerializedName("5") val essenceFor5: Int?,
    @Expose @SerializedName("6") val essenceFor6: Int?,
    @Expose @SerializedName("7") val essenceFor7: Int?,
    @Expose @SerializedName("8") val essenceFor8: Int?,
    @Expose @SerializedName("9") val essenceFor9: Int?,
    @Expose @SerializedName("10") val essenceFor10: Int?,
    @Expose @SerializedName("11") val essenceFor11: Int?,
    @Expose @SerializedName("12") val essenceFor12: Int?,
    @Expose @SerializedName("13") val essenceFor13: Int?,
    @Expose @SerializedName("14") val essenceFor14: Int?,
    @Expose @SerializedName("15") val essenceFor15: Int?,
    @Expose @SerializedName("items") val extraItems: Map<String, List<String>>?,

    @Expose @SerializedName("catacombs_requirements") val catacombsRequirements: List<NeuCatacombsRequirements>,
) {

    companion object {
        val TYPE: Type = object : TypeToken<Map<String?, NeuEssenceCostJson>>() {}.type
    }
}

data class NeuCatacombsRequirements(
    @Expose @SerializedName("type") val type: String,
    @Expose @SerializedName("dungeon_type") val dungeonType: String?,
    @Expose @SerializedName("level") val level: Int,
)
