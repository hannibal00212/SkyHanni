package at.hannibal2.skyhanni.data.jsonobjects.repo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class FameRankJson(
    @Expose @SerializedName("fame_rank") val fameRank: Map<String, FameRank>,
)

data class FameRank(
    @Expose val name: String,
    @Expose @SerializedName("fame_required") val fameRequired: Int,
    @Expose @SerializedName("bits_multiplier") val bitsMultiplier: Double,
    @Expose val votes: Int,
)
