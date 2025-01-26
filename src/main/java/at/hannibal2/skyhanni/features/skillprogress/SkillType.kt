package at.hannibal2.skyhanni.features.skillprogress

import at.hannibal2.skyhanni.utils.ItemUtils
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

enum class SkillType(val displayName: String, icon: Item, val maxLevel: Int) {
    COMBAT("Combat", Items.golden_sword, 60),
    FARMING("Farming", Items.golden_hoe, 60),
    FISHING("Fishing", Items.fishing_rod, 50),
    MINING("Mining", Items.golden_pickaxe, 60),
    FORAGING("Foraging", Items.golden_axe, 50),
    ENCHANTING("Enchanting", Blocks.enchanting_table, 60),
    ALCHEMY("Alchemy", Items.brewing_stand, 50),
    CARPENTRY("Carpentry", Blocks.crafting_table, 50),
    TAMING("Taming", Items.spawn_egg, 60),
    ;

    constructor(displayName: String, block: Block, maxLevel: Int) : this(displayName, Item.getItemFromBlock(block), maxLevel)

    val item: ItemStack by lazy { ItemUtils.createItemStack(icon, displayName) }
    val lowercaseName = displayName.lowercase()
    val uppercaseName = displayName.uppercase()

    override fun toString(): String = "§b$displayName"

    companion object {
        fun getByName(name: String) = getByNameOrNull(name) ?: error("Unknown Skill Type: '$name'")

        fun getByNameOrNull(name: String) =
            entries.firstOrNull { it.displayName.lowercase() == name.lowercase() }
    }
}
