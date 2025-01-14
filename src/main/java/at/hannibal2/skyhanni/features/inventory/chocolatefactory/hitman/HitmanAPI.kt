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
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object HitmanAPI {

    private const val MINUTES_PER_DAY = 20 // Real minutes per SkyBlock day
    private const val SB_HR_PER_DAY = 24 // SkyBlock hours per day
    private val sortedEntries get() = HoppityEggType.sortedResettingEntries
    private val orderOrdinalMap: Map<HoppityEggType, HoppityEggType> by lazy {
        sortedEntries.mapIndexed { index, hoppityEggType ->
            hoppityEggType to sortedEntries[(index + 1) % sortedEntries.size]
        }.toMap()
    }

    /**
     * Determine if the given meal will 'still' be claimed before the given duration
     */
    private fun HoppityEggType.willBeClaimableAfter(duration: Duration): Boolean = timeUntil() < duration

    /**
     * Get the time until the given number of slots are available.
     */
    private fun HitmanStatsStorage.getTimeToNumSlots(numSlots: Int): Duration {
        val currentSlots = getOpenSlots().takeIf { it < numSlots } ?: return Duration.ZERO
        val slotCooldown = singleSlotCooldownMark ?: return Duration.ZERO
        // Determine how many slots are on cooldown, -1 to account for the current slot (partial time)
        val slotsOnCooldown = numSlots - currentSlots - 1
        return slotCooldown.timeUntil() + (slotsOnCooldown * MINUTES_PER_DAY).minutes
    }

    /**
     * Return the number of extra slots that will be available after the given duration.
     */
    private fun HitmanStatsStorage.extraSlotsInDuration(duration: Duration, fromZeroMark: Boolean = false): Int {
        val timeToNextSlot = if (fromZeroMark) 0.seconds else singleSlotCooldownMark?.timeUntil() ?: return 0
        return ceil((duration - timeToNextSlot).inPartialMinutes / MINUTES_PER_DAY).toInt()
    }

    /**
     * Determine the first meal that would be hunted by Hitman if given an infinite amount of time.
     */
    private fun getFirstHuntedMeal(): HoppityEggType =
        sortedEntries.filter { !it.isClaimed() }.minByOrNull { it.timeUntil() }
            ?: sortedEntries.minByOrNull { it.timeUntil() }
            ?: ErrorManager.skyHanniError("Could not find initial meal to hunt")

    /**
     * Determine the next meal that would be hunted by Hitman if given an infinite amount of time.
     */
    private fun getNextHuntedMeal(
        previousMeal: HoppityEggType,
        duration: Duration,
    ): HoppityEggType = sortedEntries
        .filter { it.willBeClaimableAfter(duration) }
        .let { passingEggs ->
            passingEggs.firstOrNull { it.resetsAt > previousMeal.resetsAt && it.altDay == previousMeal.altDay }
                ?: passingEggs.firstOrNull { it.altDay != previousMeal.altDay }
                ?: orderOrdinalMap[previousMeal]
                ?: ErrorManager.skyHanniError("Could not find next meal to hunt after $previousMeal")
        }

    /**
     * Return the time until the given number of rabbits can be hunted.
     */
    private fun HitmanStatsStorage.getTimeToHuntCount(targetHuntCount: Int): Duration {
        // Determine how many hunts we need to perform
        var huntsToPerform = (targetHuntCount - availableHitmanEggs)
        if (huntsToPerform <= 0) return Duration.ZERO

        // Determine which pre-available meals we have, to determine better the first hunt
        val initialClaimable = sortedEntries.filter {
            !it.isClaimed()
        }.sortedBy {
            it.timeUntil()
        }.toMutableList()

        // If the claimable eggs will cover the number of hunts we need to perform, just return the time until the last meal
        if (huntsToPerform <= initialClaimable.size) return initialClaimable.take(huntsToPerform).last().timeUntil()

        // Determine the next (first) meal to hunt
        var nextHuntMeal = initialClaimable.maxByOrNull {
            it.timeUntil()
        } ?: getFirstHuntedMeal()

        // -1 as default to account for the initial meal
        val initialRemoval = initialClaimable.size.takeIf { it > 0 } ?: 1
        huntsToPerform -= initialRemoval

        // Will store the total time until the given number of meals can be hunted
        var tilSpawnDuration =
            if (nextHuntMeal.isClaimed()) nextHuntMeal.timeUntil() + (MINUTES_PER_DAY * 2).minutes // -next- cycle after spawn
            else nextHuntMeal.timeUntil() // Otherwise, just the time until the next spawn

        // Loop through the meals until the given number of meals can be hunted
        repeat(huntsToPerform) { _ ->
            // Determine the next meal to hunt, and cycle through
            nextHuntMeal = getNextHuntedMeal(nextHuntMeal, tilSpawnDuration).let {
                tilSpawnDuration += it.timeFromAnother(nextHuntMeal)
                it
            }
        }

        return tilSpawnDuration
    }

    /**
     * Return the duration between two HoppityEggTypes' spawn times.
     */
    private fun HoppityEggType.timeFromAnother(another: HoppityEggType): Duration {
        val diffInSbHours = when {
            this == another -> (SB_HR_PER_DAY * 2)
            altDay != another.altDay -> SB_HR_PER_DAY - another.resetsAt + resetsAt
            resetsAt > another.resetsAt -> resetsAt - another.resetsAt
            else -> (SB_HR_PER_DAY * 2) - (resetsAt - another.resetsAt)
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
        val allSlotsCooldownDuration = allSlotsCooldownMark?.takeIf {
            it.isInFuture()
        }?.timeUntil() ?: return purchasedHitmanSlots
        val slotsOnCooldown = ceil(allSlotsCooldownDuration.inPartialMinutes / MINUTES_PER_DAY).toInt()
        return purchasedHitmanSlots - slotsOnCooldown - availableHitmanEggs
    }

    /**
     * Get the time until slots are full (number of spawns 'catches up' to number of slots).
     * If the event ends before the slots are full, the time until the event ends is returned.
     * The boolean indicates if the Duration is "Event Inhibited" (True)
     */
    fun HitmanStatsStorage.getTimeToFull(): Pair<Duration, Boolean> {
        val eventEndMark = HoppityAPI.getEventEndMark() ?: return Pair(Duration.ZERO, false)

        var slotsToFill = getOpenSlots().takeIf { it > 0 } ?: return Pair(Duration.ZERO, false)
        repeat(20) { // Runaway protection
            // Calculate time needed to fill this many slots
            val timeToFill = getTimeToHuntCount(slotsToFill)

            // If now plus the time to fill the slots is after the event end, we're done
            if (SimpleTimeMark.now() + timeToFill > eventEndMark) return Pair(eventEndMark.timeUntil(), true)

            // How many additional slots did we gain in that time?
            val extraSlotsInTime = extraSlotsInDuration(timeToFill, true)

            // If we didn't get any extra slots, or we have enough slots to fill, we're done
            // Otherwise set the adjusted number of slots we can fill
            slotsToFill = (getOpenSlots() + extraSlotsInTime).coerceAtMost(purchasedHitmanSlots).takeIf { newVal ->
                newVal != slotsToFill && extraSlotsInTime != 0
            } ?: return Pair(timeToFill, false)
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

        val timeToSlots = getTimeToNumSlots(purchasedHitmanSlots)
        val timeToHunt = getTimeToHuntCount(purchasedHitmanSlots)

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
        val sbDayDiff = if (nextMealAfterAllSlots.altDay != isAllSlotDayAlt) 1 else 0
        val sbHourDiff = nextMealAfterAllSlots.resetsAt - sbTimeAllSlots.hour + sbDayDiff * SB_HR_PER_DAY
        return Pair(timeToSlots + (sbHourDiff * SkyBlockTime.SKYBLOCK_HOUR_MILLIS).milliseconds, false)
    }
}
