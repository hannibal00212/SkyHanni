package at.hannibal2.skyhanni.features.garden.fortuneguide

// TODO replace with ReforgeAPI
enum class FarmingReforge(
    val reforgeName: String,
    val reforgeItem: String,
    val common: Int,
    val uncommon: Int,
    val rare: Int,
    val epic: Int,
    val legendary: Int,
    val mythic: Int,
) { // if reforge item is an empty string it means it will never be called, just for upgrading and recomb stats
    BLESSED("Blessed", "BLESSED_FRUIT", 5, 7, 9, 13, 16, 20),
    BOUNTIFUL("Bountiful", "GOLDEN_BALL", 1, 2, 3, 5, 7, 10),
    BLOOMING("Blooming", "FLOWERING_BOUQUET", 1, 2, 3, 4, 5, 6),
    ROOTED("Rooted", "BURROWING_SPORES", 6, 9, 12, 15, 18, 21),
    BUSTLING("Bustling", "SKYMART_BROCHURE", 1, 2, 4, 6, 8, 10),
    MOSSY("Mossy", "OVERGROWN_GRASS", 5, 10, 15, 20, 25, 30),
    ROBUST("Robust", "", 2, 3, 4, 6, 8, 10),
    EARTHLY("Earthly", "LARGE_WALNUT", 1, 4, 6, 8, 10, 12),
    GREEN_THUMB("Green Thumb", "", 1, 2, 3, 4, 5, 6)
}

operator fun FarmingReforge.get(index: Int, current: Double = 0.0): Double? {
    return when (index) {
        0 -> common - current
        1 -> uncommon - current
        2 -> rare - current
        3 -> epic - current
        4 -> legendary - current
        5 -> mythic - current
        else -> null
    }
}
