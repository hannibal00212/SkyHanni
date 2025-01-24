package at.hannibal2.skyhanni.features.gui.customscoreboard.events

import at.hannibal2.skyhanni.data.MiningApi
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getSBLines
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardLine.Companion.align
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern
import at.hannibal2.skyhanni.utils.CollectionUtils.addNotNull
import at.hannibal2.skyhanni.utils.RegexUtils.allMatches
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatches
import at.hannibal2.skyhanni.utils.RenderUtils.HorizontalAlignment

// scoreboard
// scoreboard update event
object ScoreboardEventMining : ScoreboardEvent() {
    // TODO: Rethink this one
    override fun getDisplay() = buildList {
        // Wind
        val (compassTitle, compassArrow) = ScoreboardPattern.windCompassPattern.firstMatches(getSBLines()) to
            ScoreboardPattern.windCompassArrowPattern.firstMatches(getSBLines())
        if (compassTitle != null && compassArrow != null) {
            add(compassTitle)
            add(compassArrow align HorizontalAlignment.CENTER)
        }

        // Better Together
        ScoreboardPattern.nearbyPlayersPattern.firstMatches(getSBLines())?.let {
            add("§dBetter Together")
            add(" $it")
        }

        // Zone Events
        val zoneEvent = ScoreboardPattern.miningEventPattern.firstMatches(getSBLines())
        zoneEvent?.let { eventTitle ->
            add(eventTitle.removePrefix("Event: "))
            ScoreboardPattern.miningEventZonePattern.firstMatches(getSBLines())?.let { zone ->
                add("in ${zone.removePrefix("Zone: ")}")
            }
        }

        // Mithril Gourmand
        addAll(
            listOf(
                ScoreboardPattern.mithrilRemainingPattern,
                ScoreboardPattern.mithrilYourMithrilPattern,
            ).allMatches(getSBLines()),
        )

        // Raffle
        addAll(
            listOf(
                ScoreboardPattern.raffleTicketsPattern,
                ScoreboardPattern.rafflePoolPattern,
            ).allMatches(getSBLines()),
        )

        // Raid
        addAll(
            listOf(
                ScoreboardPattern.yourGoblinKillsPattern,
                ScoreboardPattern.remainingGoblinPattern,
            ).allMatches(getSBLines()),
        )

        // Fortunate Freezing
        addNotNull(ScoreboardPattern.fortunateFreezingBonusPattern.firstMatches(getSBLines()))

        // Fossil Dust
        addNotNull(ScoreboardPattern.fossilDustPattern.firstMatches(getSBLines()))
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
        ScoreboardPattern.raffleUselessPattern,
        ScoreboardPattern.mithrilUselessPattern,
        ScoreboardPattern.goblinUselessPattern,
        ScoreboardPattern.mineshaftNotStartedPattern,

    )

    override fun showIsland() = MiningApi.inAdvancedMiningIsland()
}
