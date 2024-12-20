package at.hannibal2.skyhanni.data.jsonobjects.repo

import at.hannibal2.skyhanni.utils.NEUInternalName
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class InfernoMinionFuelsJson(
    @Expose @SerializedName("inferno_minion_fuels") val minionFuels: List<NEUInternalName>,
)
