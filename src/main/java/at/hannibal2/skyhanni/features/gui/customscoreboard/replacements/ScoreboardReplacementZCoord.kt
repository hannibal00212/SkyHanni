package at.hannibal2.skyhanni.features.gui.customscoreboard.replacements

import at.hannibal2.skyhanni.utils.LorenzUtils.round
import net.minecraft.client.Minecraft

object ScoreboardReplacementZCoord : ScoreboardReplacement() {
    override val trigger = "%z%"
    override val name = "Z-Coordinate"
    override fun replacement(): String = Minecraft.getMinecraft().thePlayer.posZ.round(2).toString()
}
