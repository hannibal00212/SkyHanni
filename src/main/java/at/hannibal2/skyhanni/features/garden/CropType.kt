package at.hannibal2.skyhanni.features.garden

import at.hannibal2.skyhanni.features.garden.fortuneguide.FarmingItems
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.ItemStack

enum class CropType(
    val cropName: String,
    val toolName: String,
    val specialDropType: String,
    val baseDrops: Double,
    iconSupplier: () -> ItemStack,
    val simpleName: String,
    val farmingItem: FarmingItems,
    val replenish: Boolean = false,
) {

    WHEAT(
        "Wheat", "THEORETICAL_HOE_WHEAT", "CROPIE", 1.0,
        { ItemStack(Items.wheat) }, "wheat", FarmingItems.WHEAT
    ),
    SEEDS(
        "Seeds", "THEORETICAL_HOE_WHEAT", "CROPIE", 1.0,
        { ItemStack(Items.wheat_seeds) }, "seeds", FarmingItems.SEEDS,
    ),
    CARROT(
        "Carrot", "THEORETICAL_HOE_CARROT", "CROPIE", 3.0,
        { ItemStack(Items.carrot) }, "carrot", FarmingItems.CARROT, replenish = true
    ),
    POTATO(
        "Potato", "THEORETICAL_HOE_POTATO", "CROPIE", 3.0,
        { ItemStack(Items.potato) }, "potato", FarmingItems.POTATO, replenish = true
    ),
    NETHER_WART(
        "Nether Wart", "THEORETICAL_HOE_WARTS", "FERMENTO", 2.5,
        { ItemStack(Items.nether_wart) }, "wart", FarmingItems.NETHER_WART, replenish = true
    ),
    PUMPKIN(
        "Pumpkin", "PUMPKIN_DICER", "SQUASH", 1.0,
        { ItemStack(Blocks.pumpkin) }, "pumpkin", FarmingItems.PUMPKIN
    ),
    MELON(
        "Melon", "MELON_DICER", "SQUASH", 5.0,
        { ItemStack(Items.melon) }, "melon", FarmingItems.MELON
    ),
    COCOA_BEANS(
        "Cocoa Beans", "COCO_CHOPPER", "SQUASH", 3.0,
        { ItemStack(Items.dye, 1, EnumDyeColor.BROWN.dyeDamage) }, "cocoa", FarmingItems.COCOA_BEANS, replenish = true
    ),
    SUGAR_CANE(
        "Sugar Cane", "THEORETICAL_HOE_CANE", "FERMENTO", 2.0,
        { ItemStack(Items.reeds) }, "cane", FarmingItems.SUGAR_CANE
    ),
    CACTUS(
        "Cactus", "CACTUS_KNIFE", "FERMENTO", 2.0,
        { ItemStack(Blocks.cactus) }, "cactus", FarmingItems.CACTUS
    ),
    MUSHROOM(
        "Mushroom", "FUNGI_CUTTER", "FERMENTO", 1.0,
        { ItemStack(Blocks.red_mushroom_block) }, "mushroom", FarmingItems.MUSHROOM
    ),
    ;

    val icon by lazy { iconSupplier() }

    val multiplier by lazy { if (this == SUGAR_CANE || this == CACTUS) 2 else 1 }

    override fun toString(): String = cropName

    val patternKeyName = name.lowercase().replace('_', '.')
    val niceName = name.lowercase().replace('_', ' ')

    companion object {

        fun getByNameOrNull(itemName: String): CropType? {
            if (itemName == "Red Mushroom" || itemName == "Brown Mushroom") return MUSHROOM
            if (itemName == "Seeds") return WHEAT
            return entries.firstOrNull {
                it.cropName.equals(itemName, ignoreCase = true) ||
                    it.simpleName.equals(itemName, ignoreCase = true)
            }
        }

        fun getByName(name: String) = getByNameOrNull(name) ?: error("No valid crop type '$name'")

        fun IBlockState.getCropType(): CropType? {
            return when (block) {
                Blocks.wheat -> WHEAT
                Blocks.carrots -> CARROT
                Blocks.potatoes -> POTATO
                Blocks.pumpkin -> PUMPKIN
                Blocks.reeds -> SUGAR_CANE
                Blocks.melon_block -> MELON
                Blocks.cactus -> CACTUS
                Blocks.cocoa -> COCOA_BEANS
                Blocks.red_mushroom, Blocks.brown_mushroom -> MUSHROOM
                Blocks.nether_wart -> NETHER_WART
                else -> null
            }
        }

        fun CropType.getTurboCrop(): String {
            return when (this) {
                COCOA_BEANS -> "turbo_coco"
                SUGAR_CANE -> "turbo_cane"
                NETHER_WART -> "turbo_warts"
                MUSHROOM -> "turbo_mushrooms"
                else -> "turbo_${this.cropName.lowercase()}"
            }
        }
    }
}
