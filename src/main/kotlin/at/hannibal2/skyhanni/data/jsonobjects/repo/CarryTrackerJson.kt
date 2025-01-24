package at.hannibal2.skyhanni.data.jsonobjects.repo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class CarryTrackerJson(
    @Expose @SerializedName("slayer_names") val slayerNames: Map<String, List<String>>,
)
