package at.hannibal2.skyhanni.features.mining

import at.hannibal2.skyhanni.data.MiningAPI
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import net.minecraft.block.state.IBlockState

enum class OreType(
    val oreName: String,
    internalName: String,
    vararg oreBlocks: OreBlock,
) {
    MITHRIL(
        "Mithril",
        "MITHRIL_ORE",
        OreBlock.LOW_TIER_MITHRIL, OreBlock.MID_TIER_MITHRIL, OreBlock.HIGH_TIER_MITHRIL,
    ),
    TITANIUM(
        "Titanium",
        "TITANIUM_ORE",
        OreBlock.TITANIUM,
    ),
    COBBLESTONE(
        "Cobblestone",
        "COBBLESTONE",
        OreBlock.STONE, OreBlock.COBBLESTONE,
    ),
    COAL(
        "Coal",
        "COAL",
        OreBlock.COAL_ORE, OreBlock.PURE_COAL,
    ),
    IRON(
        "Iron",
        "IRON_INGOT",
        OreBlock.IRON_ORE, OreBlock.PURE_IRON,
    ),
    GOLD(
        "Gold",
        "GOLD_INGOT",
        OreBlock.GOLD_ORE, OreBlock.PURE_GOLD,
    ),
    LAPIS(
        "Lapis Lazuli",
        "INK_SACK-4",
        OreBlock.LAPIS_ORE, OreBlock.PURE_LAPIS,
    ),
    REDSTONE(
        "Redstone",
        "REDSTONE",
        OreBlock.REDSTONE_ORE, OreBlock.PURE_REDSTONE,
    ),
    EMERALD(
        "Emerald",
        "EMERALD",
        OreBlock.EMERALD_ORE, OreBlock.PURE_EMERALD,
    ),
    DIAMOND(
        "Diamond",
        "DIAMOND",
        OreBlock.DIAMOND_ORE, OreBlock.PURE_DIAMOND,
    ),
    NETHERRACK(
        "Netherrack",
        "NETHERRACK",
        OreBlock.NETHERRACK,
    ),
    QUARTZ(
        "Nether Quartz",
        "QUARTZ",
        OreBlock.QUARTZ_ORE,
    ),
    GLOWSTONE(
        "Glowstone",
        "GLOWSTONE_DUST",
        OreBlock.GLOWSTONE,
    ),
    MYCELIUM(
        "Mycelium",
        "MYCEL",
        OreBlock.MYCELIUM,
    ),
    RED_SAND(
        "Red Sand",
        "SAND-1",
        OreBlock.RED_SAND,
    ),
    SULPHUR(
        "Sulphur",
        "SULPHUR_ORE",
        OreBlock.SULPHUR,
    ),
    GRAVEL(
        "Gravel",
        "GRAVEL",
        OreBlock.GRAVEL,
    ),
    END_STONE(
        "End Stone",
        "ENDER_STONE",
        OreBlock.END_STONE,
    ),
    OBSIDIAN(
        "Obsidian",
        "OBSIDIAN",
        OreBlock.OBSIDIAN,
    ),
    HARD_STONE(
        "Hard Stone",
        "HARD_STONE",
        OreBlock.HARD_STONE_HOLLOWS, OreBlock.HARD_STONE_TUNNELS, OreBlock.HARD_STONE_MINESHAFT,
    ),
    RUBY(
        "Ruby",
        "ROUGH_RUBY_GEM",
        OreBlock.RUBY,
    ),
    AMBER(
        "Amber",
        "ROUGH_AMBER_GEM",
        OreBlock.AMBER,
    ),
    AMETHYST(
        "Amethyst",
        "ROUGH_AMETHYST_GEM",
        OreBlock.AMETHYST,
    ),
    JADE(
        "Jade",
        "ROUGH_JADE_GEM",
        OreBlock.JADE,
    ),
    SAPPHIRE(
        "Sapphire",
        "ROUGH_SAPPHIRE_GEM",
        OreBlock.SAPPHIRE,
    ),
    TOPAZ(
        "Topaz",
        "ROUGH_TOPAZ_GEM",
        OreBlock.TOPAZ,
    ),
    JASPER(
        "Jasper",
        "ROUGH_JASPER_GEM",
        OreBlock.JASPER,
    ),
    OPAL(
        "Opal",
        "ROUGH_OPAL_GEM",
        OreBlock.OPAL,
    ),
    AQUAMARINE(
        "Aquamarine",
        "ROUGH_AQUAMARINE_GEM",
        OreBlock.AQUAMARINE,
    ),
    CITRINE(
        "Citrine",
        "ROUGH_CITRINE_GEM",
        OreBlock.CITRINE,
    ),
    ONYX(
        "Onyx",
        "ROUGH_ONYX_GEM",
        OreBlock.ONYX,
    ),
    PERIDOT(
        "Peridot",
        "ROUGH_PERIDOT_GEM",
        OreBlock.PERIDOT,
    ),
    UMBER(
        "Umber",
        "UMBER",
        OreBlock.LOW_TIER_UMBER, OreBlock.HIGH_TIER_UMBER,
    ),
    TUNGSTEN(
        "Tungsten",
        "TUNGSTEN",
        OreBlock.LOW_TIER_TUNGSTEN_TUNNELS, OreBlock.LOW_TIER_TUNGSTEN_MINESHAFT, OreBlock.HIGH_TIER_TUNGSTEN,
    ),
    GLACITE(
        "Glacite",
        "GLACITE",
        OreBlock.GLACITE,
    ),
    ;

    val oreBlocks = oreBlocks.toSet()

    val internalName: NEUInternalName = internalName.toInternalName()

    fun isGemstone(): Boolean = this in gemstones

    companion object {

        private val gemstones = setOf(
            RUBY, AMBER, AMETHYST, JADE,
            SAPPHIRE, TOPAZ, JASPER, OPAL,
            AQUAMARINE, CITRINE, ONYX, PERIDOT,
        )

        fun IBlockState.isOreType(oreType: OreType): Boolean {
            for (oreBlock in oreType.oreBlocks) {
                if (oreBlock !in MiningAPI.currentAreaOreBlocks) continue
                if (oreBlock.checkBlock(this)) {
                    return true
                }
            }
            return false
        }

        fun OreBlock.getOreType(): OreType? {
            return OreType.entries.firstOrNull { this in it.oreBlocks }
        }
    }
}
