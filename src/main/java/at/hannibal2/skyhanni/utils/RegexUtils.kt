package at.hannibal2.skyhanni.utils

import java.util.regex.Matcher
import java.util.regex.Pattern

object RegexUtils {
    inline fun <T> Pattern.matchMatcher(text: String, consumer: Matcher.() -> T) =
        matcher(text).let { if (it.matches()) consumer(it) else null }

    inline fun <T> Pattern.findMatcher(text: String, consumer: Matcher.() -> T) =
        matcher(text).let { if (it.find()) consumer(it) else null }

    @Deprecated("", ReplaceWith("pattern.firstMatcher(this) { consumer() }"))
    inline fun <T> Sequence<String>.matchFirst(pattern: Pattern, consumer: Matcher.() -> T): T? =
        pattern.firstMatcher(this, consumer)

    inline fun <T> Pattern.firstMatcher(sequence: Sequence<String>, consumer: Matcher.() -> T): T? {
        for (line in sequence) {
            matcher(line).let { if (it.matches()) return consumer(it) }
        }
        return null
    }

    inline fun <T> Pattern.firstMatcherWithIndex(sequence: Sequence<String>, consumer: Matcher.(Int) -> T): T? {
        for ((index, line) in sequence.withIndex()) {
            matcher(line).let { if (it.matches()) return consumer(it, index) }
        }
        return null
    }

    @Deprecated("", ReplaceWith("pattern.firstMatcher(this) { consumer() }"))
    inline fun <T> List<String>.matchFirst(pattern: Pattern, consumer: Matcher.() -> T): T? = pattern.firstMatcher(this, consumer)

    inline fun <T> Pattern.firstMatcher(list: List<String>, consumer: Matcher.() -> T): T? = firstMatcher(list.asSequence(), consumer)

    inline fun <T> Pattern.firstMatcherWithIndex(list: List<String>, consumer: Matcher.(Int) -> T): T? =
        firstMatcherWithIndex(list.asSequence(), consumer)

    @Deprecated("", ReplaceWith("pattern.matchAll(this) { consumer() }"))
    inline fun <T> List<String>.matchAll(pattern: Pattern, consumer: Matcher.() -> T): T? = pattern.matchAll(this, consumer)

    inline fun <T> Pattern.matchAll(list: List<String>, consumer: Matcher.() -> T): T? {
        for (line in list) {
            matcher(line).let { if (it.find()) consumer(it) }
        }
        return null
    }

    inline fun <T> List<Pattern>.matchMatchers(text: String, consumer: Matcher.() -> T): T? {
        for (pattern in iterator()) {
            pattern.matchMatcher<T>(text) {
                return consumer()
            }
        }
        return null
    }

    fun List<Pattern>.allMatches(list: List<String>): List<String> = list.filter { line -> any { it.matches(line) } }
    fun List<Pattern>.anyMatches(list: List<String>?): Boolean = list?.any { line -> any { it.matches(line) } } ?: false
    fun List<Pattern>.anyMatches(string: String): Boolean = any { it.matches(string) }

    fun Pattern.matches(string: String?): Boolean = string?.let { matcher(it).matches() } ?: false
    fun Pattern.find(string: String?) = string?.let { matcher(it).find() } ?: false

    fun Pattern.anyMatches(list: List<String>?): Boolean = list?.any { matches(it) } ?: false
    fun Pattern.anyMatches(list: Sequence<String>?): Boolean = anyMatches(list?.toList())

    fun Pattern.matchGroup(text: String, groupName: String): String? = matchMatcher(text) { groupOrNull(groupName) }

    fun Pattern.matchGroups(text: String, vararg groups: String): List<String?>? =
        matchMatcher(text) { groups.toList().map { groupOrNull(it) } }

    fun Pattern.firstMatches(list: List<String>): String? = list.firstOrNull { matches(it) }
    fun Pattern.allMatches(list: List<String>): List<String> = list.filter { matches(it) }

    /**
     * Get the group, otherwise, return null
     * @param groupName The group name in the pattern
     */
    fun Matcher.groupOrNull(groupName: String): String? = runCatching { group(groupName) }.getOrNull()

    fun Matcher.groupOrEmpty(groupName: String): String = groupOrNull(groupName).orEmpty()

    fun Matcher.hasGroup(groupName: String): Boolean = groupOrNull(groupName) != null

    fun Pattern.indexOfFirstMatch(list: List<String>): Int? {
        for ((index, line) in list.withIndex()) {
            matcher(line).let { if (it.matches()) return index }
        }
        return null
    }

    fun Iterable<Pattern>.matches(string: String?): Boolean {
        if (string == null) return false
        return this.any { it.matches(string) }
    }

    /**
     * Returns a list of all occurrences of a pattern within the [input] string.
     */
    fun Pattern.findAll(input: String): List<String> {
        val matcher = matcher(input)

        return buildList {
            while (matcher.find()) {
                add(matcher.group())
            }
        }
    }
}
