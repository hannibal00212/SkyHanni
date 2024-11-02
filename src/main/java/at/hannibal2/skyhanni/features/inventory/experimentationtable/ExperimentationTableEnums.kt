package at.hannibal2.skyhanni.features.inventory.experimentationtable

enum class ExperimentMessages(private val str: String) {
    DONE("§eYou claimed the §dSuperpairs §erewards! §8(§7Claim§8)"),
    EXPERIENCE("§8 +§3141k Experience §8(§7Experience Drops§8)"),
    ENCHANTMENTS("§8 +§9Smite VII §8(§7Enchantment Drops§8)"),
    BOTTLES("§8 +§9Titanic Experience Bottle §8(§7Bottle Drops§8)"),
    MISC("§8 +§5Metaphysical Serum §8(§7Misc Drops§8)");

    override fun toString(): String {
        return str
    }
}

enum class Experiment(val nameString: String, val gridSize: Int, val startSlot: Int, val endSlot: Int, val sideSpace: Int) {
    NONE("", 0, 0, 0, 0),
    BEGINNER("Beginner", 14, 18, 35, 1),
    HIGH("High", 20, 10, 43, 2),
    GRAND("Grand", 20, 10, 43, 2),
    SUPREME("Supreme", 28, 9, 44, 1),
    TRANSCENDENT("Transcendent", 28, 9, 44, 1),
    METAPHYSICAL("Metaphysical", 28, 9, 44, 1),
    ;

    override fun toString(): String {
        return nameString
    }
}
