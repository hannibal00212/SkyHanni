package at.hannibal2.skyhanni.data.jsonobjects.repo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class AnitaUpgradeCostsJson(
    @Expose @SerializedName("level_price") val levelPrice: Map<Int, AnitaUpgradePrice>,
)

data class AnitaUpgradePrice(
    @Expose @SerializedName("gold_medals") val goldMedals: Int,
    @Expose @SerializedName("jacob_tickets") val jacobTickets: Int,
)
