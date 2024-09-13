package at.hannibal2.skyhanni.features.mining.glacitemineshaft

import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.asInternalName

enum class CorpseType(val displayName: String, private val keyName: String? = null) {
    LAPIS("§9Lapis"),
    TUNGSTEN("§7Tungsten", "TUNGSTEN_KEY"),
    UMBER("§6Umber", "UMBER_KEY"),
    VANGUARD("§fVanguard", "SKELETON_KEY"),
    ;

    val key by lazy { keyName?.asInternalName() }

    override fun toString(): String = displayName
}
