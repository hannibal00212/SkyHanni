package at.hannibal2.skyhanni.data.jsonobjects.repo.neu

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class NeuSkillLevelJson(
    @Expose @SerializedName("leveling_xp") val levelingXP: List<Int>,
    @Expose @SerializedName("leveling_caps") val levelingCaps: Map<String, Int>,
)
