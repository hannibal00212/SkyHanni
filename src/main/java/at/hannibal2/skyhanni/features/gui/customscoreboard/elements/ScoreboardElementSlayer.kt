package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.data.SlayerAPI
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.informationFilteringConfig

// internal
// scoreboard update event
object ScoreboardElementSlayer : ScoreboardElement() {
    override fun getDisplay() = buildList {
        if (!SlayerAPI.hasActiveSlayerQuest()) return@buildList
        add("Slayer Quest")
        add(SlayerAPI.latestSlayerCategory)
        add(SlayerAPI.latestSlayerProgress)
    }

    override fun showWhen() = if (informationFilteringConfig.hideIrrelevantLines) SlayerAPI.isInCorrectArea else true

    override val configLine = "Slayer Quest\n §7- §cVoidgloom Seraph III\n §7- §e12§7/§c120 §7Kills"
}
