package at.hannibal2.skyhanni.utils

enum class ItemPriceSource(val displayName: String, val sellName: String = displayName, val buyName: String = displayName) {
    BAZAAR_INSTANT_BUY("BZ Instant Buy", buyName = "BZ Sell Offer"),
    BAZAAR_INSTANT_SELL("BZ Instant Sell", buyName = "BZ Buy Order"),
    NPC_SELL("NPC Sell"),
    ;

    override fun toString(): String = displayName
}
