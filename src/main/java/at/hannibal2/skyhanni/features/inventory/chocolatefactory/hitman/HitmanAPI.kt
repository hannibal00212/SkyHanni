package at.hannibal2.skyhanni.features.inventory.chocolatefactory.hitman

import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage.ChocolateFactoryStorage.HitmanStatsStorage
import at.hannibal2.skyhanni.features.event.hoppity.HoppityAPI
import at.hannibal2.skyhanni.features.event.hoppity.HoppityAPI.isAlternateDay
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SkyBlockTime
import at.hannibal2.skyhanni.utils.inPartialMinutes
import kotlin.math.ceil
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@SkyHanniModule
object HitmanAPI {

    private val sortedEntries get() = HoppityEggType.sortedResettingEntries
    private val orderOrdinalMap: Map<HoppityEggType, HoppityEggType> by lazy {
        sortedEntries.mapIndexed { index, hoppityEggType ->
            hoppityEggType to sortedEntries[(index + 1) % sortedEntries.size]
        }.toMap()
    }

    /**
     * Get the time until the given number of slots are available.
     */
    private fun HitmanStatsStorage.getTimeToNumSlots(numSlots: Int): Duration {
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
    private fun HitmanStatsStorage.extraSlotsInDuration(duration: Duration, setSlotNumber: Int? = null): Int {
        val currentSlots = (setSlotNumber ?: this.getOpenSlots()).takeIf { it < ((this.purchasedSlots ?: 0)) } ?: return 0
        val slotCooldown = this.slotCooldown ?: return 0
        val minutesUntilSlot = slotCooldown.timeUntil().inPartialMinutes
        if (minutesUntilSlot >= duration.inPartialMinutes) return 0
        for (i in 1..(this.purchasedSlots ?: 0)) {
            // If the next slot would put us at the max slot count, return the number of slots
            if (currentSlots + i == ((this.purchasedSlots ?: 0))) return i
            val minutesUntilSlots = minutesUntilSlot + ((i - 1) * 20)
            if (minutesUntilSlots >= duration.inPartialMinutes) return i
        }
        return 0 // Should never reach here
    }

    /**
     * Return the time until the given number of rabbits can be hunted.
     */
    private fun HitmanStatsStorage.getTimeToHuntCount(huntCount: Int): Duration {
        var nextHuntMeal = sortedEntries.filter { !it.isClaimed() }.minByOrNull { it.timeUntil() }
            ?: sortedEntries.minByOrNull { it.timeUntil() }
            ?: ErrorManager.skyHanniError("Could not find next meal to hunt")

        // Store a list of all the meals that will be available to hunt at their next spawn
        val initialAvailable: MutableList<HoppityEggType> = sortedEntries.filter {
            !it.isClaimed() && it != nextHuntMeal
        }.toMutableList()

        // Will store the total time until the given number of meals can be hunted
        var tilSpawnDuration: Duration =
            if (nextHuntMeal.isClaimed()) nextHuntMeal.timeUntil() + 40.minutes // 40 min for -next- cycle after spawn
            else nextHuntMeal.timeUntil() // Otherwise, just the time until the next spawn

        // Determine if the given meal will 'still' be claimed before the given duration
        fun HoppityEggType.willBeClaimableAfter(duration: Duration): Boolean = this.timeUntil() < duration
        fun HoppityEggType.passesNotClaimed() =
            (initialAvailable.contains(this) || this.willBeClaimableAfter(tilSpawnDuration))

        // Loop through the meals until the given number of meals can be hunted
        for (i in (1 + (this.availableEggs ?: 0))..<huntCount) {
            val candidate = sortedEntries.firstOrNull {
                // Try to find the next meal on the same day
                it.resetsAt > nextHuntMeal.resetsAt && it.altDay == nextHuntMeal.altDay && it.passesNotClaimed()
            } ?: sortedEntries.firstOrNull {
                // Try to find the next meal on the next day
                it.altDay != nextHuntMeal.altDay && it.passesNotClaimed()
            } ?: orderOrdinalMap[nextHuntMeal] ?: ErrorManager.skyHanniError(
                "Could not find next meal to hunt after $nextHuntMeal"
            )

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
            this == another -> 48
            this.altDay != another.altDay -> 24 - another.resetsAt + this.resetsAt
            this.resetsAt > another.resetsAt -> this.resetsAt - another.resetsAt
            else -> another.resetsAt - this.resetsAt
        }
        return (diffInSbHours * SkyBlockTime.SKYBLOCK_HOUR_MILLIS).milliseconds
    }

    /**
     * Return the number of slots that are currently open.
     * This has to be calculated based on the cooldown of all slots,
     * as Hypixel doesn't directly expose this information in the `/cf`
     * menu, and only gives cooldown timers...
     */
    fun HitmanStatsStorage.getOpenSlots(): Int {
        val allSlotsCooldown = this.allSlotsCooldown ?: return this.purchasedSlots ?: 0
        if (allSlotsCooldown.isInPast()) return this.purchasedSlots ?: 0

        val minutesUntilAll = allSlotsCooldown.timeUntil().inPartialMinutes
        val slotsOnCooldown = ceil(minutesUntilAll / 20).toInt()
        return (this.purchasedSlots ?: 0) - slotsOnCooldown
    }

    /**
     * Get the time until slots are full (or the event ends).
     */
    fun HitmanStatsStorage.getHitmanTimeToFull(): Pair<Duration, Boolean> {
        val slotsOpenNow = this.getOpenSlots()
        val eventEndMark = HoppityAPI.getEventEndMark() ?: return Pair(Duration.ZERO, false)

        var slotsToFill = slotsOpenNow
        for (i in (0..99)) { // Runaway protection
            // Calculate time needed to fill this many slots
            val timeToSlots = this.getTimeToHuntCount(slotsToFill)

            // If now plus the time to fill the slots is after the event end, we're done
            if (SimpleTimeMark.now() + timeToSlots > eventEndMark) return Pair(eventEndMark.timeUntil(), true)

            // How many additional slots did we gain in that time?
            val extraSlotsInTime = this.extraSlotsInDuration(timeToSlots, slotsToFill)

            // If we didn't get any extra slots, we're done
            if (extraSlotsInTime == 0) return Pair(timeToSlots, false)

            slotsToFill += extraSlotsInTime
        }
        // Should never reach here
        return Pair(Duration.ZERO, false)
    }

    /**
     * Get the time until ALL purchased slots are full (or the event ends).
     * This is distinct from getHitmanTimeToFull() in that it forces the
     * calculation to use the purchased slot count, not letting itself be
     * inhibited by the cooldown "catching up" to spawn timers.
     */
    fun HitmanStatsStorage.getHitmanTimeToAll(): Pair<Duration, Boolean> {
        val eventEndMark = HoppityAPI.getEventEndMark() ?: return Pair(Duration.ZERO, false)

        val timeToSlots = this.getTimeToNumSlots(this.purchasedSlots ?: 0)
        val timeToHunt = this.getTimeToHuntCount(this.purchasedSlots ?: 0)

        // Figure out which timer is the inhibitor
        val longerTime = if (timeToSlots > timeToHunt) timeToSlots else timeToHunt

        // If the inhibitor is longer than the event end, return the time until the event ends
        if ((SimpleTimeMark.now() + longerTime) > eventEndMark) return Pair(eventEndMark.timeUntil(), true)

        // If the spawns are the inhibitor, return the time until the spawns
        if (timeToHunt > timeToSlots) return Pair(timeToHunt, false)

        // Otherwise if slots are the inhibitor, we need to find the next spawn time after the slots are full
        val timeMarkAllSlots = SimpleTimeMark.now() + timeToSlots
        val sbTimeAllSlots = timeMarkAllSlots.toSkyBlockTime()
        val isAllSlotDayAlt = sbTimeAllSlots.isAlternateDay()

        // Find the first HoppityEggType that spawns after the slots are full
        val nextMealAfterAllSlots = HoppityEggType.sortedResettingEntries.firstOrNull {
            it.resetsAt > sbTimeAllSlots.hour && it.altDay == isAllSlotDayAlt
        } ?: HoppityEggType.sortedResettingEntries.filter {
            it.altDay != isAllSlotDayAlt
        }.minByOrNull { it.resetsAt } ?: ErrorManager.skyHanniError("Could not find next meal after all slots")

        // Return the adjusted time until the next meal
        val dayDiff = if (nextMealAfterAllSlots.altDay != isAllSlotDayAlt) 1 else 0
        val hourDiff = nextMealAfterAllSlots.resetsAt - sbTimeAllSlots.hour + dayDiff * 24
        return Pair(timeToSlots + (hourDiff * SkyBlockTime.SKYBLOCK_HOUR_MILLIS).milliseconds, false)
    }
}
