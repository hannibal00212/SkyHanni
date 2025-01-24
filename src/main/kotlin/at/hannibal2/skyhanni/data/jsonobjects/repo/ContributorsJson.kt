package at.hannibal2.skyhanni.data.jsonobjects.repo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ContributorsJson(
    @Expose val contributors: Map<String, ContributorJsonEntry>,
)

data class ContributorJsonEntry(
    @Expose val suffix: String = "§c:O",
    @Expose val spinny: Boolean = false,
    @Expose val upsideDown: Boolean = false,
    @Expose @SerializedName("external_mod") val externalMod: String? = null,
)
