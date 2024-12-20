package at.hannibal2.skyhanni.features.garden.fortuneguide

import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

enum class CarrolynTable(val crop: CropType, val label: String, completeMessage: String, thxMessage: String) {
    EXPORTABLE_CARROTS(
        CropType.CARROT,
        "Exportable Carrots",
        "CARROTS EXPORTATION COMPLETE!",
        "[NPC] Carrolyn: Thank you for the carrots.",
    ),
    EXPIRED_PUMPKIN(
        CropType.PUMPKIN,
        "Expired Pumpkin",
        "PUMPKINS EXPORTATION COMPLETE!",
        "[NPC] Carrolyn: Thank you for the pumpkins.",
    ),
    SUPREME_CHOCOLATE_BAR(
        CropType.COCOA_BEANS,
        "Supreme Chocolate Bar",
        "COCOA BEANSS EXPORTATION COMPLETE!",
        "[NPC] Carrolyn: Thank you for the chocolate.",
    ),
    FINE_FLOUR(
        CropType.WHEAT,
        "Fine Flour",
        "FINE FLOURS EXPORTATION COMPLETE!",
        "[NPC] Carrolyn: Thank you for the flour.",
    ),
    ;

    /** Pattern without color codes */
    val completeMessagePattern by RepoPattern.pattern(
        "garden.ff.carrolyn.complete.${crop.patternKeyName}", completeMessage,
    )

    /** Pattern without color codes */
    val thxMessagePattern by RepoPattern.pattern(
        "garden.ff.carrolyn.thx.${crop.patternKeyName}", thxMessage,
    )

    val thxResponse = "§aYou have already given Carrolyn enough $label."

    fun get() = GardenAPI.storage?.fortune?.carrolyn?.get(crop) ?: false
    fun set(value: Boolean) = GardenAPI.storage?.fortune?.carrolyn?.set(crop, value)

    fun setVisibleActive(value: Boolean) {
        set(value)
        ChatUtils.chat("Toggled $label fortune to: ${get()}")
    }

    companion object {
        fun getByCrop(crop: CropType?) = if (crop == null) null else entries.firstOrNull { it.crop == crop }

        fun isCarrolynCrop(crop: CropType): Boolean = CarrolynTable.getByCrop(crop) != null
        fun customTabComplete(command: String): List<String>? {
            if (command == "shcarrolyn") {
                return entries.map { it.crop.name }
            }

            return null
        }
    }
}
