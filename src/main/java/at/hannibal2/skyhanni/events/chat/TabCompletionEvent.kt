package at.hannibal2.skyhanni.events.chat

import at.hannibal2.skyhanni.api.event.SkyHanniEvent

class TabCompletionEvent(
    val leftOfCursor: String,
    val fullText: String,
    val originalCompletions: List<String>,
) : SkyHanniEvent() {
    val lastWord = leftOfCursor.substringAfterLast(' ')
    val additionalSuggestions = mutableSetOf<String>()
    val suppressedSuggestions = mutableSetOf<String>()

    fun addSuggestion(suggestion: String) {
        if (suggestion.startsWith(lastWord, ignoreCase = true))
            additionalSuggestions.add(suggestion)
    }

    fun addSuggestions(suggestions: Iterable<String>) {
        suggestions.forEach(this::addSuggestion)
    }

    fun excludeAllDefault() {
        suppressedSuggestions.addAll(originalCompletions)
    }

    val command = if (leftOfCursor.startsWith("/"))
        leftOfCursor.substring(1).substringBefore(" ").lowercase()
    else ""

    fun isCommand(commandName: String): Boolean {
        return commandName.equals(command, ignoreCase = true)
    }

    fun intoSuggestionArray(): Array<String>? {
        if (additionalSuggestions.isEmpty() && suppressedSuggestions.isEmpty()) return null
        return (originalCompletions - suppressedSuggestions + additionalSuggestions).toTypedArray()
    }
}
