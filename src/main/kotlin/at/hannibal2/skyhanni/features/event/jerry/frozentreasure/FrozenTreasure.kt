package at.hannibal2.skyhanni.features.event.jerry.frozentreasure

import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

enum class FrozenTreasure(
    val internalName: String,
    val displayName: String,
    val defaultAmount: Int,
    val iceMultiplier: Int = 0,
) {

    WHITE_GIFT("WHITE_GIFT", "§fWhite Gift", 1),
    GREEN_GIFT("GREEN_GIFT", "§aGreen Gift", 1),
    RED_GIFT("RED_GIFT", "§9§cRed Gift", 1),
    PACKED_ICE("PACKED_ICE", "§fPacked Ice", 32, 9),
    ENCHANTED_ICE("ENCHANTED_ICE", "§aEnchanted Ice", 9, 160), // wiki says 1-16 so assuming 9
    ENCHANTED_PACKED_ICE("ENCHANTED_PACKED_ICE", "§9Enchanted Packed Ice", 1, 25600),
    ICE_BAIT("ICE_BAIT", "§aIce Bait", 16),
    GLOWY_CHUM_BAIT("GLOWY_CHUM_BAIT", "§aGlowy Chum Bait", 16),
    GLACIAL_FRAGMENT("GLACIAL_FRAGMENT", "§5Glacial Fragment", 1),
    GLACIAL_TALISMAN("GLACIAL_TALISMAN", "§fGlacial Talisman", 1),
    FROZEN_BAIT("FROZEN_BAIT", "§9Frozen Bait", 1),
    EINARY_RED_HOODIE("FROZEN_BAIT", "§cEinary's Red Hoodie", 1),
    ;

    val chatPattern by RepoPattern.pattern(
        "event.jerry.frozentreasure.item." + this.patternName(),
        patternText()
    )

    private fun patternName() = name.lowercase().replace("_", "")
    private fun patternText() = "FROZEN TREASURE! You found ${displayName.removeColor()}!"
}
