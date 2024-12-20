package at.hannibal2.skyhanni.features.skillprogress

import at.hannibal2.skyhanni.utils.ItemUtils
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

enum class SkillType(val displayName: String, icon: Item) {
    COMBAT("Combat", Items.golden_sword),
    FARMING("Farming", Items.golden_hoe),
    FISHING("Fishing", Items.fishing_rod),
    MINING("Mining", Items.golden_pickaxe),
    FORAGING("Foraging", Items.golden_axe),
    ENCHANTING("Enchanting", Blocks.enchanting_table),
    ALCHEMY("Alchemy", Items.brewing_stand),
    CARPENTRY("Carpentry", Blocks.crafting_table),
    TAMING("Taming", Items.spawn_egg),
    ;

    constructor(displayName: String, block: Block) : this(displayName, Item.getItemFromBlock(block))

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
