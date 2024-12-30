package at.hannibal2.skyhanni.features.gui.customscoreboard

import at.hannibal2.skyhanni.data.MiningAPI
import at.hannibal2.skyhanni.data.PurseAPI
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.chunkedConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.informationFilteringConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.formatNumber
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getBank
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getBits
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getBitsAvailable
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getBitsLine
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getCopper
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getGems
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getHeat
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getMotes
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getNorthStars
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementBank
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementBits
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementCold
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementCopper
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementGems
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementHeat
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementMotes
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementNorthStars
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementPurse

private val hideEmptyLines get() = informationFilteringConfig.hideEmptyLines

enum class ChunkedStatsLine(
    private val displayPair: () -> String,
    private val showWhen: () -> Boolean,
    private val showIsland: () -> Boolean,
    private val configLine: String,
) {
    PURSE(
        displayPair = { "§6${formatNumber(PurseAPI.currentPurse)}" },
        showWhen = { !(hideEmptyLines && PurseAPI.currentPurse.toInt() == 0) && ScoreboardElementPurse.showWhen() },
        showIsland = { ScoreboardElementPurse.showIsland() },
        configLine = "§6Purse",
    ),
    MOTES(
        displayPair = { "§b${getMotes()}" },
        showWhen = { !(hideEmptyLines && getMotes() == "0") && ScoreboardElementMotes.showWhen() },
        showIsland = { ScoreboardElementMotes.showIsland() },
        configLine = "§dMotes",
    ),
    BANK(
        displayPair = { "§6${getBank()}" },
        showWhen = { !(hideEmptyLines && (getBank() == "0" || getBank() == "0§7 / §60")) && ScoreboardElementBank.showWhen() },
        showIsland = { ScoreboardElementBank.showIsland() },
        configLine = "§6Bank",
    ),
    BITS(
        displayPair = { getBitsLine() },
        showWhen = { !(hideEmptyLines && getBits() == "0" && getBitsAvailable() == "0") && ScoreboardElementBits.showWhen() },
        showIsland = { ScoreboardElementBits.showIsland() },
        configLine = "§bBits",
    ),
    COPPER(
        displayPair = { "§c${getCopper()}" },
        showWhen = { !(hideEmptyLines && getCopper() == "0") && ScoreboardElementCopper.showWhen() },
        showIsland = { ScoreboardElementCopper.showIsland() },
        configLine = "§cCopper",
    ),
    GEMS(
        displayPair = { "§a${getGems()}" },
        showWhen = { !(hideEmptyLines && getGems() == "0") && ScoreboardElementGems.showWhen() },
        showIsland = { ScoreboardElementGems.showIsland() },
        configLine = "§aGems",
    ),
    HEAT(
        displayPair = { "§c${getHeat()}" },
        showWhen = { !(hideEmptyLines && getHeat() == "§c♨ 0") && ScoreboardElementHeat.showWhen() },
        showIsland = { ScoreboardElementHeat.showIsland() },
        configLine = "§cHeat",
    ),
    COLD(
        displayPair = { "§b${MiningAPI.cold}❄" },
        showWhen = { !(hideEmptyLines && MiningAPI.cold == 0) && ScoreboardElementCold.showWhen() },
        showIsland = { ScoreboardElementCold.showIsland() },
        configLine = "§bCold",
    ),
    NORTH_STARS(
        displayPair = { "§d${getNorthStars()}" },
        showWhen = { !(hideEmptyLines && getNorthStars() == "0") && ScoreboardElementNorthStars.showWhen() },
        showIsland = { ScoreboardElementNorthStars.showIsland() },
        configLine = "§dNorth Stars",
    ),
    ;

    override fun toString() = configLine

    companion object {

        private var currentIslandStats = listOf<ChunkedStatsLine>()

        fun getChunkedStats() = buildList {
            currentIslandStats.forEach { stat ->
                if (stat.showWhen()) {
                    add(stat.displayPair.invoke())
                }
            }
        }

        fun shouldShowChunkedStats() = currentIslandStats.any { it.showWhen() }

        fun showChunkedStatsIsland(): Boolean {
            currentIslandStats = chunkedConfig.chunkedStats.filter { it.showIsland() }
            return currentIslandStats.isNotEmpty()
        }
    }
}
