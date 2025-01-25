package at.hannibal2.skyhanni.data.jsonobjects.repo

import at.hannibal2.skyhanni.utils.NeuInternalName
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class GhostDropsJson(
    @Expose @SerializedName("ghost_drops") val ghostDrops: Set<NeuInternalName>,
)
