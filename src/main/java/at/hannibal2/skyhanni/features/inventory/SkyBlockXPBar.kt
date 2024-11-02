package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import net.minecraft.client.Minecraft
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object SkyBlockXPBar {

    var xp = 0f

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        xp = Math.random().toFloat()
    }

    @SubscribeEvent
    fun onRenderScoreboard(event: RenderGameOverlayEvent.Pre) {
        if (!isEnabled()) return
        if (event.type != RenderGameOverlayEvent.ElementType.EXPERIENCE) return
        Minecraft.getMinecraft().thePlayer.setXPStats(xp, 100, 69)
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && SkyHanniMod.Companion.feature.inventory.skyblockXpBar

}
