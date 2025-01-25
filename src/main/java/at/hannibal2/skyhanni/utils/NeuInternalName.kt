package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.utils.NeuItems.getItemStackOrNull

class NeuInternalName private constructor(private val internalName: String) {

    companion object {

        private val internalNameMap = mutableMapOf<String, NeuInternalName>()

        val NONE = "NONE".toInternalName()
        val MISSING_ITEM = "MISSING_ITEM".toInternalName()

        val GEMSTONE_COLLECTION = "GEMSTONE_COLLECTION".toInternalName()
        val JASPER_CRYSTAL = "JASPER_CRYSTAL".toInternalName()
        val RUBY_CRYSTAL = "RUBY_CRYSTAL".toInternalName()
        val SKYBLOCK_COIN = "SKYBLOCK_COIN".toInternalName()
        val WISP_POTION = "WISP_POTION".toInternalName()
        val ENCHANTED_HAY_BLOCK = "ENCHANTED_HAY_BLOCK".toInternalName()
        val TIGHTLY_TIED_HAY_BALE = "TIGHTLY_TIED_HAY_BALE".toInternalName()

        fun String.toInternalName(): NeuInternalName = uppercase().replace(" ", "_").let {
            internalNameMap.getOrPut(it) { NeuInternalName(it) }
        }

        fun Set<String>.toInternalNames(): Set<NeuInternalName> = mapTo(mutableSetOf()) { it.toInternalName() }
        fun List<String>.toInternalNames(): List<NeuInternalName> = mapTo(mutableListOf()) { it.toInternalName() }

        private val itemNameCache = mutableMapOf<String, NeuInternalName?>()

        fun fromItemNameOrNull(itemName: String): NeuInternalName? = itemNameCache.getOrPut(itemName) {
            ItemNameResolver.getInternalNameOrNull(itemName.removeSuffix(" Pet")) ?: getCoins(itemName)
        }

        fun fromItemNameOrInternalName(itemName: String): NeuInternalName = fromItemNameOrNull(itemName) ?: itemName.toInternalName()

        private fun getCoins(itemName: String): NeuInternalName? = when {
            isCoins(itemName) -> SKYBLOCK_COIN
            else -> null
        }

        private val coinNames = setOf(
            "coin", "coins",
            "skyblock coin", "skyblock coins",
            "skyblock_coin", "skyblock_coins",
        )

        private fun isCoins(itemName: String): Boolean = itemName.lowercase() in coinNames

        fun fromItemName(itemName: String): NeuInternalName = fromItemNameOrNull(itemName) ?: run {
            val name = "itemName:$itemName"
            ItemUtils.addMissingRepoItem(name, "Could not find internal name for $name")
            MISSING_ITEM
        }
    }

    fun asString() = internalName

    override fun equals(other: Any?) = this === other

    override fun toString(): String = "internalName:$internalName"

    override fun hashCode(): Int = internalName.hashCode()

    fun contains(other: String) = internalName.contains(other)

    fun startsWith(other: String) = internalName.startsWith(other)

    fun endsWith(other: String) = internalName.endsWith(other)

    fun replace(oldValue: String, newValue: String): NeuInternalName =
        internalName.replace(oldValue, newValue, ignoreCase = true).toInternalName()

    fun isKnownItem(): Boolean = getItemStackOrNull() != null || this == SKYBLOCK_COIN
}
