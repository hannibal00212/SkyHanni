package at.hannibal2.skyhanni.features.mining.crystalhollows

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayerSqIgnoreY
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.SkyHanniVec3d

@SkyHanniModule
object CrystalHollowsNamesInCore {

    private val config get() = SkyHanniMod.feature.mining
    private val coreLocations = mapOf(
        SkyHanniVec3d(550, 116, 550) to "§8Precursor Remnants",
        SkyHanniVec3d(552, 116, 474) to "§bMithril Deposits",
        SkyHanniVec3d(477, 116, 476) to "§aJungle",
        SkyHanniVec3d(474, 116, 554) to "§6Goblin Holdout",
    )

    private var showWaypoints = false

    @HandleEvent
    fun onTick(event: SkyHanniTickEvent) {
        if (!isEnabled()) return

        if (event.isMod(10)) {
            val center = SkyHanniVec3d(514.3, 106.0, 514.3)
            showWaypoints = center.distanceToPlayerSqIgnoreY() < 1100 && LocationUtils.playerLocation().y > 65
        }
    }

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!isEnabled()) return

        if (showWaypoints) {
            for ((location, name) in coreLocations) {
                event.drawDynamicText(location, name, 2.5)
            }
        }
    }

    fun isEnabled() = IslandType.CRYSTAL_HOLLOWS.isInIsland() && config.crystalHollowsNamesInCore
}
