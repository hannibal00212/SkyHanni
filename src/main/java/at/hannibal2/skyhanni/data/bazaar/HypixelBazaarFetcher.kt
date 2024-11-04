package at.hannibal2.skyhanni.data.bazaar

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigManager
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.features.inventory.bazaar.BazaarData
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.APIUtils
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUItems
import at.hannibal2.skyhanni.utils.NEUItems.getItemStackOrNull
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.json.fromJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

// https://api.hypixel.net/#tag/SkyBlock/paths/~1v2~1skyblock~1bazaar/get
@SkyHanniModule
object HypixelBazaarFetcher {
    private const val URL = "https://api.hypixel.net/v2/skyblock/bazaar"
    private const val HIDDEN_FAILED_ATTEMPTS = 3

    var latestProductInformation = mapOf<NEUInternalName, BazaarData>()
    private var lastSuccessfulFetch = SimpleTimeMark.farPast()
    private var nextFetchTime = SimpleTimeMark.farPast()
    private var failedAttempts = 0
    private var nextFetchIsManual = false

    @SubscribeEvent
    fun onDebugDataCollect(event: DebugDataCollectEvent) {
        event.title("Bazaar Data Fetcher from API")

        val data = listOf(
            "failedAttempts: $failedAttempts",
            "nextFetchIsManual: $nextFetchIsManual",
            "nextFetchTime: ${nextFetchTime.timeUntil()}",
            "lastSuccessfulFetch: ${lastSuccessfulFetch.passedSince()}",
        )

        if (failedAttempts == 0) {
            event.addIrrelevant(data)
        } else {
            event.addData(data)
        }
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!canFetch()) return
        SkyHanniMod.coroutineScope.launch {
            fetchAndProcessBazaarData()
        }
    }

    private suspend fun fetchAndProcessBazaarData() {
        nextFetchTime = SimpleTimeMark.now() + 2.minutes
        val fetchType = if (nextFetchIsManual) "manual" else "automatic"
        nextFetchIsManual = false
        try {
            val jsonResponse = withContext(Dispatchers.IO) { APIUtils.getJSONResponse(URL) }.asJsonObject
            val response = ConfigManager.gson.fromJson<BazaarApiResponseJson>(jsonResponse)
            if (response.success) {
                latestProductInformation = process(response.products)
                failedAttempts = 0
                lastSuccessfulFetch = SimpleTimeMark.now()
            } else {
                val rawResponse = jsonResponse.toString()
                onError(fetchType, Exception("success=false, cause=${response.cause}"), rawResponse)
            }
        } catch (e: Exception) {
            onError(fetchType, e)
        }
    }

    private fun process(products: Map<String, BazaarProduct>) = products.mapNotNull { (key, product) ->
        val internalName = NEUItems.transHypixelNameasInternalName(key)
        val sellOfferPrice = product.buySummary.minOfOrNull { it.pricePerUnit } ?: 0.0
        val instantBuyPrice = product.sellSummary.maxOfOrNull { it.pricePerUnit } ?: 0.0

        if (product.quickStatus.isEmpty()) {
            return@mapNotNull null
        }

        if (internalName.getItemStackOrNull() == null) {
            // Items that exist in Hypixel's Bazaar API, but not in NEU repo (not visible in the ingame bazaar).
            // Should only include Enchants
            if (LorenzUtils.debug) println("Unknown bazaar product: $key/$internalName")
            return@mapNotNull null
        }
        internalName to BazaarData(internalName.itemName, sellOfferPrice, instantBuyPrice, product)
    }.toMap()

    private fun BazaarQuickStatus.isEmpty(): Boolean = with(this) {
        sellPrice == 0.0 &&
            sellVolume == 0L &&
            sellMovingWeek == 0L &&
            sellOrders == 0L &&
            buyPrice == 0.0 &&
            buyVolume == 0L &&
            buyMovingWeek == 0L &&
            buyOrders == 0L
    }

    private fun onError(fetchType: String, e: Exception, rawResponse: String? = null) {
        val userMessage = "Failed fetching bazaar price data from hypixel"
        failedAttempts++
        if (failedAttempts <= HIDDEN_FAILED_ATTEMPTS) {
            nextFetchTime = SimpleTimeMark.now() + 15.seconds
            ChatUtils.debug("$userMessage. (errorMessage=${e.message}, failedAttempts=$failedAttempts, $fetchType")
            e.printStackTrace()
        } else {
            nextFetchTime = SimpleTimeMark.now() + 15.minutes
            if (rawResponse == null || rawResponse.toString() == "{}") {
                ChatUtils.chat(
                    "§cFailed loading Bazaar Price data!\n" +
                        "Please wait until the Hypixel API is sending correct data again! There is nothing else to do at the moment.",
                )
            } else {
                ErrorManager.logErrorWithData(
                    e,
                    userMessage,
                    "fetchType" to fetchType,
                    "failedAttempts" to failedAttempts,
                    "rawResponse" to rawResponse,
                )
            }
        }
    }

    fun fetchNow() {
        failedAttempts = 0
        nextFetchIsManual = true
        nextFetchTime = SimpleTimeMark.now()
        ChatUtils.chat("Manually updating the bazaar prices right now..")
    }

    private fun canFetch() = LorenzUtils.onHypixel && nextFetchTime.isInPast()
}
