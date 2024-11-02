package at.hannibal2.skyhanni.utils

data class CachedItemData(
    // -1 = not loaded
    var petCandies: Int? = -1,

    // "" = not loaded
    var heldItem: String? = "",

    // -1 = not loaded
    var sackInASack: Int? = -1,

    // null = not loaded
    var riftTransferable: Boolean? = null,

    // null = not loaded
    var riftExportable: Boolean? = null,

    var itemRarityLastCheck: SimpleTimeMark = SimpleTimeMark.farPast(),

    // null = not loaded
    var itemRarity: LorenzRarity? = null,

    var itemCategory: ItemCategory? = null,

    var lastInternalName: NEUInternalName? = null,

    var lastInternalNameFetchTime: SimpleTimeMark = SimpleTimeMark.farPast(),
) {
    /**
     * Delegate constructor to avoid calling a function with default arguments from java.
     * We can't call the generated no args constructors (or rather we cannot generate that constructor), because inline
     * classes are not part of the java-kotlin ABI that is super well supported (especially with default arguments).
     */
    @Suppress("ForbiddenVoid", "UnusedPrivateProperty")
    constructor(void: Void?) : this()
}
