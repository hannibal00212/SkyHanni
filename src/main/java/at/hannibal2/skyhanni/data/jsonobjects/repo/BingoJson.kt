package at.hannibal2.skyhanni.data.jsonobjects.repo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class BingoJson(
    @Expose @SerializedName("bingo_tips") val bingoTips: Map<String, BingoData>,
)

data class BingoData(
    @Expose val difficulty: String,
    @Expose val note: List<String>,
    @Expose val guide: List<String>,
    @Expose val found: String?,
)
