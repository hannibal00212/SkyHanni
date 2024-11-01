package at.hannibal2.skyhanni.features.commands.suggestions

interface SuggestionEntry {

    val suggestions: List<String>

    fun isEntryFor(argument: String): Boolean {
        return this.suggestions.any { it.equals(argument, ignoreCase = true) }
    }
}

data class LiteralSuggestionEntry(override val suggestions: List<String>) : SuggestionEntry

data class CompositeSuggestionEntry(val entries: List<SuggestionEntry>) : SuggestionEntry {
    override val suggestions: List<String> get() = entries.flatMap { it.suggestions }
    override fun isEntryFor(argument: String): Boolean = entries.any { it.isEntryFor(argument) }
}

data class ParentSuggestionEntry(val parent: SuggestionEntry, val children: List<SuggestionEntry>) : SuggestionEntry {
    override val suggestions: List<String> get() = parent.suggestions
    override fun isEntryFor(argument: String): Boolean = parent.isEntryFor(argument)
}

data class LazySuggestionEntry(val supplier: MutableList<String>.() -> Unit) : SuggestionEntry {
    override val suggestions: List<String> get() = mutableListOf<String>().apply { supplier() }
}

data class ConditionalSuggestionEntry(val condition: () -> Boolean, val entry: SuggestionEntry) : SuggestionEntry {
    override val suggestions: List<String> get() = if (condition()) entry.suggestions else emptyList()
    override fun isEntryFor(argument: String): Boolean = entry.isEntryFor(argument)
}
