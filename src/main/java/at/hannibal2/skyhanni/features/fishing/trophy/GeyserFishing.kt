package at.hannibal2.skyhanni.features.fishing.trophy

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.ReceiveParticleEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.features.fishing.FishingApi
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LocationUtils.distanceTo
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayerIgnoreY
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.SkyHanniVec3d
import at.hannibal2.skyhanni.utils.RenderUtils.drawFilledBoundingBoxNea
import at.hannibal2.skyhanni.utils.SpecialColor.toSpecialColor
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.EnumParticleTypes

@SkyHanniModule
object GeyserFishing {
    private val config get() = SkyHanniMod.feature.fishing.trophyFishing.geyserOptions

    private val geyserOffset = SkyHanniVec3d(0.1f, 0.6f, 0.1f)

    private var geyser: SkyHanniVec3d? = null
    private var geyserBox: AxisAlignedBB? = null

    @HandleEvent(priority = HandleEvent.LOW, receiveCancelled = true)
    fun onReceiveParticle(event: ReceiveParticleEvent) {
        if (!shouldProcessParticles()) return
        with(event) {
            if (type != EnumParticleTypes.CLOUD || count != 15 || speed != 0.05f || offset != geyserOffset) return
        }
        geyser = event.location
        val potentialGeyser = geyser ?: return

        geyserBox = AxisAlignedBB(
            potentialGeyser.x - 2, 118.0 - 0.1, potentialGeyser.z - 2,
            potentialGeyser.x + 2, 118.0 - 0.09, potentialGeyser.z + 2,
        )

        if (config.hideParticles && FishingApi.bobber != null) {
            hideGeyserParticles(event)
        }
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        geyser = null
        geyserBox = null
    }

    @HandleEvent(onlyOnIsland = IslandType.CRIMSON_ISLE)
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!config.drawBox) return
        val geyserBox = geyserBox ?: return
        val geyser = geyser ?: return
        if (geyser.distanceToPlayerIgnoreY() > 96) return
        if (config.onlyWithRod && !FishingApi.holdingLavaRod) return

        val color = config.boxColor.toSpecialColor()
        event.drawFilledBoundingBoxNea(geyserBox, color)
    }

    private fun hideGeyserParticles(event: ReceiveParticleEvent) {
        val bobber = FishingApi.bobber ?: return
        val geyser = geyser ?: return

        if (bobber.distanceTo(event.location) < 3 && bobber.distanceTo(geyser) < 3) {
            event.cancel()
        }
    }

    private fun shouldProcessParticles() =
        IslandType.CRIMSON_ISLE.isInIsland() && LorenzUtils.skyBlockArea == "Blazing Volcano" && (config.hideParticles || config.drawBox)
}
