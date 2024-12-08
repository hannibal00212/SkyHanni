package at.hannibal2.skyhanni.data.jsonobjects.repo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class IslandTypeJson(
    @Expose val islands: Map<String, IslandJson>,
    @Expose @SerializedName("max_players") val maxPlayers: Int,
    @Expose @SerializedName("max_players_mega") val maxPlayersMega: Int,
)

data class IslandJson(
    @Expose val name: String,
    @Expose @SerializedName("api_name") val apiName: String? = null,
    @Expose @SerializedName("max_players") val maxPlayers: Int? = null,
)
