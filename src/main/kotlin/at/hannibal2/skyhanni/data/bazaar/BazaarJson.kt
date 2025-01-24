package at.hannibal2.skyhanni.data.bazaar

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class BazaarApiResponseJson(
    @Expose val success: Boolean,
    @Expose val cause: String,
    @Expose val lastUpdated: Long,
    @Expose val products: Map<String, BazaarProduct>,
)

data class BazaarProduct(
    @Expose @SerializedName("product_id") val productId: String,
    @Expose @SerializedName("quick_status") val quickStatus: BazaarQuickStatus,
    @Expose @SerializedName("sell_summary") val sellSummary: List<BazaarSummary>,
    @Expose @SerializedName("buy_summary") val buySummary: List<BazaarSummary>,
)

class BazaarQuickStatus(
    @Expose val productId: String,
    @Expose val sellPrice: Double,
    @Expose val sellVolume: Long,
    @Expose val sellMovingWeek: Long,
    @Expose val sellOrders: Long,
    @Expose val buyPrice: Double,
    @Expose val buyVolume: Long,
    @Expose val buyMovingWeek: Long,
    @Expose val buyOrders: Long,
)

data class BazaarSummary(
    @Expose val amount: Long,
    @Expose val pricePerUnit: Double,
    @Expose val orders: Long,
)
