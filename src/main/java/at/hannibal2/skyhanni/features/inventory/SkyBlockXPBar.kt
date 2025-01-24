package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.SkyBlockXPAPI
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.inAnyIsland
import net.minecraft.client.Minecraft
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object SkyBlockXPBar {
    private val config get() = SkyHanniMod.feature.misc
    private var cache: OriginalValues? = null

    private class OriginalValues(val currentXp: Float, val maxXp: Int, val level: Int)

    @SubscribeEvent
    fun onRenderExperienceBar(event: RenderGameOverlayEvent.Pre) {
        if (!isEnabled()) return
        if (event.type != RenderGameOverlayEvent.ElementType.EXPERIENCE) return
        val (level, xp) = SkyBlockXPAPI.levelXpPair ?: return

        with(Minecraft.getMinecraft().thePlayer) {
            cache = OriginalValues(experience, experienceTotal, experienceLevel)
            setXPStats(xp / 100f, 100, level)
        }
    }

    @SubscribeEvent
    fun onRenderExperienceBarPost(event: RenderGameOverlayEvent.Post) {
        if (event.type != RenderGameOverlayEvent.ElementType.EXPERIENCE) return
        with(cache ?: return) {
            Minecraft.getMinecraft().thePlayer.setXPStats(currentXp, maxXp, level)
            cache = null
        }
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && !inAnyIsland(IslandType.THE_RIFT, IslandType.CATACOMBS) && config.skyblockXpBar
}
