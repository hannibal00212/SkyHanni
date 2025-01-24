package at.hannibal2.skyhanni.data.jsonobjects.repo

import com.google.gson.annotations.Expose

data class WarpsJson(
    @Expose val warpCommands: List<String>,
)
