package at.hannibal2.skyhanni.features.misc.visualwords

import com.google.gson.annotations.Expose

data class VisualWord(
    @Expose var phrase: String,
    @Expose var replacement: String,
    @Expose var enabled: Boolean,
    @Expose private var caseSensitive: Boolean?,
) {

    fun isCaseSensitive() = caseSensitive ?: false
    fun setCaseSensitive(value: Boolean) {
        caseSensitive = value
    }
}
