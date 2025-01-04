package at.hannibal2.skyhanni.config

import io.github.notenoughupdates.moulconfig.processor.ConfigStructureReader

class BritishSpellingWrapper(wrappedProcessor: ConfigStructureReader) : RenamingWrapper(wrappedProcessor) {
    override fun mapText(original: String): String {
        return original
            .replaceSpelling("(?i)(col)(o)(r)".toRegex(), "u")
            .replaceSpelling("(?i)(arm)(o)(r)".toRegex(), "u")
    }

    private fun String.replaceSpelling(regex: Regex, replacement: String): String {
        return this.replace(regex) {
            it.groupValues[1] +
                it.groupValues[2] +
                (if (it.groupValues[2].single().isUpperCase()) replacement.uppercase() else replacement) +
                it.groupValues[3]
        }
    }
}
