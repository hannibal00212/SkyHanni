package at.hannibal2.skyhanni.features.commands.suggestions

class SuggestionProvider {

    private val entry = mutableListOf<SuggestionEntry>()

    fun add(entry: SuggestionEntry) {
        this.entry.add(entry)
    }

    private fun List<SuggestionEntry>.getEntryForPath(path: List<String>): SuggestionEntry? {
        val entry = this.firstOrNull { it.isEntryFor(path.first()) }
        val remainingPath = path.drop(1)
        if (remainingPath.isNotEmpty()) {
            if (entry is ParentSuggestionEntry) {
                return entry.children.getEntryForPath(remainingPath)
            }
            return null
        } else if (entry is ParentSuggestionEntry) {
            return CompositeSuggestionEntry(entry.children)
        }
        return null
    }

    fun getSuggestions(command: String): List<String> {
        val arguments = command.lowercase().split(" ")
        val last = arguments.lastOrNull().orEmpty()
        val suggestions = mutableListOf<String>()
        if (arguments.size != 1) {
            entry.getEntryForPath(arguments.dropLast(1))?.suggestions?.let { suggestions.addAll(it) }
        } else {
            entry.forEach { suggestions.addAll(it.suggestions) }
        }
        return suggestions.filter { it.startsWith(last, ignoreCase = true) }
    }

    companion object {

        fun build(builder: Builder.() -> Unit): SuggestionProvider {
            val b = Builder()
            b.builder()
            return b.build()
        }
    }
}

class Builder {
    private val entries = mutableListOf<SuggestionEntry>()

    fun add(entry: SuggestionEntry) {
        entries.add(entry)
    }

    fun conditional(condition: () -> Boolean, builder: Builder.() -> Unit) {
        val childBuilder = Builder()
        childBuilder.builder()
        add(ConditionalSuggestionEntry(condition, CompositeSuggestionEntry(childBuilder.entries)))
    }

    fun literal(vararg literals: String) {
        add(LiteralSuggestionEntry(literals.toList()))
    }

    fun lazy(supplier: () -> List<String>) {
        add(LazySuggestionEntry { addAll(supplier()) })
    }

    fun group(vararg children: SuggestionEntry) {
        add(CompositeSuggestionEntry(children.toList()))
    }

    fun parent(vararg literals: String, children: Builder.() -> Unit) {
        val childBuilder = Builder()
        childBuilder.children()
        add(ParentSuggestionEntry(LiteralSuggestionEntry(literals.toList()), childBuilder.entries))
    }

    fun build(): SuggestionProvider {
        val provider = SuggestionProvider()
        entries.forEach { provider.add(it) }
        return provider
    }
}
