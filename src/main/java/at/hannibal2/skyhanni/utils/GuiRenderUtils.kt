package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.utils.NumberUtil.fractionOf
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.RenderUtils.HorizontalAlignment
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.item.ItemStack
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL14
import java.awt.Color
import java.text.DecimalFormat

/**
 * Some functions taken from NotEnoughUpdates
 */
object GuiRenderUtils {

    private fun drawStringCentered(str: String?, fr: FontRenderer, x: Float, y: Float, shadow: Boolean, color: Int) {
        val strLen = fr.getStringWidth(str)
        val x2 = x - strLen / 2f
        val y2 = y - fr.FONT_HEIGHT / 2f
        GL11.glTranslatef(x2, y2, 0f)
        fr.drawString(str, 0f, 0f, color, shadow)
        GL11.glTranslatef(-x2, -y2, 0f)
    }

    fun drawString(str: String, x: Float, y: Float) {
        Minecraft.getMinecraft().fontRendererObj.drawString(str, x, y, 0xffffff, true)
    }

    fun drawString(str: String, x: Int, y: Int) {
        Minecraft.getMinecraft().fontRendererObj.drawString(str, x.toFloat(), y.toFloat(), 0xffffff, true)
    }

    fun drawStringCentered(str: String?, x: Int, y: Int) {
        drawStringCentered(
            str, Minecraft.getMinecraft().fontRendererObj, x.toFloat(), y.toFloat(), true, 0xffffff,
        )
    }

    private fun renderItemStack(item: ItemStack, x: Int, y: Int) {
        val itemRender = Minecraft.getMinecraft().renderItem
        RenderHelper.enableGUIStandardItemLighting()
        itemRender.zLevel = -145f
        itemRender.renderItemAndEffectIntoGUI(item, x, y)
        itemRender.zLevel = 0f
        RenderHelper.disableStandardItemLighting()
    }

    fun isPointInRect(x: Int, y: Int, left: Int, top: Int, width: Int, height: Int) =
        left <= x && x < left + width && top <= y && y < top + height

    fun getFarmingBar(
        label: String,
        tooltip: String,
        currentValue: Number,
        maxValue: Number,
        width: Int,
        textScale: Float = .7f,
    ): Renderable {
        val current = currentValue.toDouble().coerceAtLeast(0.0)
        val percent = current.fractionOf(maxValue)
        val scale = textScale.toDouble()
        return Renderable.hoverTips(
            Renderable.verticalContainer(
                listOf(
                    Renderable.string(label, scale = scale),
                    Renderable.fixedSizeLine(
                        listOf(
                            Renderable.string(
                                "§2${DecimalFormat("0.##").format(current)} / ${
                                    DecimalFormat(
                                        "0.##",
                                    ).format(maxValue)
                                }☘",
                                scale = scale, horizontalAlign = HorizontalAlignment.LEFT,
                            ),
                            Renderable.string(
                                "§2${(percent * 100).roundTo(1)}%",
                                scale = scale,
                                horizontalAlign = HorizontalAlignment.RIGHT,
                            ),
                        ),
                        width,
                    ),
                    Renderable.progressBar(percent, width = width),
                ),
            ),
            tooltip.split('\n').map { Renderable.string(it) },
        )
    }

    fun Int.darkenColor(): Int {
        val color = Color(this)
        return Color(color.red / 5, color.green / 5, color.blue / 5).rgb
    }

    fun drawScaledRec(left: Int, top: Int, right: Int, bottom: Int, color: Int, inverseScale: Float) {
        GuiScreen.drawRect(
            (left * inverseScale).toInt(),
            (top * inverseScale).toInt(),
            (right * inverseScale).toInt(),
            (bottom * inverseScale).toInt(),
            color,
        )
    }

    fun renderItemAndBackground(item: ItemStack, x: Int, y: Int, color: Int) {
        renderItemStack(item, x, y)
        GuiScreen.drawRect(x, y, x + 16, y + 16, color)
    }

    /** @Mojang */
    fun drawGradientRect(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        startColor: Int = -0xfeffff0,
        endColor: Int = -0xfeffff0,
        zLevel: Double = 0.0,
    ) {
        val (startAlpha, startRed, startGreen, startBlue) = ColorUtils.getQuad(startColor)
        val (endAlpha, endRed, endGreen, endBlue) = ColorUtils.getQuad(endColor)
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.disableAlpha()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0)
        GlStateManager.shadeModel(7425)
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        worldRenderer.pos(right.toDouble(), top.toDouble(), zLevel)
            .color(startRed, startGreen, startBlue, startAlpha).endVertex()
        worldRenderer.pos(left.toDouble(), top.toDouble(), zLevel)
            .color(startRed, startGreen, startBlue, startAlpha).endVertex()
        worldRenderer.pos(left.toDouble(), bottom.toDouble(), zLevel)
            .color(endRed, endGreen, endBlue, endAlpha).endVertex()
        worldRenderer.pos(right.toDouble(), bottom.toDouble(), zLevel)
            .color(endRed, endGreen, endBlue, endAlpha).endVertex()
        tessellator.draw()
        GlStateManager.shadeModel(7424)
        GlStateManager.disableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
    }

    fun drawTexturedRect(x: Float, y: Float) {
        with(ScaledResolution(Minecraft.getMinecraft())) {
            drawTexturedRect(x, y, scaledWidth.toFloat(), scaledHeight.toFloat(), filter = GL11.GL_NEAREST)
        }
    }

    fun drawTexturedRect(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        uMin: Float = 0f,
        uMax: Float = 1f,
        vMin: Float = 0f,
        vMax: Float = 1f,
        filter: Int = GL11.GL_NEAREST,
    ) {
        drawTexturedRect(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), uMin, uMax, vMin, vMax, filter)
    }

    // Taken from NEU
    private fun drawTexturedRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        uMin: Float = 0f,
        uMax: Float = 1f,
        vMin: Float = 0f,
        vMax: Float = 1f,
        filter: Int = GL11.GL_NEAREST,
    ) {
        GlStateManager.enableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA)

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter)

        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX)
        worldRenderer.pos(x.toDouble(), (y + height).toDouble(), 0.0).tex(uMin.toDouble(), vMax.toDouble()).endVertex()
        worldRenderer.pos((x + width).toDouble(), (y + height).toDouble(), 0.0).tex(uMax.toDouble(), vMax.toDouble()).endVertex()
        worldRenderer.pos((x + width).toDouble(), y.toDouble(), 0.0).tex(uMax.toDouble(), vMin.toDouble()).endVertex()
        worldRenderer.pos(x.toDouble(), y.toDouble(), 0.0).tex(uMin.toDouble(), vMin.toDouble()).endVertex()
        tessellator.draw()

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)

        GlStateManager.disableBlend()
    }
}
