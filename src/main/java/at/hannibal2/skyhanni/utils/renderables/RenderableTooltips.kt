package at.hannibal2.skyhanni.utils.renderables

import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.renderables.RenderableUtils.renderXAligned
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent

@SkyHanniModule
object RenderableTooltips {

    private var tooltip: DeferredTooltip? = null

    @SubscribeEvent
    fun onPostRenderTick(event: RenderTickEvent) {
        if (event.phase == TickEvent.Phase.START) {
            tooltip = null
        } else if (event.phase == TickEvent.Phase.END) {
            drawHoveringText()
        }
    }

    fun setTooltipForRender(
        tips: List<Renderable>,
        stack: ItemStack? = null,
        borderColor: LorenzColor? = null,
        snapsToTopIfToLong: Boolean = true,
        spacedTitle: Boolean = false
    ) {
        tooltip = DeferredTooltip(tips, stack, borderColor, snapsToTopIfToLong, spacedTitle)
    }

    private fun drawHoveringText() {
        val tooltip = tooltip ?: return
        val tips = tooltip.tips
        if (tips.isEmpty()) return

        val x = RenderUtils.getMouseX() + 12
        val y = RenderUtils.getMouseY() - if (tips.size > 1) 1 else -7
        val borderColorStart = tooltip.getBorderColor()
        val scaled = ScaledResolution(Minecraft.getMinecraft())
        val isSpacedTitle = tooltip.isSpacedTitle()

        val tooltipTextWidth = tips.maxOf { it.width }
        val tooltipHeight = tips.sumOf { it.height }

        val tooltipY = when {
            y < 16 -> 4 // Limit Top
            y + tooltipHeight > scaled.scaledHeight -> {
                if (tooltip.snapsToTopIfToLong && tooltipHeight + 8 > scaled.scaledHeight)
                    4 // Snap to Top if to Long
                else
                    scaled.scaledHeight - tooltipHeight - 6 // Limit Bottom
            }

            else -> {
                y - 10 // normal
            }
        }
        val tooltipX = if (x + tooltipTextWidth + 4 > scaled.scaledWidth) {
            scaled.scaledWidth - tooltipTextWidth - 4 // Limit Right
        } else {
            x // normal
        }

        GlStateManager.disableRescaleNormal()
        RenderHelper.disableStandardItemLighting()
        GlStateManager.enableDepth()

        val zLevel = 400f
        GlStateManager.translate(tooltipX.toFloat(), tooltipY.toFloat(), zLevel)

        RenderUtils.drawGradientRect(
            left = -3,
            top = -4,
            right = tooltipTextWidth + 2,
            bottom = -3,
        )
        RenderUtils.drawGradientRect(
            left = -3,
            top = tooltipHeight + 3,
            right = tooltipTextWidth + 2,
            bottom = tooltipHeight + 4,
        )
        RenderUtils.drawGradientRect(
            left = -3,
            top = -3,
            right = tooltipTextWidth + 2,
            bottom = tooltipHeight + 3,
        )
        RenderUtils.drawGradientRect(
            left = -4,
            top = -3,
            right = -3,
            bottom = tooltipHeight + 3,
        )
        RenderUtils.drawGradientRect(
            left = tooltipTextWidth + 2,
            top = -3,
            right = tooltipTextWidth + 3,
            bottom = tooltipHeight + 3,
        )
        val borderColorEnd = borderColorStart and 0xFEFEFE shr 1 or (borderColorStart and -0x1000000)
        RenderUtils.drawGradientRect(
            left = -3,
            top = -3 + 1,
            right = -3 + 1,
            bottom = tooltipHeight + 3 - 1,
            startColor = borderColorStart,
            endColor = borderColorEnd
        )
        RenderUtils.drawGradientRect(
            left = tooltipTextWidth + 1,
            top = -3 + 1,
            right = tooltipTextWidth + 2,
            bottom = tooltipHeight + 3 - 1,
            startColor = borderColorStart,
            endColor = borderColorEnd
        )
        RenderUtils.drawGradientRect(
            left = -3,
            top = -3,
            right = tooltipTextWidth + 2,
            bottom = -3 + 1,
            startColor = borderColorStart,
            endColor = borderColorStart
        )
        RenderUtils.drawGradientRect(
            left = -3,
            top = tooltipHeight + 2,
            right = tooltipTextWidth + 2,
            bottom = tooltipHeight + 3,
            startColor = borderColorEnd,
            endColor = borderColorEnd
        )
        GlStateManager.translate(-1f, -1f, 0f)

        var yTranslateSum = 0
        tips.forEachIndexed { index, line ->
            line.renderXAligned(tooltipX, tooltipY, tooltipTextWidth)
            var yShift = line.height
            if (index == 0 && isSpacedTitle) yShift += 2
            GlStateManager.translate(0f, yShift.toFloat(), 0f)
            yTranslateSum += yShift
        }

        GlStateManager.translate(-tooltipX.toFloat() + 1, -tooltipY.toFloat() + 1 + yTranslateSum.toFloat(), -zLevel)
        GlStateManager.enableLighting()
        RenderHelper.enableStandardItemLighting()
        GlStateManager.enableRescaleNormal()
        GlStateManager.disableLighting()
    }
}

private data class DeferredTooltip(
    val tips: List<Renderable>,
    val stack: ItemStack? = null,
    private val borderColor: LorenzColor? = null,
    val snapsToTopIfToLong: Boolean = true,
    private val spacedTitle: Boolean = false,
) {

    fun getBorderColor(): Int =
        (borderColor?.chatColorCode ?: stack?.getLore()?.lastOrNull()?.take(4)?.get(1))
            ?.let { Minecraft.getMinecraft().fontRendererObj.getColorCode(it) }
            ?: 0x505000FF

    fun isSpacedTitle(): Boolean {
        return spacedTitle
    }
}
