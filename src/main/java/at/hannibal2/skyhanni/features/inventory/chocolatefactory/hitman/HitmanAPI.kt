package at.hannibal2.skyhanni.features.inventory.chocolatefactory.hitman

import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage.ChocolateFactoryStorage.HitmanStatsStorage
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.inPartialMinutes
import kotlin.math.ceil
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
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
        val existingHunted = this.availableEggs ?: 0
        val firstHuntMeal =
            HoppityEggType.resettingEntries.sortedBy { it.timeUntil() }.firstOrNull { !it.isClaimed() }
                ?: HoppityEggType.resettingEntries.minByOrNull { it.timeUntil() } ?: return Duration.ZERO
        val isAlreadyClaimed = firstHuntMeal.isClaimed()
        val timeToFirstHunt = firstHuntMeal.nextTime() + when {
            isAlreadyClaimed -> 20.minutes
            else -> Duration.ZERO
        }
        var timeToAllHunts = timeToFirstHunt
        var daysTil = 0
        var nextMeal = firstHuntMeal
        for (i in (1 + existingHunted) until huntCount) {
            val candidate = HoppityEggType.resettingEntries.firstOrNull {
                it.resetsAt > nextMeal.resetsAt && it.altDay == nextMeal.altDay
            }
            if (candidate == null) {
                daysTil++
                nextMeal = HoppityEggType.resettingEntries.filter {
                    it.resetsAt < nextMeal.resetsAt && it.altDay != nextMeal.altDay
                }.minByOrNull { it.timeUntil() } ?: return Duration.INFINITE
            } else nextMeal = candidate

            timeToAllHunts = nextMeal.nextTime()
        }
        return timeToAllHunts.timeUntil() + (daysTil * 20.minutes)
    }

    fun HitmanStatsStorage.getOpenSlots(): Int {
        val allSlotsCooldown = this.allSlotsCooldown ?: return MAX_SLOT_COUNT
        if (allSlotsCooldown.isInPast()) return MAX_SLOT_COUNT

        val minutesUntilAll = allSlotsCooldown.timeUntil().inPartialMinutes
        val slotsOnCooldown = ceil(minutesUntilAll / 20).toInt()
        return MAX_SLOT_COUNT - slotsOnCooldown
    }
}
