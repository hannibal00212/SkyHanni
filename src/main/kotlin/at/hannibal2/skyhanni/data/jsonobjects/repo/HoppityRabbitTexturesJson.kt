package at.hannibal2.skyhanni.data.jsonobjects.repo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class HoppityRabbitTexturesJson(
    @Expose @SerializedName("textures") val textures: Map<String, List<HoppityRabbitTextureEntry>>,
)

data class HoppityRabbitTextureEntry(
    @Expose @SerializedName("rabbits") val rabbits: List<String>,
    @Expose @SerializedName("texture_value_b64") val textureValueB64: String,
    @Expose @SerializedName("skull_id") val skullId: String,
    @Expose @SerializedName("texture_id") val textureId: String,
)
