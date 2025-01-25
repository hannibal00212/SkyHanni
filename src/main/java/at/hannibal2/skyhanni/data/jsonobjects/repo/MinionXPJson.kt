package at.hannibal2.skyhanni.data.jsonobjects.repo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class MinionXPJson(
    @Expose @SerializedName("minion_xp") val minionXP: Map<String, Map<String, Double>>,
)
