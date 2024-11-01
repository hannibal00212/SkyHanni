package at.hannibal2.skyhanni.features.mining

import at.hannibal2.skyhanni.data.MiningAPI
import at.hannibal2.skyhanni.data.MiningAPI.currentAreaOreBlocks
import at.hannibal2.skyhanni.data.MiningAPI.inCrimsonIsle
import at.hannibal2.skyhanni.data.MiningAPI.inCrystalHollows
import at.hannibal2.skyhanni.data.MiningAPI.inDwarvenMines
import at.hannibal2.skyhanni.data.MiningAPI.inEnd
import at.hannibal2.skyhanni.data.MiningAPI.inGlacite
import at.hannibal2.skyhanni.data.MiningAPI.inMineshaft
import at.hannibal2.skyhanni.data.MiningAPI.inSpidersDen
import at.hannibal2.skyhanni.data.MiningAPI.inTunnels
import at.hannibal2.skyhanni.utils.CollectionUtils.equalsOneOf
import net.minecraft.block.Block
import net.minecraft.block.BlockColored
import net.minecraft.block.BlockSand
import net.minecraft.block.BlockSilverfish
import net.minecraft.block.BlockStainedGlass
import net.minecraft.block.BlockStainedGlassPane
import net.minecraft.block.BlockStone
import net.minecraft.block.BlockStoneSlab
import net.minecraft.block.BlockStoneSlabNew
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.item.EnumDyeColor
import kotlin.math.ceil
import kotlin.math.round

enum class OreCategory {
    BLOCK,
    ORE,
    DWARVEN_METAL,
    GEMSTONE,
}

enum class OreBlock(
    val checkBlock: (IBlockState) -> Boolean,
    val checkArea: () -> Boolean,
    val category: OreCategory,
    val hasInitSound: Boolean = true,
) {
    // MITHRIL
    LOW_TIER_MITHRIL(::isLowTierMithril, { inDwarvenMines || inGlacite }, OreCategory.DWARVEN_METAL),
    MID_TIER_MITHRIL(Blocks.prismarine, { inDwarvenMines || inCrystalHollows || inGlacite }, OreCategory.DWARVEN_METAL),
    HIGH_TIER_MITHRIL(::isHighTierMithril, { inDwarvenMines || inCrystalHollows || inGlacite }, OreCategory.DWARVEN_METAL),

    // TITANIUM
    TITANIUM(::isTitanium, { inDwarvenMines || inGlacite }, OreCategory.DWARVEN_METAL),

    // VANILLA ORES
    STONE(::isStone, { inDwarvenMines }, OreCategory.BLOCK),
    COBBLESTONE(Blocks.cobblestone, { inDwarvenMines }, OreCategory.BLOCK),
    COAL_ORE(Blocks.coal_ore, { inDwarvenMines || inCrystalHollows }, OreCategory.ORE),
    IRON_ORE(Blocks.iron_ore, { inDwarvenMines || inCrystalHollows }, OreCategory.ORE),
    GOLD_ORE(Blocks.gold_ore, { inDwarvenMines || inCrystalHollows }, OreCategory.ORE),
    LAPIS_ORE(Blocks.lapis_ore, { inDwarvenMines || inCrystalHollows }, OreCategory.ORE),
    REDSTONE_ORE(
        { it.block.equalsOneOf(Blocks.redstone_ore, Blocks.lit_redstone_ore) },
        { inDwarvenMines || inCrystalHollows },
        OreCategory.ORE,
    ),
    EMERALD_ORE(Blocks.emerald_ore, { inDwarvenMines || inCrystalHollows }, OreCategory.ORE),
    DIAMOND_ORE(Blocks.diamond_ore, { inDwarvenMines || inCrystalHollows }, OreCategory.ORE),

    // NETHER
    NETHERRACK(Blocks.netherrack, { inCrimsonIsle }, OreCategory.BLOCK),
    QUARTZ_ORE(Blocks.quartz_ore, { inCrystalHollows || inCrimsonIsle }, OreCategory.ORE),
    GLOWSTONE(Blocks.glowstone, { inCrimsonIsle }, OreCategory.BLOCK),
    MYCELIUM(Blocks.mycelium, { inCrimsonIsle }, OreCategory.BLOCK),
    RED_SAND(::isRedSand, { inCrimsonIsle }, OreCategory.BLOCK),
    SULPHUR(Blocks.sponge, { inCrimsonIsle }, OreCategory.ORE),

    // SPIDER'S DEN
    GRAVEL(Blocks.gravel, { inSpidersDen }, OreCategory.BLOCK),

    // END
    END_STONE(Blocks.end_stone, { inEnd }, OreCategory.BLOCK),
    OBSIDIAN(Blocks.obsidian, { inCrystalHollows || inEnd }, OreCategory.ORE),

    // HARD STONE
    HARD_STONE_HOLLOWS(::isHardStoneHollows, { inCrystalHollows }, OreCategory.BLOCK),
    HARD_STONE_TUNNELS(::isHardstoneTunnels, { inTunnels }, OreCategory.BLOCK),
    HARD_STONE_MINESHAFT(::isHardstoneMineshaft, { inMineshaft }, OreCategory.BLOCK),

    // DWARVEN BLOCKS
    PURE_COAL(Blocks.coal_block, { inDwarvenMines || inCrystalHollows }, OreCategory.ORE),
    PURE_IRON(Blocks.iron_block, { inDwarvenMines || inCrystalHollows }, OreCategory.ORE, hasInitSound = false),
    PURE_GOLD(Blocks.gold_block, { inDwarvenMines || inCrystalHollows }, OreCategory.ORE, hasInitSound = false),
    PURE_LAPIS(Blocks.lapis_block, { inDwarvenMines || inCrystalHollows }, OreCategory.ORE),
    PURE_REDSTONE(Blocks.redstone_block, { inDwarvenMines || inCrystalHollows }, OreCategory.ORE, hasInitSound = false),
    PURE_EMERALD(Blocks.emerald_block, { inDwarvenMines || inCrystalHollows }, OreCategory.ORE, hasInitSound = false),
    PURE_DIAMOND(Blocks.diamond_block, { inDwarvenMines || inCrystalHollows }, OreCategory.ORE, hasInitSound = false),

    // GEMSTONES
    RUBY(EnumDyeColor.RED, { inCrystalHollows || inGlacite }, OreCategory.GEMSTONE),
    AMBER(EnumDyeColor.ORANGE, { inCrystalHollows || inGlacite }, OreCategory.GEMSTONE),
    AMETHYST(EnumDyeColor.PURPLE, { inCrystalHollows || inGlacite }, OreCategory.GEMSTONE),
    JADE(EnumDyeColor.LIME, { inCrystalHollows || inGlacite }, OreCategory.GEMSTONE),
    SAPPHIRE(EnumDyeColor.LIGHT_BLUE, { inCrystalHollows || inGlacite }, OreCategory.GEMSTONE),
    TOPAZ(EnumDyeColor.YELLOW, { inCrystalHollows || inGlacite }, OreCategory.GEMSTONE),
    JASPER(EnumDyeColor.MAGENTA, { inCrystalHollows || inGlacite }, OreCategory.GEMSTONE),
    OPAL(EnumDyeColor.WHITE, { inGlacite || inCrimsonIsle }, OreCategory.GEMSTONE),
    AQUAMARINE(EnumDyeColor.BLUE, { inGlacite }, OreCategory.GEMSTONE),
    CITRINE(EnumDyeColor.BROWN, { inGlacite }, OreCategory.GEMSTONE),
    ONYX(EnumDyeColor.BLACK, { inGlacite }, OreCategory.GEMSTONE),
    PERIDOT(EnumDyeColor.GREEN, { inGlacite }, OreCategory.GEMSTONE),

    // GLACIAL
    LOW_TIER_UMBER(::isLowTierUmber, { inGlacite }, OreCategory.DWARVEN_METAL),
    HIGH_TIER_UMBER(::isHighTierUmber, { inGlacite }, OreCategory.DWARVEN_METAL),

    LOW_TIER_TUNGSTEN_TUNNELS(::isLowTierTungstenTunnels, { inTunnels }, OreCategory.DWARVEN_METAL),
    LOW_TIER_TUNGSTEN_MINESHAFT(::isLowTierTungstenMineshaft, { inMineshaft }, OreCategory.DWARVEN_METAL),
    HIGH_TIER_TUNGSTEN(Blocks.clay, { inGlacite }, OreCategory.DWARVEN_METAL),

    GLACITE(Blocks.packed_ice, { inGlacite }, OreCategory.DWARVEN_METAL),
    ;

    val strength get() = MiningAPI.blockStrengths[this] ?: 0

    val speedSoftCap get() = ceil(20.0 / 3.0 * strength).toInt()

    val speedForInstantMine get() = strength * if (category != OreCategory.BLOCK) 60 else 30

    fun miningTicks(speed: Double): Int = when {
        speed >= speedForInstantMine -> 1
        speed >= speedSoftCap -> 4
        else -> round((strength * 30.0) / speed).toInt()
    }

    constructor(block: Block, checkArea: () -> Boolean, category: OreCategory, hasInitSound: Boolean = true) :
        this({ it.block == block }, checkArea, category, hasInitSound)

    constructor(gemstoneColor: EnumDyeColor, checkArea: () -> Boolean, category: OreCategory, hasInitSound: Boolean = true) :
        this({ it.isGemstoneWithColor(gemstoneColor) }, checkArea, category, hasInitSound)

    companion object {
        fun getByStateOrNull(state: IBlockState): OreBlock? = currentAreaOreBlocks.find { it.checkBlock(state) }

        fun getByNameOrNull(string: String) = entries.firstOrNull { it.name == string }
    }
}

private fun isLowTierMithril(state: IBlockState): Boolean = when (state.block) {
    Blocks.wool -> state.getValue(BlockColored.COLOR) == EnumDyeColor.GRAY
    Blocks.stained_hardened_clay -> state.getValue(BlockColored.COLOR) == EnumDyeColor.CYAN
    else -> false
}

private fun isHighTierMithril(state: IBlockState): Boolean {
    return (state.block == Blocks.wool && state.getValue(BlockColored.COLOR) == EnumDyeColor.LIGHT_BLUE)
}

fun isTitanium(state: IBlockState): Boolean {
    return (state.block == Blocks.stone && state.getValue(BlockStone.VARIANT) == BlockStone.EnumType.DIORITE_SMOOTH)
}

private fun isStone(state: IBlockState): Boolean {
    return (state.block == Blocks.stone && state.getValue(BlockStone.VARIANT) == BlockStone.EnumType.STONE)
}

private fun isHardStoneHollows(state: IBlockState): Boolean {
    return when (state.block) {
        Blocks.wool -> {
            val color = state.getValue(BlockColored.COLOR)
            color == EnumDyeColor.GRAY || color == EnumDyeColor.GREEN
        }

        Blocks.stained_hardened_clay -> when (state.getValue(BlockColored.COLOR)) {
            EnumDyeColor.CYAN, EnumDyeColor.BROWN, EnumDyeColor.GRAY, EnumDyeColor.BLACK,
            EnumDyeColor.LIME, EnumDyeColor.GREEN, EnumDyeColor.BLUE, EnumDyeColor.RED,
            EnumDyeColor.SILVER,
            -> true

            else -> false
        }

        Blocks.clay, Blocks.stonebrick, Blocks.stone -> true
        else -> false
    }
}

private fun isHardstoneTunnels(state: IBlockState): Boolean =
    (state.block == Blocks.monster_egg && state.getValue(BlockSilverfish.VARIANT) == BlockSilverfish.EnumType.STONE) ||
        (state.block == Blocks.wool && state.getValue(BlockColored.COLOR) == EnumDyeColor.SILVER)

private fun isHardstoneMineshaft(state: IBlockState): Boolean =
    (state.block == Blocks.stone && state.getValue(BlockStone.VARIANT) == BlockStone.EnumType.STONE) ||
        (state.block == Blocks.wool && state.getValue(BlockColored.COLOR) == EnumDyeColor.SILVER)

private fun isRedSand(state: IBlockState): Boolean =
    (state.block == Blocks.sand && state.getValue(BlockSand.VARIANT) == BlockSand.EnumType.RED_SAND)

private fun isLowTierUmber(state: IBlockState): Boolean =
    state.block == Blocks.hardened_clay ||
        (state.block == Blocks.stained_hardened_clay && state.getValue(BlockColored.COLOR) == EnumDyeColor.BROWN)

private fun isHighTierUmber(state: IBlockState): Boolean =
    (state.block == Blocks.double_stone_slab2 && state.getValue(BlockStoneSlabNew.VARIANT) == BlockStoneSlabNew.EnumType.RED_SANDSTONE)

private fun isLowTierTungstenTunnels(state: IBlockState): Boolean =
    state.block == Blocks.monster_egg && state.getValue(BlockSilverfish.VARIANT) == BlockSilverfish.EnumType.COBBLESTONE

private fun isLowTierTungstenMineshaft(state: IBlockState): Boolean = when (state.block) {
    Blocks.stone_slab -> state.getValue(BlockStoneSlab.VARIANT) == BlockStoneSlab.EnumType.COBBLESTONE
    Blocks.cobblestone, Blocks.stone_stairs -> true
    else -> false
}

private fun IBlockState.isGemstoneWithColor(color: EnumDyeColor): Boolean = when (block) {
    Blocks.stained_glass -> color == getValue(BlockStainedGlass.COLOR)
    Blocks.stained_glass_pane -> color == getValue(BlockStainedGlassPane.COLOR)
    else -> false
}
