package at.hannibal2.skyhanni.features.nether

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.features.crimsonisle.SulphurSkitterBoxConfig
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.features.fishing.FishingAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.BlockUtils.getBlockAt
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.RenderUtils.expandBlock
import at.hannibal2.skyhanni.utils.SpecialColor.toSpecialColor
import at.hannibal2.skyhanni.utils.toLorenzVec
import net.minecraft.init.Blocks
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object SulphurSkitterBox {

    private val config get() = SkyHanniMod.feature.fishing.trophyFishing.sulphurSkitterBox
    private var spongeLocations = listOf<LorenzVec>()
    private var closestSponge: LorenzVec? = null
    private var renderBox: AxisAlignedBB? = null
    private const val RADIUS = 4

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return
        if (event.repeatSeconds(1)) {
            calculateSpongeLocations()
        }
        if (event.isMod(5)) {
            calculateClosestSponge()
        }
    }

    private fun calculateClosestSponge() {
        val location = spongeLocations.minByOrNull { it.distanceToPlayer() }
        if (location == closestSponge) return
        closestSponge = location
        renderBox = location?.let {
            val pos1 = it.add(-RADIUS, -RADIUS, -RADIUS)
            val pos2 = it.add(RADIUS + 1, RADIUS + 1, RADIUS + 1)
            pos1.axisAlignedTo(pos2).expandBlock()
        }
    }

    private fun calculateSpongeLocations() {
        val location = LocationUtils.playerLocation()
        val from = location.add(-15, -15, -15).toBlockPos()
        val to = location.add(15, 15, 15).toBlockPos()

        spongeLocations = BlockPos.getAllInBox(from, to).filter {
            val loc = it.toLorenzVec()
            loc.getBlockAt() == Blocks.sponge && loc.distanceToPlayer() <= 15
        }.filter {
            val pos1 = it.add(-RADIUS, -RADIUS, -RADIUS)
            val pos2 = it.add(RADIUS, RADIUS, RADIUS)
            BlockPos.getAllInBox(pos1, pos2).any { pos ->
                pos.toLorenzVec().getBlockAt() in FishingAPI.lavaBlocks
            }
        }.map { it.toLorenzVec() }
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        spongeLocations = emptyList()
        closestSponge = null
        renderBox = null
    }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!isEnabled()) return
        val location = closestSponge ?: return
        if (location.distanceToPlayer() >= 50) return
        renderBox?.let { drawBox(it, event.partialTicks) }
    }

    private fun drawBox(axis: AxisAlignedBB, partialTicks: Float) {
        val color = config.boxColor.toSpecialColor()
        when (config.boxType) {
            SulphurSkitterBoxConfig.BoxType.FULL -> {
                RenderUtils.drawFilledBoundingBoxNea(
                    axis,
                    color,
                    partialTicks = partialTicks,
                    renderRelativeToCamera = false,
                )
            }

            SulphurSkitterBoxConfig.BoxType.WIREFRAME -> {
                RenderUtils.drawWireframeBoundingBoxNea(axis, color, partialTicks)
            }

            else -> {
                RenderUtils.drawWireframeBoundingBoxNea(axis, color, partialTicks)
            }
        }
    }

    fun isEnabled() =
        IslandType.CRIMSON_ISLE.isInIsland() && config.enabled && (!config.onlyWithRods || FishingAPI.holdingLavaRod)

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(24, "crimsonIsle.sulphurSkitterBoxConfig", "fishing.trophyFishing.sulphurSkitterBox")
    }
}
