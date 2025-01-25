package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.UtilsPatterns.seasonPattern
import kotlin.time.Duration.Companion.seconds

enum class SkyblockSeason(
    val season: String,
    val abbreviatedPerk: String,
    val perk: String,
) {

    SPRING("§dSpring", "§6+25☘", "§7Gain §6+25☘ Farming Fortune§7."),
    SUMMER("§6Summer", "§3+20☯", "§7Gain §3+20☯ Farming Wisdom§7."),
    AUTUMN("§eAutumn", "§a15%+§4ൠ", "§4Pests §7spawn §a15% §7more often."),
    WINTER("§9Winter", "§a5%+§cC", "§7Visitors give §a5% §7more §cCopper."),
    ;

    enum class SkyblockSeasonModifier(
        val str: String,
    ) {
        EARLY("Early"),
        NONE(""),
        LATE("Late"),
        ;

        override fun toString(): String = str
    }

    fun isSeason(): Boolean = currentSeason == this
    fun getPerk(abbreviate: Boolean): String = if (abbreviate) abbreviatedPerk else perk
    fun getSeason(abbreviate: Boolean): String = if (abbreviate) season.take(4) else season
    fun getDisplayMonth(modifier: SkyblockSeasonModifier? = null): Int = getMonth(modifier) + 1
    fun getMonth(modifier: SkyblockSeasonModifier? = null): Int =
        when (this) {
            SPRING -> 1
            SUMMER -> 4
            AUTUMN -> 7
            WINTER -> 10
        }.minus(
            when (modifier) {
                SkyblockSeasonModifier.EARLY -> 1
                SkyblockSeasonModifier.LATE -> -1
                else -> 0
            }
        )

    companion object {
        val currentSeason by RecalculatingValue(1.seconds) {
            getSeasonByName(SkyBlockTime.now().monthName)
        }

        private fun getSeasonByName(name: String): SkyblockSeason? =
            seasonPattern.matchMatcher(name) { entries.find { it.season.endsWith(group("season")) } }
    }
}
