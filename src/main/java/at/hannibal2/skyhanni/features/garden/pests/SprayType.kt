package at.hannibal2.skyhanni.features.garden.pests

enum class SprayType(val displayName: String) {
    COMPOST("Compost"),
    PLANT_MATTER("Plant Matter"),
    DUNG("Dung"),
    HONEY_JAR("Honey Jar"),
    TASTY_CHEESE("Tasty Cheese"),
    FINE_FLOUR("Fine Flour"),
    ;

    companion object {

        fun getByNameOrNull(name: String) = entries.firstOrNull { it.displayName == name }
        fun getByPestTypeOrAll(pestType: PestType?) = entries.filter {
            it == pestType?.spray
        }.takeIf {
            it.isNotEmpty()
        } ?: entries
    }
}
