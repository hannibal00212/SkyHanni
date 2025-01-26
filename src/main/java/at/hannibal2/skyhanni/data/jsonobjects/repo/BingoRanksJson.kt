package at.hannibal2.skyhanni.data.jsonobjects.repo

import com.google.gson.annotations.Expose

data class BingoRanksJson(
    @Expose val ranks: Map<String, Int>,
)
