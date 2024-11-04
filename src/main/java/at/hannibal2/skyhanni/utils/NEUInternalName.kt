package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.utils.NEUItems.getItemStackOrNull

class NEUInternalName private constructor(private val internalName: String) {

    companion object {

        private val internalNameMap = mutableMapOf<String, NEUInternalName>()

        val NONE = "NONE".asInternalName()
        val MISSING_ITEM = "MISSING_ITEM".asInternalName()

        val JASPER_CRYSTAL = "JASPER_CRYSTAL".asInternalName()
        val RUBY_CRYSTAL = "RUBY_CRYSTAL".asInternalName()
        val SKYBLOCK_COIN = "SKYBLOCK_COIN".asInternalName()
        val WISP_POTION = "WISP_POTION".asInternalName()

        fun String.asInternalName(): NEUInternalName = uppercase().replace(" ", "_").let {
            internalNameMap.getOrPut(it) { NEUInternalName(it) }
        }

        private val itemNameCache = mutableMapOf<String, NEUInternalName?>()

        fun fromItemNameOrNull(itemName: String): NEUInternalName? = itemNameCache.getOrPut(itemName) {
            ItemNameResolver.getInternalNameOrNull(itemName.removeSuffix(" Pet")) ?: getCoins(itemName)
        }

        fun fromItemNameOrInternalName(itemName: String): NEUInternalName = fromItemNameOrNull(itemName) ?: itemName.asInternalName()

        private fun getCoins(itemName: String): NEUInternalName? = when {
            isCoins(itemName) -> SKYBLOCK_COIN
            else -> null
        }

        private val coinNames = setOf(
            "coin", "coins",
            "skyblock coin", "skyblock coins",
            "skyblock_coin", "skyblock_coins",
        )

        private fun isCoins(itemName: String): Boolean = itemName.lowercase() in coinNames

        fun fromItemName(itemName: String): NEUInternalName = fromItemNameOrNull(itemName) ?: run {
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

    fun replace(oldValue: String, newValue: String): NEUInternalName =
        internalName.replace(oldValue, newValue, ignoreCase = true).asInternalName()

    fun isKnownItem(): Boolean = getItemStackOrNull() != null || this == SKYBLOCK_COIN
}
