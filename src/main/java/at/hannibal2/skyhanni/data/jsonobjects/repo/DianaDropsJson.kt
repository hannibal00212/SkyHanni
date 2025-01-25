package at.hannibal2.skyhanni.data.jsonobjects.repo

import at.hannibal2.skyhanni.utils.NeuInternalName
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class DianaDropsJson(
    @Expose @SerializedName("diana_drops") val dianaDrops: List<NeuInternalName>,
)
