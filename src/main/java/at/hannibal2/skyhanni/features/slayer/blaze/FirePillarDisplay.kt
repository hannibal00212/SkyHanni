package at.hannibal2.skyhanni.features.slayer.blaze

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.RegexUtils.matchFirst
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object FirePillarDisplay {

    private val config get() = SkyHanniMod.feature.slayer.blazes

    /**
     * REGEX-TEST: §6§l2s §c§l8 hits
     */
    private val entityNamePattern by RepoPattern.pattern(
        "slayer.blaze.firepillar.entityname",
        "§6§l(?<seconds>.*)s §c§l8 hits"
    )

    private var display = ""

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return

        val seconds = EntityUtils.getEntities<EntityArmorStand>()
            .map { it.name }
            .matchFirst<String?>(entityNamePattern) {
                group("seconds")
            }

        display = seconds?.let {
            "§cFire Pillar: §b${seconds}s"
        }.orEmpty()
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent) {
        if (!isEnabled()) return

        config.firePillarDisplayPosition.renderString(display, posLabel = "Fire Pillar")
    }

    fun isEnabled() = IslandType.CRIMSON_ISLE.isInIsland() && config.firePillarDisplay
}
