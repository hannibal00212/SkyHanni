package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.api.HotmAPI
import at.hannibal2.skyhanni.config.features.gui.customscoreboard.DisplayConfig.PowderDisplay
import at.hannibal2.skyhanni.data.MiningAPI
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.displayConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.informationFilteringConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.formatNumber

// internal
// 1s internal while on mining islands?
object ScoreboardElementPowder : ScoreboardElement() {
    override fun getDisplay() = buildList {
        val powderTypes = HotmAPI.PowderType.entries
        if (informationFilteringConfig.hideEmptyLines && powderTypes.all { it.getTotal() == 0L }) return@buildList

        add("§9§lPowder")

        val displayNumbersFirst = displayConfig.displayNumbersFirst

        for (type in powderTypes) {
            val name = type.displayName
            val color = type.color
            val current = formatNumber(type.getCurrent())
            val total = formatNumber(type.getTotal())

            when (displayConfig.powderDisplay) {
                PowderDisplay.AVAILABLE -> {
                    add(" §7 - ${CustomScoreboardUtils.formatScoreboardNumberDisplayDisplay(name, current, color)}")
                }

                PowderDisplay.TOTAL -> {
                    add(" §7- ${CustomScoreboardUtils.formatScoreboardNumberDisplayDisplay(name, total, color)}")
                }

                PowderDisplay.BOTH -> {
                    add(" §7- ${CustomScoreboardUtils.formatScoreboardNumberDisplayDisplay(name, "$current/$total", color)}")
                }

                null -> {}
            }
        }
    }

    override val configLine = "§9§lPowder\n §7- §fMithril: §254,646\n §7- §fGemstone: §d51,234\n §7- §fGlacite: §b86,574"

    override fun showIsland() = MiningAPI.inAdvancedMiningIsland()
}

// click: open /hotm
