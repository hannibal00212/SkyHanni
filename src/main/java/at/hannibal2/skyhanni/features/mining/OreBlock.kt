package at.hannibal2.skyhanni.features.mining

import at.hannibal2.skyhanni.data.MiningAPI.currentAreaOreBlocks
import at.hannibal2.skyhanni.data.MiningAPI.inCrimsonIsle
import at.hannibal2.skyhanni.data.MiningAPI.inCrystalHollows
import at.hannibal2.skyhanni.data.MiningAPI.inDwarvenMines
import at.hannibal2.skyhanni.data.MiningAPI.inEnd
import at.hannibal2.skyhanni.data.MiningAPI.inGlacite
import at.hannibal2.skyhanni.data.MiningAPI.inSpidersDen
import at.hannibal2.skyhanni.utils.CollectionUtils.equalsOneOf
import net.minecraft.block.BlockColored
import net.minecraft.block.BlockSand
import net.minecraft.block.BlockStainedGlass
import net.minecraft.block.BlockStainedGlassPane
import net.minecraft.block.BlockStone
import net.minecraft.block.BlockStoneSlabNew
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.item.EnumDyeColor

enum class OreBlock(
    val checkBlock: (IBlockState) -> Boolean,
    val checkArea: () -> Boolean,
) {
    // MITHRIL
    LOW_TIER_MITHRIL(
        checkBlock = ::isLowTierMithril,
        checkArea = { inDwarvenMines || inCrystalHollows || inGlacite },
    ),
    MID_TIER_MITHRIL(
        checkBlock = { it.block == Blocks.prismarine },
        checkArea = { inDwarvenMines || inCrystalHollows || inGlacite },
    ),
    HIGH_TIER_MITHRIL(
        checkBlock = ::isHighTierMithril,
        checkArea = { inDwarvenMines || inCrystalHollows || inGlacite },
    ),

    // TITANIUM
    TITANIUM(
        checkBlock = ::isTitanium,
        checkArea = { inDwarvenMines || inGlacite },
    ),

    // VANILLA ORES
    STONE(
        checkBlock = ::isStone,
        checkArea = { inDwarvenMines },
    ),
    COBBLESTONE(
        checkBlock = { it.block == Blocks.cobblestone },
        checkArea = { inDwarvenMines },
    ),
    COAL_ORE(
        checkBlock = { it.block == Blocks.coal_ore },
        checkArea = { inDwarvenMines || inCrystalHollows },
    ),
    IRON_ORE(
        checkBlock = { it.block == Blocks.iron_ore },
        checkArea = { inDwarvenMines || inCrystalHollows },
    ),
    GOLD_ORE(
        checkBlock = { it.block == Blocks.gold_ore },
        checkArea = { inDwarvenMines || inCrystalHollows },
    ),
    LAPIS_ORE(
        checkBlock = { it.block == Blocks.lapis_ore },
        checkArea = { inDwarvenMines || inCrystalHollows },
    ),
    REDSTONE_ORE(
        checkBlock = { it.block.equalsOneOf(Blocks.redstone_ore, Blocks.lit_redstone_ore) },
        checkArea = { inDwarvenMines || inCrystalHollows },
    ),
    EMERALD_ORE(
        checkBlock = { it.block == Blocks.emerald_ore },
        checkArea = { inDwarvenMines || inCrystalHollows },
    ),
    DIAMOND_ORE(
        checkBlock = { it.block == Blocks.diamond_ore },
        checkArea = { inDwarvenMines || inCrystalHollows },
    ),

    // NETHER
    NETHERRACK(
        checkBlock = { it.block == Blocks.netherrack },
        checkArea = { inCrimsonIsle },
    ),
    QUARTZ_ORE(
        checkBlock = { it.block == Blocks.quartz_ore },
        checkArea = { inCrystalHollows || inCrimsonIsle },
    ),
    GLOWSTONE(
        checkBlock = { it.block == Blocks.glowstone },
        checkArea = { inCrimsonIsle },
    ),
    MYCELIUM(
        checkBlock = { it.block == Blocks.mycelium },
        checkArea = { inCrimsonIsle },
    ),
    RED_SAND(
        checkBlock = ::isRedSand,
        checkArea = { inCrimsonIsle },
    ),
    SULPHUR(
        checkBlock = { it.block == Blocks.sponge },
        checkArea = { inCrimsonIsle },
    ),

    // SPIDER'S DEN
    GRAVEL(
        checkBlock = { it.block == Blocks.gravel },
        checkArea = { inSpidersDen },
    ),

    //END
    END_STONE(
        checkBlock = { it.block == Blocks.end_stone },
        checkArea = { inEnd },
    ),
    OBSIDIAN(
        checkBlock = { it.block == Blocks.obsidian },
        checkArea = { inCrystalHollows || inEnd },
    ),

    // HARD STONE
    HARD_STONE_HOLLOWS(
        checkBlock = ::isHardStoneHollows,
        checkArea = { inCrystalHollows },
    ),
    HARD_STONE_GLACIAL(
        checkBlock = ::isHardstoneGlacite,
        checkArea = { inGlacite },
    ),

    // DWARVEN BLOCKS
    PURE_COAL(
        checkBlock = { it.block == Blocks.coal_block },
        checkArea = { inDwarvenMines || inCrystalHollows },
    ),
    PURE_IRON(
        checkBlock = { it.block == Blocks.iron_block },
        checkArea = { inDwarvenMines || inCrystalHollows },
    ),
    PURE_GOLD(
        checkBlock = { it.block == Blocks.gold_block },
        checkArea = { inDwarvenMines || inCrystalHollows },
    ),
    PURE_LAPIS(
        checkBlock = { it.block == Blocks.lapis_block },
        checkArea = { inDwarvenMines || inCrystalHollows },
    ),
    PURE_REDSTONE(
        checkBlock = { it.block == Blocks.redstone_block },
        checkArea = { inDwarvenMines || inCrystalHollows },
    ),
    PURE_EMERALD(
        checkBlock = { it.block == Blocks.emerald_block },
        checkArea = { inDwarvenMines || inCrystalHollows },
    ),
    PURE_DIAMOND(
        checkBlock = { it.block == Blocks.diamond_block },
        checkArea = { inDwarvenMines || inCrystalHollows },
    ),

    // GEMSTONES
    RUBY(
        checkBlock = { it.isGemstoneWithColor(EnumDyeColor.RED) },
        checkArea = { inCrystalHollows || inGlacite },
    ),
    AMBER(
        checkBlock = { it.isGemstoneWithColor(EnumDyeColor.ORANGE) },
        checkArea = { inCrystalHollows || inGlacite },
    ),
    AMETHYST(
        checkBlock = { it.isGemstoneWithColor(EnumDyeColor.PURPLE) },
        checkArea = { inCrystalHollows || inGlacite },
    ),
    JADE(
        checkBlock = { it.isGemstoneWithColor(EnumDyeColor.LIME) },
        checkArea = { inCrystalHollows || inGlacite },
    ),
    SAPPHIRE(
        checkBlock = { it.isGemstoneWithColor(EnumDyeColor.LIGHT_BLUE) },
        checkArea = { inCrystalHollows || inGlacite },
    ),
    TOPAZ(
        checkBlock = { it.isGemstoneWithColor(EnumDyeColor.YELLOW) },
        checkArea = { inCrystalHollows || inGlacite },
    ),
    JASPER(
        checkBlock = { it.isGemstoneWithColor(EnumDyeColor.MAGENTA) },
        checkArea = { inCrystalHollows || inGlacite },
    ),
    OPAL(
        checkBlock = { it.isGemstoneWithColor(EnumDyeColor.WHITE) },
        checkArea = { inGlacite || inCrimsonIsle },
    ),
    AQUAMARINE(
        checkBlock = { it.isGemstoneWithColor(EnumDyeColor.BLUE) },
        checkArea = { inGlacite },
    ),
    CITRINE(
        checkBlock = { it.isGemstoneWithColor(EnumDyeColor.BROWN) },
        checkArea = { inGlacite },
    ),
    ONYX(
        checkBlock = { it.isGemstoneWithColor(EnumDyeColor.BLACK) },
        checkArea = { inGlacite },
    ),
    PERIDOT(
        checkBlock = { it.isGemstoneWithColor(EnumDyeColor.GREEN) },
        checkArea = { inGlacite },
    ),

    // GLACIAL
    LOW_TIER_UMBER(
        checkBlock = ::isLowTierUmber,
        checkArea = { inGlacite },
    ),
    HIGH_TIER_UMBER(
        checkBlock = ::isHighTierUmber,
        checkArea = { inGlacite },
    ),
    LOW_TIER_TUNGSTEN(
        checkBlock = { it.block == Blocks.cobblestone },
        checkArea = { inGlacite },
    ),
    HIGH_TIER_TUNGSTEN(
        checkBlock = { it.block == Blocks.clay },
        checkArea = { inGlacite },
    ),
    GLACITE(
        checkBlock = { it.block == Blocks.packed_ice },
        checkArea = { inGlacite },
    ),
    ;

    companion object {
        fun getByStateOrNull(state: IBlockState): OreBlock? = currentAreaOreBlocks.firstOrNull { it.checkBlock(state) }
    }
}

private fun isLowTierMithril(state: IBlockState): Boolean {
    return when (state.block) {
        Blocks.wool -> state.getValue(BlockColored.COLOR) == EnumDyeColor.GRAY
        Blocks.stained_hardened_clay -> state.getValue(BlockColored.COLOR) == EnumDyeColor.CYAN
        else -> false
    }
}

private fun isHighTierMithril(state: IBlockState): Boolean {
    return (state.block == Blocks.wool && state.getValue(BlockColored.COLOR) == EnumDyeColor.LIGHT_BLUE)
}

private fun isTitanium(state: IBlockState): Boolean {
    return (state.block == Blocks.stone && state.getValue(BlockStone.VARIANT) == BlockStone.EnumType.DIORITE_SMOOTH)
}

private fun isStone(state: IBlockState): Boolean {
    return (state.block == Blocks.stone && state.getValue(BlockStone.VARIANT) == BlockStone.EnumType.STONE)
}

private fun isHardStoneHollows(state: IBlockState): Boolean {
    return when (state.block) {
        Blocks.wool -> (state.getValue(BlockColored.COLOR) == EnumDyeColor.GRAY)
        Blocks.stained_hardened_clay -> (state.getValue(BlockColored.COLOR) == EnumDyeColor.CYAN)
        Blocks.stone -> (state.getValue(BlockStone.VARIANT) == BlockStone.EnumType.STONE)
        else -> false
    }
}

private fun isHardstoneGlacite(state: IBlockState): Boolean =
    (state.block == Blocks.stone && state.getValue(BlockStone.VARIANT) == BlockStone.EnumType.STONE) ||
        (state.block == Blocks.wool && state.getValue(BlockColored.COLOR) == EnumDyeColor.GRAY)

private fun isRedSand(state: IBlockState): Boolean =
    (state.block == Blocks.sand && state.getValue(BlockSand.VARIANT) == BlockSand.EnumType.RED_SAND)

private fun isLowTierUmber(state: IBlockState): Boolean = state.block == Blocks.hardened_clay ||
    (state.block == Blocks.stained_hardened_clay && state.getValue(BlockColored.COLOR) == EnumDyeColor.BROWN)

private fun isHighTierUmber(state: IBlockState): Boolean =
    (state.block == Blocks.double_stone_slab2 && state.getValue(BlockStoneSlabNew.VARIANT) == BlockStoneSlabNew.EnumType.RED_SANDSTONE)

private fun IBlockState.isGemstoneWithColor(color: EnumDyeColor): Boolean {
    return when (this.block) {
        Blocks.stained_glass -> color == this.getValue(BlockStainedGlass.COLOR)
        Blocks.stained_glass_pane -> color == this.getValue(BlockStainedGlassPane.COLOR)
        else -> false
    }
}
