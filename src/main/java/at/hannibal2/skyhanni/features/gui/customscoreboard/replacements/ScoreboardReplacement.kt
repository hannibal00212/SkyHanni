package at.hannibal2.skyhanni.features.gui.customscoreboard.replacements

abstract class ScoreboardReplacement {
    abstract val trigger: String
    abstract val name: String
    abstract fun replacement(): String
}
