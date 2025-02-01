package at.hannibal2.skyhanni.features.garden.pests

import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName

enum class SprayType(val displayName: String) {
    COMPOST("Compost"),
    PLANT_MATTER("Plant Matter"),
    DUNG("Dung"),
    HONEY_JAR("Honey Jar"),
    TASTY_CHEESE("Tasty Cheese"),
    FINE_FLOUR("Fine Flour"),
    ;

    fun toInternalName(): NeuInternalName = name.toInternalName()

    companion object {

        fun getByNameOrNull(name: String) = entries.firstOrNull { it.displayName == name }
        fun getByInternalName(internalName: NeuInternalName) = entries.firstOrNull { it.name == internalName.asString() }
        fun getByPestTypeOrAll(pestType: PestType?) = entries.filter {
            it == pestType?.spray
        }.takeIf {
            it.isNotEmpty()
        } ?: entries
    }
}
