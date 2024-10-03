package at.hannibal2.skyhanni.features.slayer.blaze

import at.hannibal2.skyhanni.utils.LorenzColor

enum class HellionShield(
    val formattedName: String,
    val cleanName: String,
    val color: LorenzColor,
    val chatColor: String = color.getChatColor(),
    var active: Boolean = false,
) {

    AURIC("§e§lAURIC", "Auric", LorenzColor.YELLOW),
    ASHEN("§8§lASHEN", "Ashen", LorenzColor.DARK_GRAY),
    SPIRIT("§f§lSPIRIT", "Spirit", LorenzColor.WHITE),
    CRYSTAL("§b§lCRYSTAL", "Crystal", LorenzColor.AQUA),
}
