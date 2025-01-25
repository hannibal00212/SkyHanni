package at.hannibal2.skyhanni.features.mining.crystalhollows

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils.getCorners
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.RenderUtils.expandBlock
import at.hannibal2.skyhanni.utils.RenderUtils.inflateBlock
import net.minecraft.client.Minecraft
import net.minecraft.util.AxisAlignedBB
import java.awt.Color

@SkyHanniModule
object CrystalHollowsWalls {

    private val config get() = SkyHanniMod.feature.mining.crystalHollowsAreaWalls

    private enum class Area(val color: Color) {
        MITHRIL(LorenzColor.GREEN.addOpacity(60)),
        PRECURSOR(LorenzColor.BLUE.addOpacity(60)),
        JUNGLE(LorenzColor.LIGHT_PURPLE.addOpacity(60)),
        GOBLIN(LorenzColor.GOLD.addOpacity(60)),
        HEAT(LorenzColor.RED.addOpacity(60)),
        NUCLEUS(LorenzColor.WHITE.addOpacity(60)),
    }

    private const val EXPAND_TIMES = 20

    // Heat is active at Y=64.0 and below as of SkyBlock 0.20.1. We draw the line
    // one above to accurately show whether the player is inside the Magma Fields.
    private const val HEAT_HEIGHT = 65.0
    private const val MAX_HEIGHT = 190.0

    private const val MIN_X = 0.0
    private const val MIDDLE_X = 513.0
    private const val MAX_X = 1024.0

    private const val MIN_Z = 0.0
    private const val MIDDLE_Z = 513.0
    private const val MAX_Z = 1024.0

    private val yViewOffset get() = -Minecraft.getMinecraft().thePlayer.getEyeHeight().toDouble()

    // Yes Hypixel has misaligned the nucleus
    private val nucleusBB = AxisAlignedBB(
        463.0, HEAT_HEIGHT, 460.0,
        560.0, MAX_HEIGHT, 563.0,
    )

    private val nucleusBBInflate = nucleusBB.inflateBlock(EXPAND_TIMES)
    private val nucleusBBExpand = nucleusBB.expandBlock(EXPAND_TIMES)

    private val nucleusBBOffsetY get() = nucleusBB.offset(0.0, yViewOffset, 0.0)

    private fun Double.shiftPX() = this + LorenzVec.expandVector.x * EXPAND_TIMES
    private fun Double.shiftNX() = this - LorenzVec.expandVector.x * EXPAND_TIMES

    private fun Double.shiftPY() = this + LorenzVec.expandVector.y * EXPAND_TIMES
    private fun Double.shiftNY() = this - LorenzVec.expandVector.y * EXPAND_TIMES

    private fun Double.shiftPZ() = this + LorenzVec.expandVector.z * EXPAND_TIMES
    private fun Double.shiftNZ() = this - LorenzVec.expandVector.z * EXPAND_TIMES

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!isEnabled()) return
        val position = RenderUtils.getViewerPos(event.partialTicks)
        when {
            position.y < HEAT_HEIGHT + yViewOffset -> drawHeat(event)
            nucleusBBOffsetY.isVecInside(position.toVec3()) -> {
                if (!config.nucleus) return
                drawNucleus(event)
            }
            position.x > MIDDLE_X -> {
                if (position.z > MIDDLE_Z) {
                    drawPrecursor(event)
                } else {
                    drawMithril((event))
                }
            }
            else -> {
                if (position.z > MIDDLE_Z) {
                    drawGoblin(event)
                } else {
                    drawJungle(event)
                }
            }
        }
    }

    private fun drawGoblin(event: SkyHanniRenderWorldEvent) = RenderUtils.QuadDrawer.draw3D(event.partialTicks) {
        drawArea(true, false, Area.JUNGLE.color, Area.PRECURSOR.color)
    }

    private fun drawJungle(event: SkyHanniRenderWorldEvent) = RenderUtils.QuadDrawer.draw3D(event.partialTicks) {
        drawArea(true, true, Area.GOBLIN.color, Area.MITHRIL.color)
    }

    private fun drawPrecursor(event: SkyHanniRenderWorldEvent) = RenderUtils.QuadDrawer.draw3D(event.partialTicks) {
        drawArea(false, false, Area.MITHRIL.color, Area.GOBLIN.color)
    }

    private fun drawMithril(event: SkyHanniRenderWorldEvent) = RenderUtils.QuadDrawer.draw3D(event.partialTicks) {
        drawArea(false, true, Area.PRECURSOR.color, Area.JUNGLE.color)
    }

    private fun drawHeat(event: SkyHanniRenderWorldEvent) = RenderUtils.QuadDrawer.draw3D(event.partialTicks) {
        val heatHeight = HEAT_HEIGHT.shiftNY()
        draw(
            LorenzVec(nucleusBB.minX, heatHeight, nucleusBB.minZ),
            LorenzVec(nucleusBB.maxX, heatHeight, nucleusBB.minZ),
            LorenzVec(nucleusBB.minX, heatHeight, nucleusBB.maxZ),
            Area.NUCLEUS.color,
        )

        drawHeatAreaForHeat(false, false, Area.PRECURSOR.color, heatHeight)
        drawHeatAreaForHeat(false, true, Area.MITHRIL.color, heatHeight)
        drawHeatAreaForHeat(true, false, Area.GOBLIN.color, heatHeight)
        drawHeatAreaForHeat(true, true, Area.JUNGLE.color, heatHeight)
    }

    private fun drawNucleus(event: SkyHanniRenderWorldEvent) {
        val (southEastCorner, southWestCorner, northWestCorner, northEastCorner) = nucleusBBInflate.getCorners(nucleusBBInflate.minY)
        val (southEastTopCorner, southWestTopCorner, northWestTopCorner, northEastTopCorner) =
            nucleusBBInflate.getCorners(nucleusBBInflate.maxY)

        RenderUtils.QuadDrawer.draw3D(event.partialTicks) {
            draw(
                southEastCorner,
                southWestCorner,
                northEastCorner,
                Area.HEAT.color,
            )
            draw(
                southEastCorner,
                southEastTopCorner,
                LorenzVec(nucleusBBInflate.minX, nucleusBBInflate.minY, MIDDLE_Z),
                Area.JUNGLE.color,
            )
            draw(
                southEastCorner,
                southEastTopCorner,
                LorenzVec(MIDDLE_X, nucleusBBInflate.minY, nucleusBBInflate.minZ),
                Area.JUNGLE.color,
            )
            draw(
                northWestCorner,
                northWestTopCorner,
                LorenzVec(nucleusBBInflate.maxX, nucleusBBInflate.minY, MIDDLE_Z),
                Area.PRECURSOR.color,
            )
            draw(
                northWestCorner,
                northWestTopCorner,
                LorenzVec(MIDDLE_X, nucleusBBInflate.minY, nucleusBBInflate.maxZ),
                Area.PRECURSOR.color,
            )
            draw(
                southWestCorner,
                southWestTopCorner,
                LorenzVec(nucleusBBInflate.minX, nucleusBBInflate.minY, MIDDLE_Z),
                Area.GOBLIN.color,
            )
            draw(
                southWestCorner,
                southWestTopCorner,
                LorenzVec(MIDDLE_X, nucleusBBInflate.minY, nucleusBBInflate.maxZ),
                Area.GOBLIN.color,
            )
            draw(
                northEastCorner,
                northEastTopCorner,
                LorenzVec(nucleusBBInflate.maxX, nucleusBBInflate.minY, MIDDLE_Z),
                Area.MITHRIL.color,
            )
            draw(
                northEastCorner,
                northEastTopCorner,
                LorenzVec(MIDDLE_X, nucleusBBInflate.minY, nucleusBBInflate.minZ),
                Area.MITHRIL.color,
            )
        }
    }

    private fun RenderUtils.QuadDrawer.drawArea(
        isMinXEsleMaxX: Boolean,
        isMinZElseMaxZ: Boolean,
        color1: Color,
        color2: Color,
    ) {
        val nucleusX = if (isMinXEsleMaxX) nucleusBBExpand.minX else nucleusBBExpand.maxX
        val middleX = if (isMinXEsleMaxX) MIDDLE_X.shiftNX() else MIDDLE_X.shiftPX()
        val x = if (isMinXEsleMaxX) MIN_X else MAX_X

        val nucleusZ = if (isMinZElseMaxZ) nucleusBBExpand.minZ else nucleusBBExpand.maxZ
        val middleZ = if (isMinZElseMaxZ) MIDDLE_Z.shiftNZ() else MIDDLE_Z.shiftPZ()
        val z = if (isMinZElseMaxZ) MIN_Z else MAX_Z

        val heatHeight = HEAT_HEIGHT.shiftPY()

        val nucleusBase = LorenzVec(nucleusX, heatHeight, nucleusZ)

        val nucleusZSideBase = LorenzVec(middleX, heatHeight, nucleusZ)
        val nucleusXSideBase = LorenzVec(nucleusX, heatHeight, middleZ)

        drawHeatArea(
            Area.HEAT.color,
            heatHeight,
            nucleusX,
            middleX,
            x,
            nucleusZ,
            middleZ,
            z,
        )
        draw(
            nucleusXSideBase,
            LorenzVec(nucleusX, MAX_HEIGHT, middleZ),
            LorenzVec(x, heatHeight, middleZ),
            color1,
        )
        draw(
            nucleusZSideBase,
            LorenzVec(middleX, MAX_HEIGHT, nucleusZ),
            LorenzVec(middleX, heatHeight, z),
            color2,
        )
        draw(
            nucleusXSideBase,
            nucleusBase,
            LorenzVec(nucleusX, MAX_HEIGHT, middleZ),
            Area.NUCLEUS.color,
        )
        draw(
            nucleusZSideBase,
            nucleusBase,
            LorenzVec(middleX, MAX_HEIGHT, nucleusZ),
            Area.NUCLEUS.color,
        )
    }

    private fun RenderUtils.QuadDrawer.drawHeatAreaForHeat(
        isMinXEsleMaxX: Boolean,
        isMinZElseMaxZ: Boolean,
        color: Color,
        heatHeight: Double,
    ) = this.drawHeatArea(
        color,
        heatHeight,
        nucleusX = if (isMinXEsleMaxX) nucleusBB.minX else nucleusBB.maxX,
        middleX = MIDDLE_X,
        x = if (isMinXEsleMaxX) MIN_X else MAX_X,
        nucleusZ = if (isMinZElseMaxZ) nucleusBB.minZ else nucleusBB.maxZ,
        middleZ = MIDDLE_X,
        z = if (isMinZElseMaxZ) MIN_Z else MAX_Z,
    )

    private fun RenderUtils.QuadDrawer.drawHeatArea(
        color: Color,
        heatHeight: Double,
        nucleusX: Double,
        middleX: Double,
        x: Double,
        nucleusZ: Double,
        middleZ: Double,
        z: Double,
    ) {
        val nucleusBase = LorenzVec(nucleusX, heatHeight, nucleusZ)

        draw(
            nucleusBase,
            LorenzVec(nucleusX, heatHeight, z),
            LorenzVec(middleX, heatHeight, nucleusZ),
            color,
        )
        draw(
            nucleusBase,
            LorenzVec(x, heatHeight, nucleusZ),
            LorenzVec(nucleusX, heatHeight, middleZ),
            color,
        )
        draw(
            nucleusBase,
            LorenzVec(x, heatHeight, nucleusZ),
            LorenzVec(nucleusX, heatHeight, z),
            color,
        )
    }

    private fun isEnabled() = config.enabled && IslandType.CRYSTAL_HOLLOWS.isInIsland()
}
