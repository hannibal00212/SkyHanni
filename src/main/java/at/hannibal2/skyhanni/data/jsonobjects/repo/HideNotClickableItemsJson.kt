package at.hannibal2.skyhanni.data.jsonobjects.repo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class HideNotClickableItemsJson(
    @Expose @SerializedName("hide_npc_sell") val hideNpcSell: MultiFilterJson,
    @Expose @SerializedName("hide_in_storage") val hideInStorage: MultiFilterJson,
    @Expose @SerializedName("hide_player_trade") val hidePlayerTrade: MultiFilterJson,
    @Expose @SerializedName("not_auctionable") val notAuctionable: MultiFilterJson,
    @Expose val salvage: SalvageFilter,
)

data class SalvageFilter(
    @Expose val armor: List<String>,
    @Expose val items: List<String>,
)
