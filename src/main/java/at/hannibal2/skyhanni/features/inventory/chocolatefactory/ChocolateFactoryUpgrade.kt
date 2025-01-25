package at.hannibal2.skyhanni.features.inventory.chocolatefactory

import at.hannibal2.skyhanni.utils.SimpleTimeMark

data class ChocolateFactoryUpgrade(
    val slotIndex: Int,
    val level: Int,
    val price: Long?,
    val extraPerSecond: Double? = null,
    val effectiveCost: Double? = null,
    val isRabbit: Boolean = false,
    val isPrestige: Boolean = false,
) {
    private var chocolateAmountType = ChocolateAmount.CURRENT
    val isMaxed = price == null
    var canAffordAt: SimpleTimeMark? = null

    init {
        if (isPrestige) {
            chocolateAmountType = ChocolateAmount.PRESTIGE
        }
        val canAffordIn = chocolateAmountType.timeUntilGoal(price ?: 0)
        canAffordAt = when {
            canAffordIn.isInfinite() -> SimpleTimeMark.farFuture()
            else -> SimpleTimeMark.now() + canAffordIn
        }
    }

    fun canAfford(): Boolean {
        if (price == null) return false
        return chocolateAmountType.chocolate() >= price
    }

    fun formattedTimeUntilGoal(): String {
        return chocolateAmountType.formattedTimeUntilGoal(price ?: 0)
    }

    fun stackTip(): String {
        return when {
            level == 0 -> ""
            isMaxed -> "§a✔"

            isRabbit -> when (level) {
                in (0..9) -> ""
                in (10..74) -> "§a"
                in (75..124) -> "§9"
                in (125..174) -> "§5"
                in (175..199) -> "§6"
                in (200..219) -> "§d"
                in (220..225) -> "§b"
                else -> "§c"
            } + level

            else -> "$level"
        }
    }

    fun getValidUpgradeIndex(): Int {
        return when (slotIndex) {
            in ignoredSlotIndexes -> -1
            else -> slotIndex
        }
    }

    companion object {
        var ignoredSlotIndexes = listOf<Int>()

        fun updateIgnoredSlots() {
            ignoredSlotIndexes = listOf(
                ChocolateFactoryApi.prestigeIndex,
                ChocolateFactoryApi.handCookieIndex,
                ChocolateFactoryApi.shrineIndex,
                ChocolateFactoryApi.barnIndex,
            )
        }
    }
}
