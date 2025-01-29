package at.hannibal2.skyhanni.features.slayer.blaze

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.entity.item.EntityArmorStand

@SkyHanniModule
object FirePillarDisplay {

    private val config get() = SkyHanniMod.feature.slayer.blazes

    /**
     * REGEX-TEST: §6§l2s §c§l8 hits
     */
    private val entityNamePattern by RepoPattern.pattern(
        "slayer.blaze.firepillar.entityname",
        "§6§l(?<seconds>.*)s §c§l8 hits",
    )

    private var display = ""

    @HandleEvent
    fun onTick(event: SkyHanniTickEvent) {
        if (!isEnabled()) return

        val entityNames = EntityUtils.getEntities<EntityArmorStand>().map { it.name }
        val seconds = entityNamePattern.firstMatcher(entityNames) { group("seconds") }
        display = seconds?.let { "§cFire Pillar: §b${it}s" }.orEmpty()
    }

    @HandleEvent
    fun onRenderOverlay(event: GuiRenderEvent) {
        if (!isEnabled()) return

        config.firePillarDisplayPosition.renderString(display, posLabel = "Fire Pillar")
    }

    fun isEnabled() = IslandType.CRIMSON_ISLE.isInIsland() && config.firePillarDisplay
}
