package at.hannibal2.skyhanni.features.gui.customscoreboard.replacements

import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import net.minecraft.client.Minecraft

object ScoreboardReplacementYCoord : ScoreboardReplacement() {
    override val trigger = "%y%"
    override val name = "Y-Coordinate"
    override fun replacement(): String = Minecraft.getMinecraft().thePlayer.posY.roundTo(2).toString()
}
