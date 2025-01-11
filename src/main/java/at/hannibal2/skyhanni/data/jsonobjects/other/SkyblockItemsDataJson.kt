package at.hannibal2.skyhanni.data.jsonobjects.other

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class SkyblockItemsDataJson(
    @Expose val items: List<SkyblockItemData>,
)

data class SkyblockItemData(
    @Expose val id: String?,
    @Expose @SerializedName("npc_sell_price") val npcPrice: Double?,
    @Expose @SerializedName("motes_sell_price") val motesPrice: Double?,
    @Expose @SerializedName("stats") val stats: Map<String, Int>?,
)
