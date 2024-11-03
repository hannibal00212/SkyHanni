package at.hannibal2.skyhanni.features.gui.customscoreboard.replacements

import at.hannibal2.skyhanni.utils.LorenzUtils.round
import net.minecraft.client.Minecraft

object ScoreboardReplacementYCoord : ScoreboardReplacement() {
    override val trigger = "%y%"
    override val name = "Y-Coordinate"
    override fun replacement(): String = Minecraft.getMinecraft().thePlayer.posY.round(2).toString()
}
