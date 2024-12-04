package at.hannibal2.skyhanni.data.jsonobjects.repo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class MiningJson(
    @Expose @SerializedName("block_strengths") val blockStrengths: Map<String, Int>,
)
