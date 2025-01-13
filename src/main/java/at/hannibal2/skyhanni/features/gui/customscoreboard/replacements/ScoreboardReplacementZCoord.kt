package at.hannibal2.skyhanni.features.gui.customscoreboard.replacements

import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import net.minecraft.client.Minecraft

object ScoreboardReplacementZCoord : ScoreboardReplacement() {
    override val trigger = "%z%"
    override val name = "Z-Coordinate"
    override fun replacement() = Minecraft.getMinecraft().thePlayer.posZ.roundTo(2).toString()
}
