package at.hannibal2.skyhanni.features.event.hoppity

import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.asTimeMark
import at.hannibal2.skyhanni.utils.SkyBlockTime
import java.util.regex.Matcher
import kotlin.time.Duration

enum class HoppityEggType(
    val mealName: String,
    private val mealColor: String,
    val resetsAt: Int,
    var lastResetDay: Int = -1,
    private var claimed: Boolean = false,
) {
    BREAKFAST("Breakfast", "§6", 7),
    LUNCH("Lunch", "§9", 14),
    DINNER("Dinner", "§a", 21),
    BRUNCH("Brunch", "§6", -1),
    DEJEUNER("Déjeuner", "§9", -1),
    SUPPER("Supper", "§a", -1),
    SIDE_DISH("Side Dish", "§6§l", -1),
    BOUGHT("Bought", "§a", -1),
    CHOCOLATE_SHOP_MILESTONE("Shop Milestone", "§6", -1),
    CHOCOLATE_FACTORY_MILESTONE("Chocolate Milestone", "§6", -1),
    STRAY("Stray Rabbit", "§a", -1)
    ;

    fun timeUntil(): Duration {
        if (resetsAt == -1) return Duration.INFINITE
        val now = SkyBlockTime.now()
        if (now.hour >= resetsAt) {
            return now.copy(day = now.day + 1, hour = resetsAt, minute = 0, second = 0)
                .asTimeMark().timeUntil()
        }
        return now.copy(hour = resetsAt, minute = 0, second = 0).asTimeMark().timeUntil()
    }

    fun markClaimed() {
        claimed = true
    }

    fun markSpawned() {
        claimed = false
    }

    fun isClaimed() = claimed
    val isResetting get() = resettingEntries.contains(this)
    val formattedName get() = "${if (isClaimed()) "§7§m" else mealColor}$mealName:$mealColor"
    val coloredName get() = "$mealColor$mealName"

    companion object {
        val resettingEntries = entries.filter { it.resetsAt != -1 }

        fun allFound() = resettingEntries.forEach { it.markClaimed() }

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

            for (eggType in resettingEntries) {
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

        fun eggsRemaining(): Boolean {
            return resettingEntries.any { !it.claimed }
        }

        fun allEggsRemaining(): Boolean {
            return resettingEntries.all { !it.claimed }
        }
    }
}
