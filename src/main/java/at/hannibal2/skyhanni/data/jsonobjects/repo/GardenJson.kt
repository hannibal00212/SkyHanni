package at.hannibal2.skyhanni.data.jsonobjects.repo

import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.garden.pests.PestType
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NeuInternalName
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class GardenJson(
    @Expose @SerializedName("garden_exp") val gardenExp: List<Int>,
    @Expose @SerializedName("crop_milestones") val cropMilestones: Map<CropType, List<Int>>,
    @Expose @SerializedName("crop_milestone_community_help") val cropMilestoneCommunityHelp: Map<String, Boolean>,
    @Expose val visitors: Map<String, GardenVisitor>,
    @Expose @SerializedName("organic_matter") val organicMatter: Map<NeuInternalName, Double>,
    @Expose val fuel: Map<NeuInternalName, Double>,
    @Expose @SerializedName("personal_best_increment") val personalBestIncrement: Map<CropType, Int>,
    @Expose @SerializedName("pest_rare_drops") val pestRareDrops: Map<PestType, Map<NeuInternalName, Int>>
)

data class GardenVisitor(
    @Expose @SerializedName("rarity") private val _rarity: LorenzRarity,
    @Expose @SerializedName("new_rarity") private val _newRarity: LorenzRarity?,
    @Expose val position: LorenzVec?,
    @Expose var skinOrType: String?,
    @Expose val mode: String,
    @Expose @SerializedName("need_items") val needItems: List<String>,
) {
    val rarity: LorenzRarity
        get() = _newRarity ?: _rarity
}
