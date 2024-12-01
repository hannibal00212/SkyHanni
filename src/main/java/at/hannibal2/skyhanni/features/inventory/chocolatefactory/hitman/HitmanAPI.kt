package at.hannibal2.skyhanni.features.inventory.chocolatefactory.hitman

import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage.ChocolateFactoryStorage.HitmanStatsStorage
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.SkyBlockTime
import at.hannibal2.skyhanni.utils.inPartialMinutes
import kotlin.math.ceil
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@SkyHanniModule
object HitmanAPI {

    private val MAX_SLOT_COUNT get() = ChocolateFactoryAPI.hitmanCosts.size
    private val sortedEntries get() = HoppityEggType.sortedResettingEntries
    private val orderOrdinalMap: Map<HoppityEggType, HoppityEggType> by lazy {
        sortedEntries.mapIndexed { index, hoppityEggType ->
            hoppityEggType to sortedEntries[(index + 1) % sortedEntries.size]
        }.toMap()
    }

    /**
     * Get the time until the given number of slots are available.
     */
    fun HitmanStatsStorage.getTimeToNumSlots(numSlots: Int): Duration {
        val currentSlots = this.getOpenSlots()
        if (currentSlots >= numSlots) return Duration.ZERO
        val slotCooldown = this.slotCooldown ?: return Duration.ZERO
        val minutesUntilSlot = slotCooldown.timeUntil().inPartialMinutes
        val minutesUntilSlots = minutesUntilSlot + ((numSlots - currentSlots - 1) * 20)
        return minutesUntilSlots.minutes
    }

    /**
     * Return the number of extra slots that will be available after the given duration.
     */
    fun HitmanStatsStorage.extraSlotsInDuration(duration: Duration, setSlotNumber: Int? = null): Int {
        val currentSlots = (setSlotNumber ?: this.getOpenSlots()).takeIf { it < MAX_SLOT_COUNT } ?: return 0
        val slotCooldown = this.slotCooldown ?: return 0
        val minutesUntilSlot = slotCooldown.timeUntil().inPartialMinutes
        if (minutesUntilSlot >= duration.inPartialMinutes) return 0
        for (i in 1..MAX_SLOT_COUNT) {
            // If the next slot would put us at the max slot count, return the number of slots
            if (currentSlots + i == MAX_SLOT_COUNT) return i
            val minutesUntilSlots = minutesUntilSlot + ((i - 1) * 20)
            if (minutesUntilSlots >= duration.inPartialMinutes) return i
        }
        return 0 // Should never reach here
    }

    /**
     * Return the time until the given number of rabbits can be hunted.
     */
    fun HitmanStatsStorage.getTimeToHuntCount(huntCount: Int): Duration {
        var nextHuntMeal = sortedEntries.firstOrNull { !it.isClaimed() }
            ?: sortedEntries.firstOrNull() ?: return Duration.ZERO

        val initialAvailable: MutableList<HoppityEggType> = sortedEntries.filter {
            !it.isClaimed() && it != nextHuntMeal
        }.toMutableList()

        var tilSpawnDuration = if (nextHuntMeal.isClaimed()) nextHuntMeal.timeUntil() else Duration.ZERO

        fun HoppityEggType.passesNotClaimed() =
            (initialAvailable.contains(this) || !willBeClaimed(this, tilSpawnDuration))

        for (i in (1 + (this.availableEggs ?: 0))..<huntCount) {
            val candidate = sortedEntries.firstOrNull {
                // Try to find the next meal on the same day
                it.resetsAt > nextHuntMeal.resetsAt && it.altDay == nextHuntMeal.altDay && it.passesNotClaimed()
            } ?: sortedEntries.firstOrNull {
                // Try to find the next meal on the next day
                it.altDay != nextHuntMeal.altDay && it.passesNotClaimed()
            } ?: orderOrdinalMap[nextHuntMeal] ?: return Duration.ZERO

            if (initialAvailable.contains(candidate)) initialAvailable.remove(candidate)
            else tilSpawnDuration += candidate.timeFromAnother(nextHuntMeal)

            nextHuntMeal = candidate
        }
        return tilSpawnDuration
    }

    /**
     * Return the duration between two HoppityEggTypes' spawn times.
     */
    private fun HoppityEggType.timeFromAnother(another: HoppityEggType): Duration {
        val diffInSbHours = when {
            this.altDay != another.altDay -> 24 - another.resetsAt + this.resetsAt
            this.resetsAt == another.resetsAt -> 48
            this.resetsAt > another.resetsAt -> this.resetsAt - another.resetsAt
            else -> another.resetsAt - this.resetsAt
        }
        return (diffInSbHours * SkyBlockTime.SKYBLOCK_HOUR_MILLIS).milliseconds
    }

    private fun willBeClaimed(meal: HoppityEggType, afterDuration: Duration): Boolean {
        if (!meal.isClaimed()) return false
        return afterDuration < meal.timeUntil()
    }

    fun HitmanStatsStorage.getOpenSlots(): Int {
        val allSlotsCooldown = this.allSlotsCooldown ?: return MAX_SLOT_COUNT
        if (allSlotsCooldown.isInPast()) return MAX_SLOT_COUNT

        val minutesUntilAll = allSlotsCooldown.timeUntil().inPartialMinutes
        val slotsOnCooldown = ceil(minutesUntilAll / 20).toInt()
        return MAX_SLOT_COUNT - slotsOnCooldown
    }
}
