package at.hannibal2.skyhanni.data.jsonobjects.other

import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.garden.pests.PestType
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
    @Expose val cropWeight: Map<CropType, Double>,
    @Expose val bonusWeight: Map<String, Int>,
    @Expose val uncountedCrops: Map<CropType, Int>,
    @Expose val pests: Map<PestType, Int>,
)

data class EliteLeaderboardJson(
    @Expose val data: EliteLeaderboard,
)

data class EliteLeaderboard(
    @Expose val rank: Int,
    @Expose val amount: Long,
    @Expose val upcomingRank: Int,
    @Expose val upcomingPlayers: List<UpcomingLeaderboardPlayer>,
)

data class UpcomingLeaderboardPlayer(
    @Expose @SerializedName("ign") val name: String,
    @Expose @SerializedName("amount") val weight: Double,
)

data class EliteWeightsJson(
    @Expose val crops: Map<CropType, Double>,
    @Expose val pests: PestWeightData,
)

data class PestWeightData(
    @Expose val brackets: Map<Int, Int>,
    @Expose @SerializedName("values") val pestWeights: Map<PestType, Map<Int, Double>>,
)

data class EliteCollectionGraphEntry(
    @Expose val timestamp: Long,
    @Expose val crops: Map<CropType, Long>,
)

data class EliteSkillGraphEntry(
    @Expose val timestamp: Long,
    @Expose val skills: Map<String, Long>,
)

data class EliteProfileMember(
    @Expose val farmingWeight: EliteProfileMemberFarmingWeight
)

data class EliteProfileMemberFarmingWeight(
    @Expose val pests: Map<PestType, Int>
)

