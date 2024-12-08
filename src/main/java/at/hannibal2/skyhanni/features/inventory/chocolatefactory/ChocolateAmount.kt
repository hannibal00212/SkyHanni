package at.hannibal2.skyhanni.features.inventory.chocolatefactory

import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryAPI.profileStorage
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeUtils.format
import kotlin.time.Duration

enum class ChocolateAmount(val chocolate: () -> Long) {
    CURRENT({ profileStorage?.currentChocolate ?: 0 }),
    PRESTIGE({ profileStorage?.chocolateThisPrestige ?: 0 }),
    ALL_TIME({ profileStorage?.chocolateAllTime ?: 0 }),
    ;

    val formatted get(): String = (chocolate() + chocolateSinceUpdate()).addSeparators()

    fun formattedTimeUntilGoal(goal: Long): String {
        val time = timeUntilGoal(goal)
        return when {
            time.isInfinite() -> "§cNever"
            time.isNegative() -> "§aNow"
            else -> "§b${time.format()}"
        }
    }

    fun timeUntilGoal(goal: Long): Duration {
        val profileStorage = profileStorage ?: return Duration.ZERO
        val updatedAgo = profileStorage.lastDataSave.passedSince()
        return ChocolateFactoryAPI.timeUntilNeed(goal - chocolate()) - updatedAgo
    }

    companion object {
        fun chocolateSinceUpdate(): Long {
            if (ChocolateFactoryAPI.isMax()) return 0L
            val lastUpdate = profileStorage?.lastDataSave ?: return 0
            val currentTime = SimpleTimeMark.now()
            val secondsSinceUpdate = (currentTime - lastUpdate).inWholeSeconds

            val perSecond = ChocolateFactoryAPI.chocolatePerSecond
            return (perSecond * secondsSinceUpdate).toLong()
        }

        fun averageChocPerSecond(
            baseMultiplierIncrease: Double = 0.0,
            rawPerSecondIncrease: Int = 0,
            includeTower: Boolean = false,
        ): Double {
            val profileStorage = profileStorage ?: return 0.0

            val baseMultiplier = profileStorage.rawChocolateMultiplier + baseMultiplierIncrease
            val rawPerSecond = profileStorage.rawChocPerSecond + rawPerSecondIncrease

            val timeTowerCooldown = profileStorage.timeTowerCooldown

            val basePerSecond = rawPerSecond * baseMultiplier
            if (!includeTower) return basePerSecond
            val towerCalc = (rawPerSecond * .1) / timeTowerCooldown

            return basePerSecond + towerCalc
        }

        fun addToAll(amount: Long) {
            profileStorage?.let {
                it.currentChocolate += amount
                it.chocolateThisPrestige += amount
                it.chocolateAllTime += amount
                updateBestUpgrade()
            }
        }

        fun addToCurrent(amount: Long) {
            profileStorage?.let {
                it.currentChocolate += amount
                updateBestUpgrade()
            }
        }

        private fun updateBestUpgrade() {
            profileStorage?.let {
                if (it.bestUpgradeAvailableAt.isFarPast() || it.bestUpgradeCost == 0L) return
                val canAffordAt = SimpleTimeMark.now() + CURRENT.timeUntilGoal(it.bestUpgradeCost)
                it.bestUpgradeAvailableAt = canAffordAt
            }
        }
    }
}
