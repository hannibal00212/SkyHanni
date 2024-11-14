package at.hannibal2.skyhanni.features.gui.customscoreboard.events

import at.hannibal2.skyhanni.data.MiningAPI
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getSbLines
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardLine.Companion.align
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern
import at.hannibal2.skyhanni.utils.CollectionUtils.addNotNull
import at.hannibal2.skyhanni.utils.RegexUtils.allMatches
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatches
import at.hannibal2.skyhanni.utils.RenderUtils.HorizontalAlignment
import java.util.regex.Pattern

// scoreboard
// scoreboard update event
object ScoreboardEventMining : ScoreboardEvent() {
    // TODO: Rethink this one
    override fun getDisplay() = buildList {
        // Wind
        val (compassTitle, compassArrow) = ScoreboardPattern.windCompassPattern.firstMatches(getSbLines()) to
            ScoreboardPattern.windCompassArrowPattern.firstMatches(getSbLines())
        if (compassTitle != null && compassArrow != null) {
            add(compassTitle)
            add(compassArrow align HorizontalAlignment.CENTER)
        }

        // Better Together
        ScoreboardPattern.nearbyPlayersPattern.firstMatches(getSbLines())?.let {
            add("§dBetter Together")
            add(" $it")
        }

        // Zone Events
        val zoneEvent = ScoreboardPattern.miningEventPattern.firstMatches(getSbLines())
        zoneEvent?.let { eventTitle ->
            add(eventTitle.removePrefix("Event: "))
            ScoreboardPattern.miningEventZonePattern.firstMatches(getSbLines())?.let { zone ->
                add("in ${zone.removePrefix("Zone: ")}")
            }
        }

        // Mithril Gourmand
        addAll(
            listOf(
                ScoreboardPattern.mithrilRemainingPattern,
                ScoreboardPattern.mithrilYourMithrilPattern,
            ).allMatches(getSbLines()),
        )

        // Raffle
        addAll(
            listOf(
                ScoreboardPattern.raffleTicketsPattern,
                ScoreboardPattern.rafflePoolPattern,
            ).allMatches(getSbLines()),
        )

        // Raid
        addAll(
            listOf(
                ScoreboardPattern.yourGoblinKillsPattern,
                ScoreboardPattern.remainingGoblinPattern,
            ).allMatches(getSbLines()),
        )

        // Fortunate Freezing
        addNotNull(ScoreboardPattern.fortunateFreezingBonusPattern.firstMatches(getSbLines()))

        // Fossil Dust
        addNotNull(ScoreboardPattern.fossilDustPattern.firstMatches(getSbLines()))
    }

    override val configLine = "§7(All Mining Event Lines)"

    override val elementPatterns = listOf(
        ScoreboardPattern.windCompassPattern,
        ScoreboardPattern.windCompassArrowPattern,
        ScoreboardPattern.nearbyPlayersPattern,
        ScoreboardPattern.miningEventPattern,
        ScoreboardPattern.miningEventZonePattern,
        ScoreboardPattern.mithrilRemainingPattern,
        ScoreboardPattern.mithrilYourMithrilPattern,
        ScoreboardPattern.raffleTicketsPattern,
        ScoreboardPattern.rafflePoolPattern,
        ScoreboardPattern.yourGoblinKillsPattern,
        ScoreboardPattern.remainingGoblinPattern,
        ScoreboardPattern.fortunateFreezingBonusPattern,
        ScoreboardPattern.fossilDustPattern,
    )

    override fun showIsland() = MiningAPI.inAdvancedMiningIsland()
}
