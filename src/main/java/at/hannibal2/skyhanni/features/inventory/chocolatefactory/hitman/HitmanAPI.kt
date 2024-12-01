package at.hannibal2.skyhanni.features.inventory.chocolatefactory.hitman

import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage.ChocolateFactoryStorage.HitmanStatsStorage
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.SkyBlockTime
import at.hannibal2.skyhanni.utils.SkyBlockTime.Companion.plus
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
        if (existingHunted >= huntCount) return Duration.ZERO // Already have enough hunted

        var totalDuration = Duration.ZERO
        var huntsNeeded = huntCount - existingHunted
        var currentTime = SkyBlockTime.now()
        val claimedMeals = mutableSetOf<HoppityEggType>()

        while (huntsNeeded > 0) {
            // Find the next available meal that is not claimed yet
            val availableMeals = HoppityEggType.resettingEntries.filter { !claimedMeals.contains(it) }

            // Calculate timeUntil() from the updated currentTime
            val nextMeal = availableMeals.minByOrNull { it.timeUntil(currentTime) } ?: return Duration.INFINITE
            val timeUntilMeal = nextMeal.timeUntil(currentTime)

            // If the meal is already claimed, wait for the next reset (add 20 minutes)
            val timeToNextHunt = if (nextMeal.isClaimed()) timeUntilMeal + 20.minutes else timeUntilMeal

            // Accumulate total duration and update currentTime
            totalDuration += timeToNextHunt
            currentTime += timeToNextHunt

            // Mark the meal as claimed for future iterations
            claimedMeals.add(nextMeal)
            huntsNeeded--
        }

        return totalDuration
    }

    fun HitmanStatsStorage.getOpenSlots(): Int {
        val allSlotsCooldown = this.allSlotsCooldown ?: return MAX_SLOT_COUNT
        if (allSlotsCooldown.isInPast()) return MAX_SLOT_COUNT

        val minutesUntilAll = allSlotsCooldown.timeUntil().inPartialMinutes
        val slotsOnCooldown = ceil(minutesUntilAll / 20).toInt()
        return MAX_SLOT_COUNT - slotsOnCooldown
    }
}
