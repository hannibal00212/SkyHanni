package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.utils.ItemUtils.getItemCategoryOrNull
import net.minecraft.item.ItemStack

enum class ItemCategory {
    SWORD,
    LONGSWORD,
    BOW,
    SHORT_BOW,
    WAND,
    FISHING_WEAPON,
    FISHING_ROD,
    AXE,
    GAUNTLET,
    HOE,
    PICKAXE,
    SHOVEL,
    DRILL,
    SHEARS,
    DEPLOYABLE,
    VACUUM,
    ABIPHONE,
    BELT,
    NECKLACE,
    CLOAK,
    GLOVES,
    BRACELET,
    HELMET,
    CHESTPLATE,
    LEGGINGS,
    BOOTS,
    HATCESSORY,
    ACCESSORY,
    CARNIVAL_MASK,
    POWER_STONE,
    TRAVEL_SCROLL,
    REFORGE_STONE,
    BAIT,
    PET,
    TROPHY_FISH,
    ARROW,
    ARROW_POISON,
    ITEM,
    PET_ITEM,
    ENCHANTED_BOOK,
    FISHING_BAIT,
    POTION,
    RIFT_TIMECHARM,
    COSMETIC,
    MEMENTO,
    PORTAL,
    SACK,
    CHISEL,
    DYE,
    ORE,
    BLOCK,
    DWARVEN_METAL,
    GEMSTONE,

    NONE,
    ;

    companion object {

        fun Collection<ItemCategory>.containsItem(stack: ItemStack?) =
            stack?.getItemCategoryOrNull()?.let { this.contains(it) } ?: false

        val miningTools = listOf(PICKAXE, DRILL, GAUNTLET)

        val armor = setOf(HELMET, CHESTPLATE, LEGGINGS, BOOTS)
    }
}
