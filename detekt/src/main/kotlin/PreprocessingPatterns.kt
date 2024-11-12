package at.hannibal2.skyhanni.detektrules

enum class PreprocessingPattern(val text: String) {
    IF("#if"),
    ELSE("#else"),
    ELSEIF("#elseif"),
    ENDIF("#endif"),
    DOLLAR_DOLLAR("$$"),
    ;

    val asComment: String
        get() = "//$text"

    companion object {
        fun String.containsPreprocessingPattern(): Boolean {
            return entries.any { this.startsWith(it.text) }
        }
    }
}
