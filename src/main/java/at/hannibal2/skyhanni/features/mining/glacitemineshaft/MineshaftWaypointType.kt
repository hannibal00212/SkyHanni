package at.hannibal2.skyhanni.features.mining.glacitemineshaft

import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName

enum class MineshaftWaypointType(
    val displayText: String,
    val color: LorenzColor,
    private val itemName: String? = null
) {
    LAPIS("Lapis Corpse", LorenzColor.DARK_BLUE, "LAPIS_ARMOR_HELMET"),
    UMBER("Umber Corpse", LorenzColor.GOLD, "ARMOR_OF_YOG_HELMET"),
    TUNGSTEN("Tungsten Corpse", LorenzColor.GRAY, "MINERAL_HELMET"),
    VANGUARD("Vanguard Corpse", LorenzColor.BLUE, "VANGUARD_HELMET"),
    ENTRANCE("Entrance", LorenzColor.YELLOW),
    LADDER("Ladder", LorenzColor.YELLOW)
    ;

    val helmet by lazy {
        itemName?.toInternalName()
    }

    companion object {
        fun getByHelmetOrNull(internalName: NEUInternalName): MineshaftWaypointType? {
            return entries.firstOrNull { it.helmet == internalName }
        }
    }
}
