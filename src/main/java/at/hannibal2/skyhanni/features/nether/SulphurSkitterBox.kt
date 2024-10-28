package at.hannibal2.skyhanni.features.nether

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.features.crimsonisle.SulphurSkitterBoxConfig
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.features.fishing.FishingAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.BlockUtils.getBlockAt
import at.hannibal2.skyhanni.utils.ColorUtils.toChromaColor
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.RenderUtils.expandBlock
import at.hannibal2.skyhanni.utils.SpecialColor
import at.hannibal2.skyhanni.utils.toLorenzVec
import net.minecraft.init.Blocks
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

@SkyHanniModule
object SulphurSkitterBox {

    private val config get() = SkyHanniMod.feature.fishing.trophyFishing.sulphurSkitterBox
    private var spongeBlocks = listOf<BlockPos>()
    private var closestBlock: BlockPos? = null
    private const val RADIUS = 4

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return
        if (event.isMod(5)) {
            closestBlock = getClosestBlockToPlayer()
        }
        if (event.repeatSeconds(1)) {
            val location = LocationUtils.playerLocation()
            val from = location.add(-20, -20, -20).toBlockPos()
            val to = location.add(20, 20, 20).toBlockPos()

            spongeBlocks = BlockPos.getAllInBox(from, to).filter {
                val loc = it.toLorenzVec()
                loc.getBlockAt() == Blocks.sponge && loc.distanceToPlayer() <= 15
            }.filter {
                val pos1 = it.add(-RADIUS, -RADIUS, -RADIUS)
                val pos2 = it.add(RADIUS, RADIUS, RADIUS)
                BlockPos.getAllInBox(pos1, pos2).any { pos ->
                    pos.toLorenzVec().getBlockAt() in FishingAPI.lavaBlocks
                }
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        spongeBlocks = emptyList()
    }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!isEnabled()) return
        closestBlock?.let {
            if (it.toLorenzVec().distanceToPlayer() >= 50) return
            val pos1 = it.add(-RADIUS, -RADIUS, -RADIUS)
            val pos2 = it.add(RADIUS, RADIUS, RADIUS)
            val axis = AxisAlignedBB(pos1, pos2).expandBlock()

            drawBox(axis, event.partialTicks)
        }
    }

    private fun getClosestBlockToPlayer(): BlockPos? {
        return spongeBlocks.minByOrNull { it.toLorenzVec().distanceToPlayer() }
    }

    private fun drawBox(axis: AxisAlignedBB, partialTicks: Float) {
        val color = config.boxColor.toChromaColor()
        when (config.boxType) {
            SulphurSkitterBoxConfig.BoxType.FULL -> {
                RenderUtils.drawFilledBoundingBoxNea(
                    axis,
                    color,
                    partialTicks = partialTicks,
                    renderRelativeToCamera = false
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

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(24, "crimsonIsle.sulphurSkitterBoxConfig", "fishing.trophyFishing.sulphurSkitterBox")
    }
}
