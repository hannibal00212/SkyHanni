package at.hannibal2.skyhanni.features.event.hoppity

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.hoppity.EggSpawnedEvent
import at.hannibal2.skyhanni.features.event.hoppity.HoppityAPI.isAlternateDay
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.CollectionUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.asTimeMark
import at.hannibal2.skyhanni.utils.SkyBlockTime
import at.hannibal2.skyhanni.utils.SkyblockSeason
import java.util.regex.Matcher
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

enum class HoppityEggType(
    val mealName: String,
    private val mealColor: String,
    val resetsAt: Int,
    private var claimed: Boolean = false,
    private var lastReset: SkyBlockTime = SkyBlockTime.fromSbYear(0),
    val altDay: Boolean = false
) {
    BREAKFAST("Breakfast", "§6", 7),
    LUNCH("Lunch", "§9", 14),
    DINNER("Dinner", "§a", 21),
    BRUNCH("Brunch", "§6", 7, altDay = true),
    DEJEUNER("Déjeuner", "§9", 14, altDay = true),
    SUPPER("Supper", "§a", 21, altDay = true),
    SIDE_DISH("Side Dish", "§6§l", -1),
    HITMAN("Hitman", "§c", -1),
    BOUGHT("Bought", "§a", -1),
    BOUGHT_ABIPHONE("✆ Bought", "§a", -1),
    CHOCOLATE_SHOP_MILESTONE("Shop Milestone", "§6§l", -1),
    CHOCOLATE_FACTORY_MILESTONE("Chocolate Milestone", "§6§l", -1),
    STRAY("Stray", "§a", -1)
    ;

    val isResetting get() = resettingEntries.contains(this)
    val formattedName get() = "${if (isClaimed()) "§7§m" else mealColor}$mealName:$mealColor"
    val coloredName get() = "$mealColor$mealName"

    val timeUntil: Duration get() = nextSpawn.timeUntil()
    private val nextSpawn: SimpleTimeMark get() = nextSpawnCache[this]?.takeIf { !it.isInPast() }
        ?: calculateNextSpawn()
    val lastSpawn: SimpleTimeMark get() = (nextSpawn - 40.minutes).takeIfHoppity() ?: SimpleTimeMark.farPast()

    private fun SimpleTimeMark.takeIfHoppity() = takeIf { it.toSkyBlockTime().getSeason() == SkyblockSeason.SPRING }

    private fun spawnsToday(): Boolean {
        val sbTimeNow = SkyBlockTime.now()
        return altDay == sbTimeNow.isAlternateDay()
    }

    fun spawnedToday(): Boolean {
        val sbTimeNow = SkyBlockTime.now()
        return altDay == sbTimeNow.isAlternateDay() && sbTimeNow.hour >= resetsAt
    }

    fun alreadyResetToday(): Boolean {
        val sbTimeNow = SkyBlockTime.now()
        return lastReset.day == sbTimeNow.day && lastReset.month == sbTimeNow.month
    }

    private fun calculateNextSpawn(): SimpleTimeMark {
        if (resetsAt == -1) return SimpleTimeMark.farFuture()
        val sbTimeNow = SkyBlockTime.now()
        val isEggDayToday = spawnsToday()

        val daysToAdd = when {
            isEggDayToday && sbTimeNow.hour < resetsAt -> 0
            isEggDayToday && sbTimeNow.hour >= resetsAt -> 2
            else -> 1
        }

        return sbTimeNow.copy(
            day = sbTimeNow.day + daysToAdd,
            hour = resetsAt,
            minute = 0,
            second = 0
        ).asTimeMark().also {
            nextSpawnCache[this] = it
        }
    }

    fun markClaimed(mark: SimpleTimeMark? = null) {
        claimed = true
        mark?.let { profileStorage?.mealLastFound?.set(this, it) }
    }

    fun markSpawned() {
        claimed = false
    }

    fun isClaimed() = claimed || hasNotFirstSpawnedYet()

    fun hasRemainingSpawns(): Boolean {
        val hoppityEndMark = HoppityAPI.getEventEndMark() ?: return false
        // If it's before the last two days of the event, we can assume there are more spawns
        if (hoppityEndMark.toMillis() > SkyBlockTime.SKYBLOCK_DAY_MILLIS * 2) return true
        // Otherwise we have to check if the next spawn is after the end of the event
        return timeUntil < hoppityEndMark.timeUntil()
    }

    private fun hasNotFirstSpawnedYet(): Boolean {
        val now = SkyBlockTime.now()
        if (now.month > 1 || (altDay && now.day > 2) || (!altDay && now.day > 1)) return false
        return (altDay && now.day < 2) || now.hour < resetsAt
    }

    @SkyHanniModule
    companion object {
        private val profileStorage get() = ProfileStorageData.profileSpecific?.chocolateFactory
        private val nextSpawnCache = CollectionUtils.ObservableMap<HoppityEggType, SimpleTimeMark>(
            postUpdate = { key, _ ->
                profileStorage?.mealLastSpawn?.set(key, key.lastSpawn)
            }
        )
        val resettingEntries = entries.filter { it.resetsAt != -1 }
        val sortedResettingEntries = resettingEntries.sortedBy { it.resetsAt }

        fun markAllFound() = resettingEntries.forEach { it.markClaimed() }
        fun anyEggsUnclaimed(): Boolean = resettingEntries.any { !it.claimed }
        fun allEggsUnclaimed(): Boolean = resettingEntries.all { !it.claimed }

        @HandleEvent
        fun onEggSpawned(event: EggSpawnedEvent) {
            event.eggType.markSpawned()
            event.eggType.lastReset = SkyBlockTime.now()
        }

        internal fun Matcher.getEggType(event: LorenzChatEvent): HoppityEggType =
            entries.find { it.mealName == group("meal") } ?: run {
                ErrorManager.skyHanniError(
                    "Unknown meal: ${group("meal")}",
                    "message" to event.message,
                )
            }
    }
}
