package at.hannibal2.skyhanni.features.garden

import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName

enum class FarmingBooks(
    val cropType: CropType,
) {
    TURBO_CACTI(CropType.CACTUS),
    TURBO_CANE(CropType.SUGAR_CANE),
    TURBO_CARROT(CropType.CARROT),
    TURBO_COCOA(CropType.COCOA_BEANS),
    TURBO_MELON(CropType.MELON),
    TURBO_MUSHROOMS(CropType.MUSHROOM),
    TURBO_POTATO(CropType.POTATO),
    TURBO_PUMPKIN(CropType.PUMPKIN),
    TURBO_WARTS(CropType.NETHER_WART),
    TURBO_WHEAT(CropType.WHEAT)
    ;

    private val bookSet: Map<Int, NEUInternalName> by lazy {
        (1..5).associateWith { tier ->
            "TURBO_${cropType.enchantName.uppercase()};$tier".toInternalName()
        }
    }

    fun getBook(tier: Int): NEUInternalName = bookSet.getValue(tier)

    companion object {
        fun getByCropTypeOrNull(type: CropType): FarmingBooks? = entries.find { it.cropType == type }
    }
}
