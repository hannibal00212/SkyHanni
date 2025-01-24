package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.data.jsonobjects.repo.MultiFilterJson
import at.hannibal2.skyhanni.utils.StringUtils.trimWhiteSpace

class MultiFilter {

    private val equals = mutableListOf<String>()
    private val startsWith = mutableListOf<String>()
    private val endsWith = mutableListOf<String>()
    private val contains = mutableListOf<String>()
    private val containsWord = mutableListOf<String>()

    fun load(data: MultiFilterJson) {
        equals.clear()
        startsWith.clear()
        endsWith.clear()
        contains.clear()
        containsWord.clear()

        fill(equals, data.equals)
        fill(startsWith, data.startsWith)
        fill(endsWith, data.endsWith)
        fill(contains, data.contains)
        fill(containsWord, data.containsWord)
    }

    private fun fill(list: MutableList<String>, data: List<String>?) {
        if (data == null) return
        list.addAll(data)
    }

    fun match(string: String): Boolean {
        return matchResult(string) != null
    }

    fun matchResult(string: String): String? {
        var result = equals.find { it == string }
        if (result != null) return result
        result = startsWith.find { string.startsWith(it) }
        if (result != null) return result
        result = endsWith.find { string.endsWith(it) }
        if (result != null) return result
        result = contains.find { string.contains(it) }
        if (result != null) return result
        result = containsWord.find { containsWord(string, it) }
        if (result != null) return result

        return null
    }

    private fun containsWord(message: String, word: String): Boolean {
        return message.split(" ").any { it.trimWhiteSpace() == word }
    }

    fun count(): Int {
        return equals.size + startsWith.size + endsWith.size + contains.size + containsWord.size
    }
}
