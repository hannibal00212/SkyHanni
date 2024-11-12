package at.hannibal2.skyhanni.features.garden

import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName

enum class CropAccessory(
    val internalName: NEUInternalName?,
    private val affectedCrops: Set<CropType>,
    private val fortune: Double,
    val upgradeCost: Pair<String, Int>?,
) {

    NONE(null, emptySet(), 0.0, null),
    CROPIE(
        "CROPIE_TALISMAN".toInternalName(),
        setOf(CropType.WHEAT, CropType.POTATO, CropType.CARROT),
        10.0,
        Pair("CROPIE", 256)
    ),
    SQUASH(
        "SQUASH_RING".toInternalName(),
        setOf(CropType.WHEAT, CropType.POTATO, CropType.CARROT, CropType.COCOA_BEANS, CropType.MELON, CropType.PUMPKIN),
        20.0, Pair("SQUASH", 128)
    ),
    FERMENTO("FERMENTO_ARTIFACT".toInternalName(), CropType.entries.toSet(), 30.0, Pair("CONDENSED_FERMENTO", 8)),
    ;

    fun getFortune(cropType: CropType): Double {
        return if (this.affectedCrops.contains(cropType)) this.fortune else 0.0
    }

    companion object {

        fun getByName(internalName: NEUInternalName) = entries.firstOrNull { internalName == it.internalName }
    }
}
