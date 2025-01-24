package at.hannibal2.skyhanni.data.jsonobjects.repo

import com.google.gson.annotations.Expose

data class MaxwellPowersJson(
    @Expose val powers: MutableList<String>,
)
