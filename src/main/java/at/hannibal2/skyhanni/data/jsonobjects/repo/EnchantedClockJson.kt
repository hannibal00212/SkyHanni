package at.hannibal2.skyhanni.data.jsonobjects.repo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class EnchantedClockJson(
    @Expose val boosts: List<BoostJson>,
)

data class BoostJson(
    @Expose val name: String,
    @Expose @SerializedName("display_name") val displayName: String,
    @Expose @SerializedName("usage_string") val usageString: String?,
    @Expose val color: String,
    @Expose @SerializedName("display_slot") val displaySlot: Int,
    @Expose @SerializedName("status_slot") val statusSlot: Int,
    @Expose @SerializedName("usage_hours") val cooldownHours: Int = 48,
)
