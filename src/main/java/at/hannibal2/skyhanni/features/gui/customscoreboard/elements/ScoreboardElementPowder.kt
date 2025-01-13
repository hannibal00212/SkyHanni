package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.api.HotmAPI
import at.hannibal2.skyhanni.config.features.gui.customscoreboard.DisplayConfig.PowderDisplay
import at.hannibal2.skyhanni.data.MiningAPI
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.displayConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.informationFilteringConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.formatNumber

// internal
// 1s internal while on mining islands?
object ScoreboardElementPowder : ScoreboardElement() {
    override fun getDisplay() = buildList {
        val powderTypes = HotmAPI.PowderType.entries
        if (informationFilteringConfig.hideEmptyLines && powderTypes.all { it.total == 0L }) return@buildList

        add("§9§lPowder")

        val displayNumbersFirst = displayConfig.displayNumbersFirst

        for (type in powderTypes) {
            val name = type.displayName
            val color = type.color
            val current = formatNumber(type.current)
            val total = formatNumber(type.total)

            when (displayConfig.powderDisplay) {
                PowderDisplay.AVAILABLE -> {
                    add(" §7- ${if (displayNumbersFirst) "$color$current $name" else "§f$name: $color$current"}")
                }

                PowderDisplay.TOTAL -> {
                    add(" §7- ${if (displayNumbersFirst) "$color$total $name" else "§f$name: $color$total"}")
                }

                PowderDisplay.BOTH -> {
                    add(" §7- ${if (displayNumbersFirst) "$color$current/$total $name" else "§f$name: $color$current/$total"}")
                }

                null -> {}
            }
        }
    }

    override val configLine = "§9§lPowder\n §7- §fMithril: §254,646\n §7- §fGemstone: §d51,234\n §7- §fGlacite: §b86,574"

    override fun showIsland() = MiningAPI.inAdvancedMiningIsland()
}

// click: open /hotm
