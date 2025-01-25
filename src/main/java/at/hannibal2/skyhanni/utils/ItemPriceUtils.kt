package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.features.inventory.bazaar.BazaarApi.getBazaarData
import at.hannibal2.skyhanni.features.inventory.bazaar.HypixelItemApi
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getRecipePrice
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NeuItems.getItemStackOrNull
import at.hannibal2.skyhanni.utils.NeuItems.getRecipes
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.system.PlatformUtils
import com.google.gson.JsonObject
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes

@SkyHanniModule
object ItemPriceUtils {

    private val JACK_O_LANTERN = "JACK_O_LANTERN".toInternalName()
    private val GOLDEN_CARROT = "GOLDEN_CARROT".toInternalName()

    fun NeuInternalName.getPrice(
        priceSource: ItemPriceSource = ItemPriceSource.BAZAAR_INSTANT_BUY,
        pastRecipes: List<PrimitiveRecipe> = emptyList(),
    ) = getPriceOrNull(priceSource, pastRecipes) ?: 0.0

    fun NeuInternalName.getPriceOrNull(
        priceSource: ItemPriceSource = ItemPriceSource.BAZAAR_INSTANT_BUY,
        pastRecipes: List<PrimitiveRecipe> = emptyList(),
    ): Double? {
        when (this) {
            NeuInternalName.GEMSTONE_COLLECTION -> return 0.0
            NeuInternalName.JASPER_CRYSTAL -> return 0.0
            NeuInternalName.RUBY_CRYSTAL -> return 0.0
            NeuInternalName.SKYBLOCK_COIN -> return 1.0
            NeuInternalName.WISP_POTION -> return 20_000.0
            NeuInternalName.ENCHANTED_HAY_BLOCK -> return 7_776.0
            NeuInternalName.TIGHTLY_TIED_HAY_BALE -> return 1_119_744.0
        }

        if (priceSource != ItemPriceSource.NPC_SELL) {
            getBazaarData()?.let {
                return if (priceSource == ItemPriceSource.BAZAAR_INSTANT_BUY) it.sellOfferPrice else it.instantBuyPrice
            }

            getLowestBinOrNull()?.let {
                return it
            }

            if (this == JACK_O_LANTERN) {
                return "PUMPKIN".toInternalName().getPrice(priceSource) + 1
            }
        }
        if (this == GOLDEN_CARROT) {
            // 6.8 for some players
            return 7.0 // NPC price
        }

        return getNpcPriceOrNull() ?: getRawCraftCostOrNull(priceSource, pastRecipes)
    }

    fun NeuInternalName.isAuctionHouseItem(): Boolean = getLowestBinOrNull() != null

    private fun NeuInternalName.getLowestBinOrNull(): Double? {
        val result = if (PlatformUtils.isNeuLoaded()) {
            getNeuLowestBin(this)
        } else {
            getShLowestBin(this)
        }
        if (result == -1L) return null
        return result.toDouble()
    }

    private fun getNeuLowestBin(internalName: NeuInternalName) =
        NotEnoughUpdates.INSTANCE.manager.auctionManager.getLowestBin(internalName.asString())

    // We can not use NEU craft cost, since we want to respect the price source choice
    // NEUItems.manager.auctionManager.getCraftCost(asString())?.craftCost
    fun NeuInternalName.getRawCraftCostOrNull(
        priceSource: ItemPriceSource = ItemPriceSource.BAZAAR_INSTANT_BUY,
        pastRecipes: List<PrimitiveRecipe> = emptyList(),
    ): Double? = getRecipes(this).filter { it !in pastRecipes }
        .map { it.getRecipePrice(priceSource, pastRecipes + it) }
        .filter { it >= 0 }
        .minOrNull()

    fun NeuInternalName.getNpcPrice(): Double = getNpcPriceOrNull() ?: 0.0

    fun NeuInternalName.getNpcPriceOrNull(): Double? {
        if (this == NeuInternalName.WISP_POTION) {
            return 20_000.0
        }
        return HypixelItemApi.getNpcPrice(this)
    }

    fun debugItemPrice(args: Array<String>) {
        val internalName = getItemOrFromHand(args)
        if (internalName == null) {
            ChatUtils.userError("Hold an item in hand or do /shdebugprice <item name/id>")
            return
        }

        val defaultPrice = internalName.getPrice().addSeparators()
        ChatUtils.chat("${internalName.itemName}§f: §6$defaultPrice")

        println("")
        println(" Debug Item Price for $internalName ")
        println("defaultPrice: $defaultPrice")

        println(" #")
        for (source in ItemPriceSource.entries) {
            val price = internalName.getPrice(source)
            println("${source.displayName} price: ${price.addSeparators()}")
        }
        println(" #")

        println(" ")
        println("getLowestBinOrNull: ${internalName.getLowestBinOrNull()?.addSeparators()}")

        internalName.getBazaarData().let {
            println("getBazaarData sellOfferPrice: ${it?.sellOfferPrice?.addSeparators()}")
            println("getBazaarData instantBuyPrice: ${it?.instantBuyPrice?.addSeparators()}")
        }

        println("getNpcPriceOrNull: ${internalName.getNpcPriceOrNull()?.addSeparators()}")
        println("getRawCraftCostOrNull: ${internalName.getRawCraftCostOrNull()?.addSeparators()}")
        println(" ")
    }

    // TODO move either into inventory utils or new command utils
    fun getItemOrFromHand(args: Array<String>): NeuInternalName? {
        val name = args.joinToString(" ")
        return if (name.isEmpty()) {
            InventoryUtils.getItemInHand()?.getInternalName()
        } else {
            val internalName = name.toInternalName()
            if (internalName.getItemStackOrNull() != null) {
                internalName
            } else {
                NeuInternalName.fromItemNameOrNull(name)
            }

        }
    }

    private var lastLowestBinRefresh = SimpleTimeMark.farPast()
    private var lowestBins = JsonObject()

    private fun getShLowestBin(internalName: NeuInternalName): Long {
        if (lowestBins.has(internalName.asString())) {
            return lowestBins[internalName.asString()].asLong
        }

        return -1L
    }

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (PlatformUtils.isNeuLoaded()) return
        if (lastLowestBinRefresh.passedSince() < 2.minutes) return
        lastLowestBinRefresh = SimpleTimeMark.now()

        SkyHanniMod.coroutineScope.launch {
            refreshLowestBins()
        }
    }

    private fun refreshLowestBins() {
        lowestBins = ApiUtils.getJSONResponse("https://moulberry.codes/lowestbin.json.gz", gunzip = true)
    }
}
