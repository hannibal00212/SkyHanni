package at.hannibal2.skyhanni.data.jsonobjects.repo

import at.hannibal2.skyhanni.features.fishing.trophy.TrophyRarity
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class TrophyFishJson(
    @Expose @SerializedName("trophy_fish") val trophyFish: Map<String, TrophyFishInfo>,
)

data class TrophyFishInfo(
    @Expose val displayName: String,
    @Expose val description: String,
    @Expose val rate: Int?,
    @Expose val fillet: Map<TrophyRarity, Int>,
)
