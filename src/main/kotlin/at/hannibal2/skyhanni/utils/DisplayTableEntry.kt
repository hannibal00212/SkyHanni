package at.hannibal2.skyhanni.utils

class DisplayTableEntry(
    val left: String,
    val right: String,
    val sort: Double,
    val item: NEUInternalName,
    val hover: List<String> = emptyList(),
    val highlightsOnHoverSlots: List<Int> = emptyList(),
)
