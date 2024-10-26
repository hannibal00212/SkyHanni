package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.informationFilteringConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils.getElementsFromAny
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardLine

abstract class ScoreboardElement {
    /**
     * Must be specified as one of the following:
     * - `String`
     * - `List<String>`
     * - `ScoreboardLine` (`String align HorizontalAlignment`)
     * - `List<ScoreboardLine>`
     *
     * `null` values will be treated as empty lines/lists.
     */
    protected abstract fun getDisplay(): Any?
    open fun showWhen(): Boolean = true
    abstract val configLine: String

    open fun showIsland(): Boolean = true

    // TODO: Add Hover and Clickable Feedback to Lines
    //  Suggestion: https://discord.com/channels/997079228510117908/1226508204762992733


    open fun getLines(): List<ScoreboardLine> = if (isVisible()) getElementsFromAny(getDisplay()) else listOf()

    private fun isVisible(): Boolean {
        if (!informationFilteringConfig.hideIrrelevantLines) return true
        return showWhen()
    }
}
