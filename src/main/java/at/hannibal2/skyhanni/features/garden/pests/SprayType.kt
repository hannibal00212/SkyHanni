package at.hannibal2.skyhanni.features.garden.pests

import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName

enum class SprayType(val displayName: String) {
    COMPOST("Compost"),
    PLANT_MATTER("Plant Matter"),
    DUNG("Dung"),
    HONEY_JAR("Honey Jar"),
    TASTY_CHEESE("Tasty Cheese"),
    FINE_FLOUR("Fine Flour"),
    ;

    fun toInternalName(): NEUInternalName = name.toInternalName()

    companion object {

        fun getByNameOrNull(name: String) = entries.firstOrNull { it.displayName == name }
        fun getByPestTypeOrAll(pestType: PestType?) = entries.filter {
            it == pestType?.spray
        }.takeIf {
            it.isNotEmpty()
        } ?: entries
    }
}
