package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.SkyBlockXPAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import net.minecraft.client.Minecraft
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object SkyBlockXPBar {
    private val config get() = SkyHanniMod.feature.misc

    @SubscribeEvent
    fun onRenderScoreboard(event: RenderGameOverlayEvent.Pre) {
        if (!isEnabled()) return
        if (event.type != RenderGameOverlayEvent.ElementType.EXPERIENCE) return
        val (level, xp) = SkyBlockXPAPI.levelXpPair ?: return
        Minecraft.getMinecraft().thePlayer.setXPStats(xp / 100f, 100, level)
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.skyblockXpBar
}
