package at.hannibal2.skyhanni.features.gui.customscoreboard

import at.hannibal2.skyhanni.features.gui.customscoreboard.replacements.ScoreboardReplacementArea
import at.hannibal2.skyhanni.features.gui.customscoreboard.replacements.ScoreboardReplacementBits
import at.hannibal2.skyhanni.features.gui.customscoreboard.replacements.ScoreboardReplacementBitsAvailable
import at.hannibal2.skyhanni.features.gui.customscoreboard.replacements.ScoreboardReplacementDate
import at.hannibal2.skyhanni.features.gui.customscoreboard.replacements.ScoreboardReplacementIsland
import at.hannibal2.skyhanni.features.gui.customscoreboard.replacements.ScoreboardReplacementPitch
import at.hannibal2.skyhanni.features.gui.customscoreboard.replacements.ScoreboardReplacementPurse
import at.hannibal2.skyhanni.features.gui.customscoreboard.replacements.ScoreboardReplacementTime
import at.hannibal2.skyhanni.features.gui.customscoreboard.replacements.ScoreboardReplacementXCoord
import at.hannibal2.skyhanni.features.gui.customscoreboard.replacements.ScoreboardReplacementYCoord
import at.hannibal2.skyhanni.features.gui.customscoreboard.replacements.ScoreboardReplacementYaw
import at.hannibal2.skyhanni.features.gui.customscoreboard.replacements.ScoreboardReplacementYear
import at.hannibal2.skyhanni.features.gui.customscoreboard.replacements.ScoreboardReplacementZCoord

object CustomLines {

    internal val replacements = listOf(
        ScoreboardReplacementXCoord,
        ScoreboardReplacementYCoord,
        ScoreboardReplacementZCoord,
        ScoreboardReplacementYaw,
        ScoreboardReplacementPitch,
        ScoreboardReplacementPurse,
        ScoreboardReplacementBits,
        ScoreboardReplacementBitsAvailable,
        ScoreboardReplacementIsland,
        ScoreboardReplacementArea,
        ScoreboardReplacementDate,
        ScoreboardReplacementYear,
        ScoreboardReplacementTime,
    )

    internal fun String.handleCustomLine(): List<String> = this.replace("&&", "§").replaceWithReplacements()

    private fun String.replaceWithReplacements(): List<String> {
        var modifiedString = this
        replacements.forEach {
            modifiedString = modifiedString.replace(it.trigger, it.replacement())
        }
        return modifiedString.split("\\n")
    }

}
