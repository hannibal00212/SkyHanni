package at.hannibal2.skyhanni.data.jsonobjects.repo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class MaxwellPowersJson(
    // @Expose val powers: MutableList<String>, old data
    @Expose @SerializedName("maxwell_powers") val maxwellPowers: MutableMap<String, RepoMaxwellPower>,
)

data class RepoMaxwellPower(
    @Expose val name: String,
)
