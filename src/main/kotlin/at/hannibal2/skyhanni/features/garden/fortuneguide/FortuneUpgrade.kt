package at.hannibal2.skyhanni.features.garden.fortuneguide

import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName

data class FortuneUpgrade(
    val description: String,
    val costCopper: Int?,
    private val requiredItemName: String,
    val itemQuantity: Int,
    val fortuneIncrease: Double,
    var cost: Int? = null,
    var costPerFF: Int? = null, // also the same as time to repay
) {
    val requiredItem: NEUInternalName = requiredItemName.toInternalName()
}
