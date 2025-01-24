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
    private var playerExperience: Float = 0f
    private var playerExperienceTotal: Int = 0
    private var playerExperienceLevel: Int = 0

    @SubscribeEvent
    fun onRenderExperienceBar(event: RenderGameOverlayEvent.Pre) {
        if (!isEnabled()) return
        if (event.type != RenderGameOverlayEvent.ElementType.EXPERIENCE) return
        val thePlayer = Minecraft.getMinecraft().thePlayer
        playerExperience = thePlayer.experience
        playerExperienceTotal = thePlayer.experienceTotal
        playerExperienceLevel = thePlayer.experienceLevel
        val (level, xp) = SkyBlockXPAPI.levelXpPair ?: return

        thePlayer.setXPStats(xp / 100f, 100, level)
    }

    @SubscribeEvent
    fun onRenderExperienceBarPost(event: RenderGameOverlayEvent.Post) {
        if (!isEnabled()) return
        if (event.type != RenderGameOverlayEvent.ElementType.EXPERIENCE) return
        val thePlayer = Minecraft.getMinecraft().thePlayer
        thePlayer.setXPStats(playerExperience, playerExperienceTotal, playerExperienceLevel)
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && !inAnyIsland(IslandType.THE_RIFT, IslandType.CATACOMBS) && config.skyblockXpBar
}
