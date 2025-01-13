package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.ScoreboardUpdateEvent
import at.hannibal2.skyhanni.events.currency.CurrencyChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.NumberUtil.formatLong
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.TimeUtils
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import java.time.Month

@SkyHanniModule
object WinterAPI {

    private val storage get() = ProfileStorageData.profileSpecific?.winter

    private val patternGroup = RepoPattern.group("winter")

    /**
     * REGEX-TEST: North Stars: §d1,539
     */
    val northstarsPattern by patternGroup.pattern(
        "northstars",
        "North Stars: §d(?<northstars>[\\w,]+).*",
    )

    var northStars: Long
        get() = storage?.northStars ?: 0
        private set(value) {
            storage?.northStars = value
        }

    fun inWorkshop() = IslandType.WINTER.isInIsland()

    fun isDecember() = TimeUtils.getCurrentLocalDate().month == Month.DECEMBER

    @HandleEvent(onlyOnIsland = IslandType.WINTER)
    fun onScoreboardUpdate(event: ScoreboardUpdateEvent) {
        northstarsPattern.firstMatcher(event.added) {
            val newNorthStars = group("northstars").formatLong()
            val difference = (newNorthStars - northStars).toInt()
            if (difference == 0) return
            northStars = newNorthStars
            CurrencyChangeEvent.NorthStars(difference, northStars).post()
        }
    }
}
