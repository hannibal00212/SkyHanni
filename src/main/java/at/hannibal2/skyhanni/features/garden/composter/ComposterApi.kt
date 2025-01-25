package at.hannibal2.skyhanni.features.garden.composter

import at.hannibal2.skyhanni.data.model.ComposterUpgrade
import at.hannibal2.skyhanni.features.garden.GardenApi
import at.hannibal2.skyhanni.utils.NumberUtil.formatLong
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.TimeUtils
import kotlin.math.floor
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

object ComposterApi {

    var tabListData = mapOf<ComposterDisplay.DataType, String>()
    val composterUpgrades: MutableMap<ComposterUpgrade, Int>? get() = GardenApi.storage?.composterUpgrades

    fun ComposterUpgrade.getLevel(addOne: ComposterUpgrade?) =
        (composterUpgrades?.get(this) ?: 0) + if (addOne == this) 1 else 0

    fun estimateEmptyTimeFromTab(): Duration? {
        if (composterUpgrades.isNullOrEmpty()) {
            return null
        }

        val nextCompostTime = tabListData[ComposterDisplay.DataType.TIME_LEFT]?.removeColor()?.let {
            if (it != "INACTIVE") TimeUtils.getDuration(it) else null
        } ?: Duration.ZERO

        val timePerCompost = timePerCompost(null)
        val fractionRemaining = nextCompostTime / timePerCompost

        val remainingTimeByOrganicMatter = getDurationUntilEndOfResource(
            getOrganicMatter(), fractionRemaining, organicMatterRequiredPer(null), timePerCompost
        )

        val remainingTimeByFuel = getDurationUntilEndOfResource(
            getFuel(), fractionRemaining, fuelRequiredPer(null), timePerCompost
        )

        return nextCompostTime + minOf(remainingTimeByOrganicMatter, remainingTimeByFuel)
    }

    private fun getDurationUntilEndOfResource(
        amount: Long,
        fractionOfCompostRemaining: Double,
        requiredPer: Double,
        timePerCompost: Duration,
    ): Duration {
        val resourceConsumedByNextCompost = fractionOfCompostRemaining * requiredPer
        val resourceRemainingAfterNextCompostFinishes = amount - resourceConsumedByNextCompost
        val compostRemainingAfterNextCompostFinishes = floor(resourceRemainingAfterNextCompostFinishes / requiredPer)
        return timePerCompost * compostRemainingAfterNextCompostFinishes
    }

    fun getFuel() = tabListData[ComposterDisplay.DataType.FUEL]?.removeColor()?.formatLong() ?: 0

    fun getOrganicMatter() = tabListData[ComposterDisplay.DataType.ORGANIC_MATTER]?.removeColor()?.formatLong() ?: 0

    fun maxOrganicMatter(addOne: ComposterUpgrade?) =
        40_000 + ComposterUpgrade.ORGANIC_MATTER_CAP.getLevel(addOne) * 20_000

    fun multiDropChance(addOne: ComposterUpgrade?) = ComposterUpgrade.MULTI_DROP.getLevel(addOne) * 0.03

    fun maxFuel(addOne: ComposterUpgrade?) = 100_000 + ComposterUpgrade.FUEL_CAP.getLevel(addOne) * 30_000

    fun timePerCompost(addOne: ComposterUpgrade?): Duration {
        val speedUpgrade = ComposterUpgrade.COMPOSTER_SPEED.getLevel(addOne)
        val speedFactor = 1 + speedUpgrade * 0.2
        val baseDuration = 10.minutes
        return baseDuration / speedFactor
    }

    fun organicMatterRequiredPer(addOne: ComposterUpgrade?): Double {
        val costReduction = ComposterUpgrade.COST_REDUCTION.getLevel(addOne)
        val costFactor = 1.0 - costReduction.toDouble() / 100
        return 4_000.0 * costFactor
    }

    fun fuelRequiredPer(addOne: ComposterUpgrade?): Double {
        val costReduction = ComposterUpgrade.COST_REDUCTION.getLevel(addOne)
        val costFactor = 1.0 - costReduction.toDouble() / 100
        return 2_000.0 * costFactor
    }
}
