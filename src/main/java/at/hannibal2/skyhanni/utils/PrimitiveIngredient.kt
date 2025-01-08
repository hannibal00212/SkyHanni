package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.SKYBLOCK_COIN
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.formatDouble

class PrimitiveIngredient(val internalName: NEUInternalName, val count: Double = 1.0) {

    constructor(internalName: NEUInternalName, count: Int) : this(internalName, count.toDouble())

    constructor(ingredientIdentifier: String) : this(
        ingredientIdentifier.substringBefore(':').toInternalName(),
        // if second part is blank, the count is assumed to be 1
        ingredientIdentifier.substringAfter(':', "").let { if (it.isBlank()) 1.0 else it.formatDouble() },
    )

    companion object {
        fun coinIngredient(count: Double = 1.0) = PrimitiveIngredient(SKYBLOCK_COIN, count)

        fun Set<PrimitiveIngredient>.toPrimitiveItemStacks(): List<PrimitiveItemStack> =
            map { it.toPrimitiveItemStack() }
    }

    fun isCoin() = internalName == SKYBLOCK_COIN

    override fun toString() = "$internalName x$count"

    fun toPair() = Pair(internalName, count)

    // TODO should maybe throw an error when trying to use with internalName == SKYBLOCK_COIN
    fun toPrimitiveItemStack() = PrimitiveItemStack(internalName, count.toInt())
}
