package at.hannibal2.skyhanni.features.gui.customscoreboard.replacements

import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import net.minecraft.client.Minecraft

object ScoreboardReplacementXCoord : ScoreboardReplacement() {
    override val trigger = "%x%"
    override val name = "X-Coordinate"
    override fun replacement(): String = Minecraft.getMinecraft().thePlayer.posX.roundTo(2).toString()
}
