package at.hannibal2.skyhanni.features.event.hoppity

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.features.event.hoppity.HoppityAPI.isAlternateDay
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.CollectionUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.enumMapOf
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.asTimeMark
import at.hannibal2.skyhanni.utils.SkyBlockTime
import java.util.regex.Matcher
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

enum class HoppityEggType(
    val mealName: String,
    private val mealColor: String,
    val resetsAt: Int,
    var lastResetDay: Int = -1,
    private var claimed: Boolean = false,
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

    private val nextSpawnCache = CollectionUtils.ObservableMutableMap<HoppityEggType, SimpleTimeMark>(
        enumMapOf(),
        onUpdate = { type, markOrNull ->
            val mark = markOrNull ?: return@ObservableMutableMap
            val profileStorage = profileStorage ?: return@ObservableMutableMap
            profileStorage.mealNextSpawn[type] = mark
        }
    )

    fun timeUntil(): Duration {
        if (resetsAt == -1) return Duration.INFINITE
        nextSpawnCache[this]?.takeIf { it.isInFuture() }?.let { return it.timeUntil() }

        val now = SkyBlockTime.now()
        val isEggDayToday = altDay == now.isAlternateDay()

        val daysToAdd = when {
            isEggDayToday && now.hour < resetsAt -> 0
            isEggDayToday && now.hour >= resetsAt -> 2
            else -> 1
        }

        val nextSpawn = now.copy(day = now.day + daysToAdd, hour = resetsAt, minute = 0, second = 0).asTimeMark()
        nextSpawnCache[this] = nextSpawn
        return nextSpawn.timeUntil()
    }

    fun markClaimed(mark: SimpleTimeMark? = null, setMark: Boolean = true) {
        claimed = true
        if (!setMark) return
        val profileStorage = profileStorage ?: return
        profileStorage.mealLastFound[this] = mark ?: SimpleTimeMark.now()
    }

    fun markSpawned() {
        claimed = false
        nextSpawnCache.remove(this)
    }

    private fun hasNotFirstSpawnedYet(): Boolean {
        val now = SkyBlockTime.now()
        if (now.month > 1 || (altDay && now.day > 2) || (!altDay && now.day > 1)) return false
        return (altDay && now.day < 2) || now.hour < resetsAt
    }

    fun hasRemainingSpawns(): Boolean {
        val hoppityEndMark = HoppityAPI.getEventEndMark() ?: return false
        // If it's before the last two days of the event, we can assume there are more spawns
        if (hoppityEndMark.toMillis() > SkyBlockTime.SKYBLOCK_DAY_MILLIS * 2) return true
        // Otherwise we have to check if the next spawn is after the end of the event
        return timeUntil() < hoppityEndMark.timeUntil()
    }

    fun isClaimed() = claimed || hasNotFirstSpawnedYet()
    val isResetting get() = resettingEntries.contains(this)
    val formattedName get() = "${if (isClaimed()) "§7§m" else mealColor}$mealName:$mealColor"
    val coloredName get() = "$mealColor$mealName"

    @SkyHanniModule
    companion object {
        private val profileStorage get() = ProfileStorageData.profileSpecific?.chocolateFactory

        @HandleEvent
        fun onProfileJoin(event: ProfileJoinEvent) {
            val spawnMap = profileStorage?.mealNextSpawn ?: return
            val findMap = profileStorage?.mealLastFound ?: return
            for ((meal, mark) in spawnMap) {
                val lastFound = findMap[meal] ?: continue
                if (mark.isInPast()) meal.markSpawned()
                else if (lastFound.passedSince() <= 40.minutes) meal.markClaimed(lastFound)
            }
        }

        val resettingEntries = entries.filter { it.resetsAt != -1 }
        val sortedResettingEntries = resettingEntries.sortedBy { it.resetsAt }

        fun allFound() = resettingEntries.forEach { it.markClaimed(setMark = false) }

        private fun getMealByName(mealName: String) = entries.find { it.mealName == mealName }

        internal fun Matcher.getEggType(event: LorenzChatEvent): HoppityEggType =
            HoppityEggType.getMealByName(group("meal")) ?: run {
                ErrorManager.skyHanniError(
                    "Unknown meal: ${group("meal")}",
                    "message" to event.message,
                )
            }

        fun checkClaimed() {
            val currentSbTime = SkyBlockTime.now()
            val currentSbDay = currentSbTime.day
            val currentSbHour = currentSbTime.hour
            val isAltDay = currentSbTime.isAlternateDay()

            for (eggType in resettingEntries.filter { it.altDay == isAltDay }) {
                if (currentSbHour < eggType.resetsAt || eggType.lastResetDay == currentSbDay) continue
                eggType.markSpawned()
                eggType.lastResetDay = currentSbDay
                if (HoppityEggLocator.currentEggType == eggType) {
                    HoppityEggLocator.currentEggType = null
                    HoppityEggLocator.currentEggNote = null
                    HoppityEggLocator.sharedEggLocation = null
                }
            }
        }

        fun anyEggsUnclaimed(): Boolean {
            return resettingEntries.any { !it.claimed }
        }

        fun allEggsUnclaimed(): Boolean {
            return resettingEntries.all { !it.claimed }
        }
    }
}
