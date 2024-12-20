package at.hannibal2.skyhanni.features.event.hoppity

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.skyblock.GraphAreaChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.RenderUtils.drawFilledBoundingBoxNea
import at.hannibal2.skyhanni.utils.RenderUtils.expandBlock
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object NucleusBarriersBox {
    private val config get() = SkyHanniMod.feature.mining.crystalHighlighter

    private var inNucleus = false

    private enum class Crystal(val color: LorenzColor, val boundingBox: AxisAlignedBB) {
        AMBER(
            LorenzColor.GOLD,
            AxisAlignedBB(
                BlockPos(474.0, 124.0, 524.0),
                BlockPos(485.0, 111.0, 535.0),
            ).expandBlock(),
        ),
        AMETHYST(
            LorenzColor.DARK_PURPLE,
            AxisAlignedBB(
                BlockPos(474.0, 124.0, 492.0),
                BlockPos(485.0, 111.0, 503.0),
            ).expandBlock(),
        ),
        TOPAZ(
            LorenzColor.YELLOW,
            AxisAlignedBB(
                BlockPos(508.0, 124.0, 473.0),
                BlockPos(519.0, 111.0, 484.0),
            ).expandBlock(),
        ),
        JADE(
            LorenzColor.GREEN,
            AxisAlignedBB(
                BlockPos(542.0, 124.0, 492.0),
                BlockPos(553.0, 111.0, 503.0),
            ).expandBlock(),
        ),
        SAPPHIRE(
            LorenzColor.BLUE,
            AxisAlignedBB(
                BlockPos(542.0, 124.0, 524.0),
                BlockPos(553.0, 111.0, 535.0),
            ).expandBlock(),
        ),
    }

    @HandleEvent
    fun onAreaChange(event: GraphAreaChangeEvent) {
        inNucleus = event.area == "Crystal Nucleus"
    }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!isEnabled()) return

        Crystal.entries.forEach { crystal ->
            event.drawFilledBoundingBoxNea(
                crystal.boundingBox,
                crystal.color.addOpacity(config.opacity),
                renderRelativeToCamera = false,
            )
        }
    }

    private fun isEnabled() =
        IslandType.CRYSTAL_HOLLOWS.isInIsland() && (HoppityAPI.isHoppityEvent() || !config.onlyDuringHoppity) && config.enabled && inNucleus
}
