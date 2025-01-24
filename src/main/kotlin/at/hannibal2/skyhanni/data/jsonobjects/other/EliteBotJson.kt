package at.hannibal2.skyhanni.data.jsonobjects.other

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ElitePlayerWeightJson(
    @Expose val selectedProfileId: String,
    @Expose val profiles: List<WeightProfile>,
)

data class WeightProfile(
    @Expose val profileId: String,
    @Expose val profileName: String,
    @Expose val totalWeight: Double,
    @Expose val cropWeight: Map<String, Double>,
    @Expose val bonusWeight: Map<String, Int>,
    @Expose val uncountedCrops: Map<String, Int>,
    @Expose val pests: Map<String, Int>,
)

data class EliteLeaderboardJson(
    @Expose val data: EliteLeaderboard,
)

data class EliteLeaderboard(
    @Expose val rank: Int,
    @Expose val upcomingRank: Int,
    @Expose val upcomingPlayers: List<UpcomingLeaderboardPlayer>,
)

data class UpcomingLeaderboardPlayer(
    @Expose @SerializedName("ign") val name: String,
    @Expose @SerializedName("amount") val weight: Double,
)

data class EliteWeightsJson(
    @Expose val crops: Map<String, Double>,
    @Expose val pests: PestWeightData,
)

data class PestWeightData(
    @Expose val brackets: Map<Int, Int>,
    @Expose @SerializedName("values") val pestWeights: Map<String, Map<Int, Double>>,
)
