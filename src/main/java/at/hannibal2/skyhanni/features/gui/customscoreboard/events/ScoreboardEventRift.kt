package at.hannibal2.skyhanni.features.gui.customscoreboard.events

import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getSBLines
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern
import at.hannibal2.skyhanni.features.rift.RiftApi
import at.hannibal2.skyhanni.features.rift.area.stillgorechateau.RiftBloodEffigies
import at.hannibal2.skyhanni.utils.RegexUtils.allMatches

// scoreboard
// scoreboard update event
object ScoreboardEventRift : ScoreboardEvent() {

    private val importantPatterns = listOf(
        RiftBloodEffigies.heartsPattern,
        ScoreboardPattern.riftHotdogTitlePattern,
        ScoreboardPattern.timeLeftPattern,
        ScoreboardPattern.riftHotdogEatenPattern,
        ScoreboardPattern.riftAveikxPattern,
        ScoreboardPattern.riftHayEatenPattern,
        ScoreboardPattern.cluesPattern,
        ScoreboardPattern.barryProtestorsQuestlinePattern,
        ScoreboardPattern.barryProtestorsHandledPattern,
        ScoreboardPattern.timeSlicedPattern,
        ScoreboardPattern.bigDamagePattern,
    )

    override fun getDisplay() = importantPatterns.allMatches(getSBLines())

    override val configLine = "§7(All Rift Lines)"

    override val elementPatterns = importantPatterns + listOf(ScoreboardPattern.riftDimensionPattern)

    override fun showIsland() = RiftApi.inRift()
}
