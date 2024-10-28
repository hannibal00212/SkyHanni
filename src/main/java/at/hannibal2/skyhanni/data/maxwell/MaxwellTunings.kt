package at.hannibal2.skyhanni.data.maxwell

import com.google.gson.annotations.Expose

data class MaxwellTunings(
    @Expose val value: String,
    @Expose val color: String,
    @Expose var name: String,
    @Expose val icon: String,
)
