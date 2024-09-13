package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.utils.NEUItems.getItemStackOrNull

class NEUInternalName private constructor(private val internalName: String) {

    companion object {

        private val map = mutableMapOf<String, NEUInternalName>()

        val NONE = "NONE".asInternalName()
        val MISSING_ITEM = "MISSING_ITEM".asInternalName()

        val JASPER_CRYSTAL = "JASPER_CRYSTAL".asInternalName()
        val RUBY_CRYSTAL = "RUBY_CRYSTAL".asInternalName()
        val SKYBLOCK_COIN = "SKYBLOCK_COIN".asInternalName()
        val WISP_POTION = "WISP_POTION".asInternalName()

        fun String.asInternalName(): NEUInternalName {
            val internalName = uppercase().replace(" ", "_")
            return map.getOrPut(internalName) { NEUInternalName(internalName) }
        }

        fun fromItemNameOrNull(itemName: String): NEUInternalName? =
            ItemNameResolver.getInternalNameOrNull(itemName.removeSuffix(" Pet")) ?: getCoins(itemName)

        fun fromItemNameOrInternalName(itemName: String): NEUInternalName =
            fromItemNameOrNull(itemName) ?: itemName.asInternalName()

        private fun getCoins(itemName: String): NEUInternalName? = if (isCoins(itemName)) SKYBLOCK_COIN else null

        private fun isCoins(itemName: String): Boolean =
            itemName.lowercase().let {
                when (it) {
                    "coin", "coins",
                    "skyblock coin", "skyblock coins",
                    "skyblock_coin", "skyblock_coins",
                    -> true

                    else -> false
                }
            }


        fun fromItemName(itemName: String): NEUInternalName = fromItemNameOrNull(itemName) ?: run {
            val name = "itemName:$itemName"
            ItemUtils.addMissingRepoItem(name, "Could not find internal name for $name")
            return NEUInternalName.MISSING_ITEM
        }
    }

    fun asString() = internalName

    override fun equals(other: Any?): Boolean {
        if (other is NEUInternalName) {
            return internalName == other.internalName
        }
        return super.equals(other)
    }

    override fun toString(): String = "internalName:$internalName"

    override fun hashCode(): Int = internalName.hashCode()

    fun equals(other: String) = internalName == other

    fun contains(other: String) = internalName.contains(other)

    fun startsWith(other: String) = internalName.startsWith(other)

    fun endsWith(other: String) = internalName.endsWith(other)

    fun replace(oldValue: String, newValue: String) =
        internalName.replace(oldValue.uppercase(), newValue.uppercase()).asInternalName()

    fun isKnownItem(): Boolean = getItemStackOrNull() != null || this == SKYBLOCK_COIN
}
