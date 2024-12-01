package at.hannibal2.skyhanni.features.inventory.chocolatefactory.hitman

import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage.ChocolateFactoryStorage.HitmanStatsStorage
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.inPartialMinutes
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.times

@SkyHanniModule
object HitmanAPI {

    private val MAX_SLOT_COUNT get() = ChocolateFactoryAPI.hitmanCosts.size

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
        val firstHuntMeal =
            HoppityEggType.sortedEntries.firstOrNull { !it.isClaimed() }
                ?: HoppityEggType.resettingEntries.minByOrNull { it.timeUntil() } ?: return Duration.ZERO

        var nextHuntMeal = firstHuntMeal
        var tilSpawnDuration = nextHuntMeal.timeUntil()

        val boundedHuntRange = (1 + (this.availableEggs ?: 0))..huntCount

        for (i in boundedHuntRange) {
            // Try to find the next meal on the same day
            var candidate = HoppityEggType.sortedEntries.firstOrNull {
                it.resetsAt > nextHuntMeal.resetsAt && it.altDay == nextHuntMeal.altDay &&
                    (!it.isClaimed() || !willBeClaimed(it, tilSpawnDuration))
            }
            candidate?.let { tilSpawnDuration = realTimeUntil(it, tilSpawnDuration) }

            // Try to find the next meal on the next day
            if (candidate == null) {
                candidate = HoppityEggType.sortedEntries.firstOrNull {
                    it.resetsAt == nextHuntMeal.resetsAt + 1 && it.altDay != nextHuntMeal.altDay &&
                        (!it.isClaimed() || !willBeClaimed(it, tilSpawnDuration))
                }
                candidate?.let { tilSpawnDuration = realTimeUntil(it, tilSpawnDuration) }
            }

            // If no candidate was found, return the time until the last candidate
            if (candidate == null) {
                candidate = nextHuntMeal
                tilSpawnDuration = realTimeUntil(candidate, tilSpawnDuration)
            }

            nextHuntMeal = candidate
        }
        return tilSpawnDuration
    }

    private fun realTimeUntil(meal: HoppityEggType, tilSpawnDuration: Duration): Duration {
        val timeUntil = meal.timeUntil()
        return (tilSpawnDuration / 20.minutes).toInt() * 20.minutes + timeUntil
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
