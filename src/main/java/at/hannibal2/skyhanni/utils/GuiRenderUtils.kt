package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.config.features.skillprogress.SkillProgressBarConfig
import at.hannibal2.skyhanni.features.chroma.ChromaShaderManager
import at.hannibal2.skyhanni.features.chroma.ChromaType
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
import kotlin.math.ceil
import kotlin.math.min

/**
 * Some functions taken from NotEnoughUpdates
 */
// TODO cleanup of redundant functions
object GuiRenderUtils {

    fun drawStringCentered(str: String?, fr: FontRenderer, x: Float, y: Float, shadow: Boolean, color: Int) {
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

    fun drawTwoLineString(str: String, x: Float, y: Float) {
        val desiredSplitIndex = str.length / 2
        var splitIndex = -1
        var lastColorCode = ""

        for (i in desiredSplitIndex downTo 0) {
            if (str[i] == ' ') {
                splitIndex = i
                break
            }
        }

        if (splitIndex == -1) {
            splitIndex = desiredSplitIndex
        }
        for (i in 0 until desiredSplitIndex) {
            if (str[i] == '§' && i + 1 < str.length) {
                lastColorCode = str.substring(i, i + 2)
            }
        }

        val firstString = str.substring(0, splitIndex).trim()
        val secondString = lastColorCode + str.substring(splitIndex).trim()

        Minecraft.getMinecraft().fontRendererObj.drawString(firstString, x, y - 5, 0xffffff, true)
        Minecraft.getMinecraft().fontRendererObj.drawString(secondString, x, y + 5, 0xffffff, true)
    }

    fun drawStringCentered(str: String?, x: Int, y: Int) {
        drawStringCentered(
            str, Minecraft.getMinecraft().fontRendererObj, x.toFloat(), y.toFloat(), true, 0xffffff,
        )
    }

    fun drawStringCentered(str: String?, x: Float, y: Float) {
        drawStringCentered(str, x.toInt(), y.toInt())
    }

    fun renderItemStack(item: ItemStack, x: Int, y: Int) {
        val itemRender = Minecraft.getMinecraft().renderItem
        RenderHelper.enableGUIStandardItemLighting()
        itemRender.zLevel = -145f
        itemRender.renderItemAndEffectIntoGUI(item, x, y)
        itemRender.zLevel = 0f
        RenderHelper.disableStandardItemLighting()
    }

    // Code taken and edited from NEU
    private fun drawTooltip(
        textLines: List<String>,
        mouseX: Int,
        mouseY: Int,
        screenHeight: Int,
        fr: FontRenderer,
    ) {
        if (textLines.isNotEmpty()) {
            val borderColor = StringUtils.getColor(textLines[0], 0x505000FF)

            GlStateManager.disableRescaleNormal()
            RenderHelper.disableStandardItemLighting()
            GlStateManager.disableLighting()
            GlStateManager.enableDepth()
            var tooltipTextWidth = 0

            for (textLine in textLines) {
                val textLineWidth: Int = fr.getStringWidth(textLine)
                if (textLineWidth > tooltipTextWidth) {
                    tooltipTextWidth = textLineWidth
                }
            }

            val tooltipX = mouseX + 12
            var tooltipY = mouseY - 12
            var tooltipHeight = 8

            if (textLines.size > 1) tooltipHeight += (textLines.size - 1) * 10 + 2
            GlStateManager.translate(0f, 0f, 100f)
            if (tooltipY + tooltipHeight + 6 > screenHeight) tooltipY = screenHeight - tooltipHeight - 6
            // main background
            GuiScreen.drawRect(
                tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, -0xfeffff0,
            )

            // borders
            GuiScreen.drawRect(
                tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1, tooltipY + tooltipHeight + 3 - 1, borderColor,
            )

            GuiScreen.drawRect(
                tooltipX + tooltipTextWidth + 2,
                tooltipY - 3 + 1,
                tooltipX + tooltipTextWidth + 3,
                tooltipY + tooltipHeight + 3 - 1,
                borderColor,
            )

            GuiScreen.drawRect(
                tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY - 3 + 1, borderColor,
            )

            GuiScreen.drawRect(
                tooltipX - 3,
                tooltipY + tooltipHeight + 2,
                tooltipX + tooltipTextWidth + 3,
                tooltipY + tooltipHeight + 3,
                borderColor,
            )
            GlStateManager.translate(0f, 0f, -100f)
            GlStateManager.disableDepth()

            for (line in textLines) {
                fr.drawString(line, tooltipX.toFloat(), tooltipY.toFloat(), 0xffffff, true)

                tooltipY += if (line == textLines[0]) 12 else 10
            }

            GlStateManager.enableDepth()
            GlStateManager.enableLighting()
            GlStateManager.enableRescaleNormal()
            RenderHelper.enableStandardItemLighting()
        }
        GlStateManager.disableLighting()
    }

    fun drawTooltip(textLines: List<String>, mouseX: Int, mouseY: Int, screenHeight: Int) {
        drawTooltip(textLines, mouseX, mouseY, screenHeight, Minecraft.getMinecraft().fontRendererObj)
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

    private fun barColorGradient(double: Double): Int {
        var newDouble = (double - .5) * 2
        if (newDouble < 0) newDouble = 0.0
        return Color((255 * (1 - newDouble)).toInt(), (255 * newDouble).toInt(), 0).rgb
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

    // Taken and edited from NEU <- it's broken
    fun renderTexturedBar(
        x: Float,
        y: Float,
        xSize: Float,
        completed: Float,
        color: Color,
        useChroma: Boolean,
        texture: SkillProgressBarConfig.TexturedBar.UsedTexture,
        height: Float,
    ) {
        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, 0f)
        val w = xSize.toInt()
        val w_2 = w / 2
        val k = min(w.toDouble(), ceil((completed * w).toDouble())).toInt()
        val vanilla = texture == SkillProgressBarConfig.TexturedBar.UsedTexture.MATCH_PACK
        val vMinEmpty = if (vanilla) 64 / 256f else 0f
        val vMaxEmpty = if (vanilla) 69 / 256f else .5f
        val vMinFilled = if (vanilla) 69 / 256f else .5f
        val vMaxFilled = if (vanilla) 74 / 256f else 1f

        if (useChroma) {
            ChromaShaderManager.begin(ChromaType.TEXTURED)
            GlStateManager.color(
                Color.LIGHT_GRAY.darker().red / 255f,
                Color.LIGHT_GRAY.darker().green / 255f,
                Color.LIGHT_GRAY.darker().blue / 255f,
                1f,
            )
        } else {
            GlStateManager.color(color.darker().red / 255f, color.darker().green / 255f, color.darker().blue / 255f, 1f)
        }

        drawTexturedRect(x, y, w_2.toFloat(), height, 0f, w_2 / xSize, vMinEmpty, vMaxEmpty, GL11.GL_NEAREST)
        drawTexturedRect(x + w_2, y, w_2.toFloat(), height, 1 - w_2 / xSize, 1f, vMinEmpty, vMaxEmpty, GL11.GL_NEAREST)

        if (useChroma) {
            GlStateManager.color(Color.WHITE.red / 255f, Color.WHITE.green / 255f, Color.WHITE.blue / 255f, 1f)
        } else {
            GlStateManager.color(color.red / 255f, color.green / 255f, color.blue / 255f, 1f)
        }

        if (k > 0) {
            val uMax = w_2.toDouble().coerceAtMost(k.toDouble() / xSize).toFloat()
            val width = w_2.coerceAtMost(k).toFloat()
            drawTexturedRect(x, y, width, height, 0f, uMax, vMinFilled, vMaxFilled, GL11.GL_NEAREST)
            if (completed > 0.5f) {
                drawTexturedRect(
                    x + w_2,
                    y,
                    (k - w_2).toFloat(),
                    height,
                    1 - w_2 / xSize,
                    1 + (k - w) / xSize,
                    vMinFilled,
                    vMaxFilled,
                    GL11.GL_NEAREST,
                )
            }
        }
        if (useChroma) {
            ChromaShaderManager.end()
        }
        GlStateManager.popMatrix()
    }

    /**@Mojang */
    fun drawGradientRect(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        startColor: Int,
        endColor: Int,
        zLevel: Double,
    ) {
        val f = (startColor shr 24 and 255).toFloat() / 255.0f
        val g = (startColor shr 16 and 255).toFloat() / 255.0f
        val h = (startColor shr 8 and 255).toFloat() / 255.0f
        val i = (startColor and 255).toFloat() / 255.0f
        val j = (endColor shr 24 and 255).toFloat() / 255.0f
        val k = (endColor shr 16 and 255).toFloat() / 255.0f
        val l = (endColor shr 8 and 255).toFloat() / 255.0f
        val m = (endColor and 255).toFloat() / 255.0f
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.disableAlpha()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0)
        GlStateManager.shadeModel(7425)
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        worldRenderer.pos(right.toDouble(), top.toDouble(), zLevel).color(g, h, i, f).endVertex()
        worldRenderer.pos(left.toDouble(), top.toDouble(), zLevel).color(g, h, i, f).endVertex()
        worldRenderer.pos(left.toDouble(), bottom.toDouble(), zLevel).color(k, l, m, j).endVertex()
        worldRenderer.pos(right.toDouble(), bottom.toDouble(), zLevel).color(k, l, m, j).endVertex()
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
    fun drawTexturedRect(
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
