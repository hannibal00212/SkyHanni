package at.hannibal2.skyhanni.data.jsonobjects.repo

import at.hannibal2.skyhanni.utils.NEUInternalName
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class GhostDrops(
    @Expose @SerializedName("ghost_drops") val ghostDrops: Set<NEUInternalName>,
)
