package at.hannibal2.skyhanni.data.jsonobjects.repo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ArmorDropsJson(
    @Expose @SerializedName("special_crops") val specialCrops: Map<String, ArmorDropInfo>,
)

data class ArmorDropInfo(
    @Expose @SerializedName("armor_type") val armorType: String,
    @Expose val chance: List<Double>,
)
