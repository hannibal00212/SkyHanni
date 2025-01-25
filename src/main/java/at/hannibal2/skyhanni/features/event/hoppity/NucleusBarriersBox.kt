package at.hannibal2.skyhanni.features.event.hoppity

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.events.skyblock.GraphAreaChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.RenderUtils.drawFilledBoundingBoxNea
import at.hannibal2.skyhanni.utils.RenderUtils.expandBlock
import at.hannibal2.skyhanni.utils.SpecialColor.toSpecialColor
import io.github.notenoughupdates.moulconfig.observer.Property
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos

// TODO move into mining category and package
@SkyHanniModule
object NucleusBarriersBox {
    private val config get() = SkyHanniMod.feature.mining.crystalHighlighter
    private val colorConfig get() = config.colors

    private var inNucleus = false

    private enum class Crystal(
        val boundingBox: AxisAlignedBB,
        val configColorOption: Property<String>,
    ) {
        AMBER(
            AxisAlignedBB(
                BlockPos(474.0, 124.0, 524.0),
                BlockPos(485.0, 111.0, 535.0),
            ).expandBlock(),
            colorConfig.amber,
        ),
        AMETHYST(
            AxisAlignedBB(
                BlockPos(474.0, 124.0, 492.0),
                BlockPos(485.0, 111.0, 503.0),
            ).expandBlock(),
            colorConfig.amethyst,
        ),
        TOPAZ(
            AxisAlignedBB(
                BlockPos(508.0, 124.0, 473.0),
                BlockPos(519.0, 111.0, 484.0),
            ).expandBlock(),
            colorConfig.topaz,
        ),
        JADE(
            AxisAlignedBB(
                BlockPos(542.0, 124.0, 492.0),
                BlockPos(553.0, 111.0, 503.0),
            ).expandBlock(),
            colorConfig.jade,
        ),
        SAPPHIRE(
            AxisAlignedBB(
                BlockPos(542.0, 124.0, 524.0),
                BlockPos(553.0, 111.0, 535.0),
            ).expandBlock(),
            colorConfig.sapphire,
        ),
    }

    @HandleEvent
    fun onAreaChange(event: GraphAreaChangeEvent) {
        inNucleus = event.area == "Crystal Nucleus"
    }

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!isEnabled()) return

        Crystal.entries.forEach { crystal ->
            event.drawFilledBoundingBoxNea(
                crystal.boundingBox,
                crystal.configColorOption.get().toSpecialColor(),
                renderRelativeToCamera = false,
            )
        }
    }

    private fun isEnabled() =
        IslandType.CRYSTAL_HOLLOWS.isInIsland() && (HoppityApi.isHoppityEvent() || !config.onlyDuringHoppity) && config.enabled && inNucleus
}
