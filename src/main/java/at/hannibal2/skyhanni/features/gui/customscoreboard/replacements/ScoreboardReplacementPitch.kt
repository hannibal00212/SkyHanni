package at.hannibal2.skyhanni.features.gui.customscoreboard.replacements

import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import net.minecraft.client.Minecraft

object ScoreboardReplacementPitch : ScoreboardReplacement() {
    override val trigger = "%pitch%"
    override val name = "Pitch"
    override fun replacement() = Minecraft.getMinecraft().thePlayer.rotationPitch.roundTo(2).toString()
}
