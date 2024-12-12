package at.hannibal2.skyhanni.data.jsonobjects.repo

import com.google.gson.annotations.Expose

data class EnchantedClockJson(
    @Expose val boosts: List<BoostJson>
)

data class BoostJson(
    @Expose val name: String,
    @Expose val displayName: String,
    @Expose val usageString: String?,
    @Expose val color: String,
    @Expose val displaySlot: Int,
    @Expose val statusSlot: Int,
    @Expose val cooldownHours: Int = 48,
)
