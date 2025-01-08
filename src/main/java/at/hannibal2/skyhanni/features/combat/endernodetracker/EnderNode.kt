package at.hannibal2.skyhanni.features.combat.endernodetracker

import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName

enum class EnderNode(
    val internalName: NEUInternalName,
    val displayName: String,
) {

    ENCHANTED_ENDSTONE("ENCHANTED_ENDSTONE".toInternalName(), "§aEnchanted End Stone"),
    ENCHANTED_OBSIDIAN("ENCHANTED_OBSIDIAN".toInternalName(), "§aEnchanted Obsidian"),
    ENCHANTED_ENDER_PEARL("ENCHANTED_ENDER_PEARL".toInternalName(), "§aEnchanted Ender Pearl"),
    GRAND_EXP_BOTTLE("GRAND_EXP_BOTTLE".toInternalName(), "§aGrand Experience Bottle"),
    TITANIC_EXP_BOTTLE("TITANIC_EXP_BOTTLE".toInternalName(), "§9Titanic Experience Bottle"),
    END_STONE_SHULKER("END_STONE_SHULKER".toInternalName(), "§9End Stone Shulker"),
    ENDSTONE_GEODE("ENDSTONE_GEODE".toInternalName(), "§9End Stone Geode"),
    MAGIC_RUNE("MAGIC_RUNE;1".toInternalName(), "§d◆ Magical Rune I"),
    ENDER_GAUNTLET("ENDER_GAUNTLET".toInternalName(), "§5Ender Gauntlet"),
    MITE_GEL("MITE_GEL".toInternalName(), "§5Mite Gel"),
    SHRIMP_THE_FISH("SHRIMP_THE_FISH".toInternalName(), "§cShrimp the Fish"),

    END_HELMET("END_HELMET".toInternalName(), "§5Ender Helmet"),
    END_CHESTPLATE("END_CHESTPLATE".toInternalName(), "§5Ender Chestplate"),
    END_LEGGINGS("END_LEGGINGS".toInternalName(), "§5Ender Leggings"),
    END_BOOTS("END_BOOTS".toInternalName(), "§5Ender Boots"),
    ENDER_NECKLACE("ENDER_NECKLACE".toInternalName(), "§5Ender Necklace"),
    COMMON_ENDERMAN_PET("ENDERMAN;0".toInternalName(), "§fEnderman"),
    UNCOMMON_ENDERMAN_PET("ENDERMAN;1".toInternalName(), "§aEnderman"),
    RARE_ENDERMAN_PET("ENDERMAN;2".toInternalName(), "§9Enderman"),
    EPIC_ENDERMAN_PET("ENDERMAN;3".toInternalName(), "§5Enderman"),
    LEGENDARY_ENDERMAN_PET("ENDERMAN;4".toInternalName(), "§6Enderman")
}
