package at.hannibal2.skyhanni.features.gui.customscoreboard.replacements

import at.hannibal2.skyhanni.utils.LorenzUtils.round
import net.minecraft.client.Minecraft

object ScoreboardReplacementYaw : ScoreboardReplacement() {
    override val trigger = "%yaw%"
    override val name = "Direction"
    override fun replacement(): String = normalizeYaw(Minecraft.getMinecraft().thePlayer.rotationYaw).round(2).toString()

    private fun normalizeYaw(yaw: Float): Float {
        var result = yaw % 360
        if (result > 180) {
            result -= 360
        } else if (result <= -180) {
            result += 360
        }
        return result
    }
}
