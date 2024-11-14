package at.hannibal2.skyhanni.features.gui.customscoreboard.events

import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getSbLines
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.features.rift.area.stillgorechateau.RiftBloodEffigies
import at.hannibal2.skyhanni.utils.RegexUtils.allMatches

// scoreboard
// scoreboard update event
object ScoreboardEventRift : ScoreboardEvent() {

    override fun getDisplay() = elementPatterns.allMatches(getSbLines())

    override val configLine = "§7(All Rift Lines)"

    override val elementPatterns = listOf(
        RiftBloodEffigies.heartsPattern,
        ScoreboardPattern.riftHotdogTitlePattern,
        ScoreboardPattern.timeLeftPattern,
        ScoreboardPattern.riftHotdogEatenPattern,
        ScoreboardPattern.riftAveikxPattern,
        ScoreboardPattern.riftHayEatenPattern,
        ScoreboardPattern.cluesPattern,
        ScoreboardPattern.barryProtestorsQuestlinePattern,
        ScoreboardPattern.barryProtestorsHandledPattern,
    )

    override fun showIsland() = RiftAPI.inRift()
}
