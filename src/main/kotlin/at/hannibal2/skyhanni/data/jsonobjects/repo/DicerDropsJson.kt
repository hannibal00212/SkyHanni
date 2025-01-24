package at.hannibal2.skyhanni.data.jsonobjects.repo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class DicerDropsJson(
    @Expose val MELON: DicerType,
    @Expose val PUMPKIN: DicerType,
)

data class DicerType(
    @Expose @SerializedName("total chance") val totalChance: Int,
    @Expose val drops: List<DropInfo>,
)

data class DropInfo(
    @Expose val chance: Int,
    @Expose val amount: List<Int>,
)
