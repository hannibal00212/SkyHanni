package at.hannibal2.skyhanni.features.event.chocolatefactory

import at.hannibal2.skyhanni.features.event.chocolatefactory.ChocolateFactoryAPI.profileStorage
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeUtils.format
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

enum class ChocolateAmount(val chocolate: () -> Long) {
    CURRENT({ profileStorage?.currentChocolate ?: 0 }),
    PRESTIGE({ profileStorage?.chocolateThisPrestige ?: 0 }),
    ALL_TIME({ profileStorage?.chocolateAllTime ?: 0 }),
    ;

    val formatted get(): String = chocolate().addSeparators()

    fun formattedTimeUntilGoal(goal: Long): String {
        val time = timeUntilGoal(goal)
        return when {
            time.isInfinite() -> "§cNever"
            time.isNegative() -> "§aNow"
            else -> "§6${time.format()}"
        }
    }

    private fun timeUntilGoal(goal: Long): Duration {
        val profileStorage = ChocolateFactoryAPI.profileStorage ?: return Duration.ZERO

        val updatedAgo = SimpleTimeMark(profileStorage.lastDataSave).passedSince().inWholeSeconds

        val baseMultiplier = profileStorage.rawChocolateMultiplier
        val rawChocolatePerSecond = profileStorage.rawChocPerSecond
        val timeTowerMultiplier = baseMultiplier + profileStorage.timeTowerLevel * 0.1

        var needed = goal - chocolate()
        val secondsUntilTowerExpires = ChocolateFactoryTimeTowerManager.timeTowerActiveDuration().inWholeSeconds

        val timeTowerChocPerSecond = rawChocolatePerSecond * timeTowerMultiplier

        val secondsAtRate = needed / timeTowerChocPerSecond
        if (secondsAtRate < secondsUntilTowerExpires) {
            return secondsAtRate.seconds - updatedAgo.seconds
        }

        needed -= (secondsUntilTowerExpires * timeTowerChocPerSecond).toLong()
        val basePerSecond = rawChocolatePerSecond * baseMultiplier
        return (needed / basePerSecond + secondsUntilTowerExpires).seconds - updatedAgo.seconds
    }

    companion object {
        fun averageChocPerSecond(
            baseMultiplierIncrease: Double = 0.0,
            rawPerSecondIncrease: Int = 0,
            timeTowerLevelIncrease: Int = 0,
        ): Double {
            val profileStorage = profileStorage ?: return 0.0

            val baseMultiplier = profileStorage.chocolateMultiplier + baseMultiplierIncrease
            val rawPerSecond = profileStorage.rawChocPerSecond + rawPerSecondIncrease
            val timeTowerLevel = profileStorage.timeTowerLevel + timeTowerLevelIncrease

            val timeTowerCooldown = profileStorage.timeTowerCooldown

            val basePerSecond = rawPerSecond * baseMultiplier
            val towerCalc = (rawPerSecond * timeTowerLevel * .1) / timeTowerCooldown

            return basePerSecond + towerCalc
        }

        fun chocPerTimeTower(): Int {
            val profileStorage = profileStorage ?: return 0
            val amountPerSecond = profileStorage.rawChocPerSecond * profileStorage.timeTowerLevel * .1
            val amountPerHour = amountPerSecond * 60 * 60
            return amountPerHour.toInt()
        }
    }
}
