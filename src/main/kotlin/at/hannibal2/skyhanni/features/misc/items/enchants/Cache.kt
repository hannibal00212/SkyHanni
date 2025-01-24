package at.hannibal2.skyhanni.features.misc.items.enchants

class Cache {
    var cachedLoreBefore: List<String> = listOf()
    var cachedLoreAfter: List<String> = listOf()

    // So tooltip gets changed on the same item if the config was changed in the interim
    var configChanged = false

    fun updateBefore(loreBeforeModification: List<String>) {
        cachedLoreBefore = loreBeforeModification.toList()
    }

    fun updateAfter(loreAfterModification: List<String>) {
        cachedLoreAfter = loreAfterModification.toList()
        configChanged = false
    }

    fun isCached(loreBeforeModification: List<String>): Boolean = !configChanged && loreBeforeModification == cachedLoreBefore
}
