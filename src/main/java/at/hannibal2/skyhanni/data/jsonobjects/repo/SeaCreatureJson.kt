package at.hannibal2.skyhanni.data.jsonobjects.repo

import at.hannibal2.skyhanni.utils.LorenzRarity
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

data class SeaCreatureJson(
    @Expose @SerializedName("chat_color") val chatColor: String,
    @Expose @SerializedName("sea_creatures") val seaCreatures: Map<String, SeaCreatureInfo>,
) {
    companion object {
        val TYPE: Type = object : TypeToken<Map<String?, SeaCreatureJson>>() {}.type
    }
}

data class SeaCreatureInfo(
    @Expose @SerializedName("chat_message") val chatMessage: String,
    @Expose @SerializedName("fishing_experience") val fishingExperience: Int,
    @Expose val rare: Boolean = false,
    @Expose val rarity: LorenzRarity,
)
