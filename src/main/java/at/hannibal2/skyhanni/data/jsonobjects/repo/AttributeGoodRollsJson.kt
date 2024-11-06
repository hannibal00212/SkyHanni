package at.hannibal2.skyhanni.data.jsonobjects.repo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class AttributeGoodRollsJson(
    @Expose @SerializedName("good_rolls") val goodRolls: Map<String, ItemRoll>,
)

data class ItemRoll(
    @Expose val regex: String,
    @Expose val list: List<List<String>>,
)
