package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

abstract class ScoreboardElement {
    abstract fun getDisplay(): List<Any>
    open fun showWhen(): Boolean = true
    abstract val configLine: String

    open fun showIsland(): Boolean = true

    // TODO: Add Hover and Clickable Feedback to Lines
    // Suggestion: https://discord.com/channels/997079228510117908/1226508204762992733
}
