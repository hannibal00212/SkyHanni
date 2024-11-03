package at.hannibal2.skyhanni.features.gui.customscoreboard.replacements

import at.hannibal2.skyhanni.utils.LorenzUtils.round
import net.minecraft.client.Minecraft

object ScoreboardReplacementXCoord : ScoreboardReplacement() {
    override val trigger = "%x%"
    override val name = "X-Coordinate"
    override fun replacement(): String = Minecraft.getMinecraft().thePlayer.posX.round(2).toString()
}
