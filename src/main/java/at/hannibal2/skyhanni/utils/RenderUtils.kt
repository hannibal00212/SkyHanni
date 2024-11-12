package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.data.GuiEditManager
import at.hannibal2.skyhanni.data.GuiEditManager.getAbsX
import at.hannibal2.skyhanni.data.GuiEditManager.getAbsY
import at.hannibal2.skyhanni.data.model.Graph
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.GuiRenderItemEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.RenderGuiItemOverlayEvent
import at.hannibal2.skyhanni.features.misc.PatcherFixes
import at.hannibal2.skyhanni.features.misc.RoundedRectangleOutlineShader
import at.hannibal2.skyhanni.features.misc.RoundedRectangleShader
import at.hannibal2.skyhanni.features.misc.RoundedTextureShader
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.CollectionUtils.zipWithNext3
import at.hannibal2.skyhanni.utils.ColorUtils.getFirstColorCode
import at.hannibal2.skyhanni.utils.LorenzColor.Companion.toLorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils.getCorners
import at.hannibal2.skyhanni.utils.compat.EnchantmentsCompat
import at.hannibal2.skyhanni.utils.compat.GuiScreenUtils
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.RenderableUtils.renderXAligned
import at.hannibal2.skyhanni.utils.renderables.RenderableUtils.renderYAligned
import at.hannibal2.skyhanni.utils.shader.ShaderManager
import io.github.notenoughupdates.moulconfig.internal.TextRenderUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GLAllocation
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.MathHelper
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.nio.FloatBuffer
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.time.Duration
import kotlin.time.DurationUnit

@Suppress("LargeClass")
object RenderUtils {

    enum class HorizontalAlignment(private val value: String) {
        LEFT("Left"),
        CENTER("Center"),
        RIGHT("Right"),
        DONT_ALIGN("Don't Align"),
        ;

        override fun toString() = value
    }

    enum class VerticalAlignment(private val value: String) {
        TOP("Top"),
        CENTER("Center"),
        BOTTOM("Bottom"),
        DONT_ALIGN("Don't Align"),
        ;

        override fun toString() = value
    }

    private val beaconBeam = ResourceLocation("textures/entity/beacon_beam.png")

    private val matrixBuffer: FloatBuffer = GLAllocation.createDirectFloatBuffer(16)
    private val colorBuffer: FloatBuffer = GLAllocation.createDirectFloatBuffer(16)
    private val bezier2Buffer: FloatBuffer = GLAllocation.createDirectFloatBuffer(9)

    infix fun Slot.highlight(color: LorenzColor) {
        highlight(color.toColor())
    }

    infix fun Slot.highlight(color: Color) {
        highlight(color, xDisplayPosition, yDisplayPosition)
    }

    infix fun RenderGuiItemOverlayEvent.highlight(color: LorenzColor) {
        highlight(color.toColor())
    }

    infix fun RenderGuiItemOverlayEvent.highlight(color: Color) {
        highlight(color, x, y)
    }

    fun highlight(color: Color, x: Int, y: Int) {
        GlStateManager.disableLighting()
        GlStateManager.disableDepth()
        GlStateManager.pushMatrix()
        // TODO don't use z
        GlStateManager.translate(0f, 0f, 110 + Minecraft.getMinecraft().renderItem.zLevel)
        Gui.drawRect(x, y, x + 16, y + 16, color.rgb)
        GlStateManager.popMatrix()
        GlStateManager.enableDepth()
        GlStateManager.enableLighting()
    }

    infix fun Slot.drawBorder(color: LorenzColor) {
        drawBorder(color.toColor())
    }

    infix fun Slot.drawBorder(color: Color) {
        drawBorder(color, xDisplayPosition, yDisplayPosition)
    }

    infix fun RenderGuiItemOverlayEvent.drawBorder(color: LorenzColor) {
        drawBorder(color.toColor())
    }

    infix fun RenderGuiItemOverlayEvent.drawBorder(color: Color) {
        drawBorder(color, x, y)
    }

    fun drawBorder(color: Color, x: Int, y: Int) {
        GlStateManager.disableLighting()
        GlStateManager.disableDepth()
        GlStateManager.pushMatrix()
        GlStateManager.translate(0f, 0f, 110 + Minecraft.getMinecraft().renderItem.zLevel)
        Gui.drawRect(x, y, x + 1, y + 16, color.rgb)
        Gui.drawRect(x, y, x + 16, y + 1, color.rgb)
        Gui.drawRect(x, y + 15, x + 16, y + 16, color.rgb)
        Gui.drawRect(x + 15, y, x + 16, y + 16, color.rgb)
        GlStateManager.popMatrix()
        GlStateManager.enableDepth()
        GlStateManager.enableLighting()
    }

    fun LorenzRenderWorldEvent.drawColor(
        location: LorenzVec,
        color: LorenzColor,
        beacon: Boolean = false,
        alpha: Float = -1f,
    ) {
        drawColor(location, color.toColor(), beacon, alpha)
    }

    fun LorenzRenderWorldEvent.drawColor(
        location: LorenzVec,
        color: Color,
        beacon: Boolean = false,
        alpha: Float = -1f,
    ) {
        val (viewerX, viewerY, viewerZ) = getViewerPos(partialTicks)
        val x = location.x - viewerX
        val y = location.y - viewerY
        val z = location.z - viewerZ
        val distSq = x * x + y * y + z * z
        val realAlpha = if (alpha == -1f) {
            (0.1f + 0.005f * distSq.toFloat()).coerceAtLeast(0.2f)
        } else {
            alpha
        }
        GlStateManager.disableDepth()
        GlStateManager.disableCull()
        drawFilledBoundingBox(
            AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1).expandBlock(),
            color,
            realAlpha,
        )
        GlStateManager.disableTexture2D()
        if (distSq > 5 * 5 && beacon) renderBeaconBeam(x, y + 1, z, color.rgb, 1.0f, partialTicks)
        GlStateManager.disableLighting()
        GlStateManager.enableTexture2D()
        GlStateManager.enableDepth()
        GlStateManager.enableCull()
    }

    val absoluteTranslation
        get() = run {
            matrixBuffer.clear()

            GlStateManager.getFloat(GL11.GL_MODELVIEW_MATRIX, matrixBuffer)

            val read = generateSequence(0) { it + 1 }.take(16).map { matrixBuffer.get() }.toList()

            val xTranslate = read[12].toInt()
            val yTranslate = read[13].toInt()
            val zTranslate = read[14].toInt()

            matrixBuffer.flip()

            Triple(xTranslate, yTranslate, zTranslate)
        }

    fun getViewerPos(partialTicks: Float) =
        Minecraft.getMinecraft().renderViewEntity?.let { exactLocation(it, partialTicks) } ?: LorenzVec()

    fun AxisAlignedBB.expandBlock(n: Int = 1) = expand(LorenzVec.expandVector * n)
    fun AxisAlignedBB.inflateBlock(n: Int = 1) = expand(LorenzVec.expandVector * -n)

    /**
     * Taken from NotEnoughUpdates under Creative Commons Attribution-NonCommercial 3.0
     * https://github.com/Moulberry/NotEnoughUpdates/blob/master/LICENSE
     * @author Moulberry
     * @author Mojang
     */
    private fun renderBeaconBeam(
        x: Double,
        y: Double,
        z: Double,
        rgb: Int,
        alphaMultiplier: Float,
        partialTicks: Float,
    ) {
        val height = 300
        val bottomOffset = 0
        val topOffset = bottomOffset + height
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer
        Minecraft.getMinecraft().textureManager.bindTexture(beaconBeam)
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, 10497.0f)
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, 10497.0f)
        GlStateManager.disableLighting()
        GlStateManager.enableCull()
        GlStateManager.enableTexture2D()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, 1, 1, 0)
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0)
        val time = Minecraft.getMinecraft().theWorld.totalWorldTime + partialTicks.toDouble()
        val d1 = MathHelper.func_181162_h(
            -time * 0.2 - MathHelper.floor_double(-time * 0.1)
                .toDouble(),
        )
        val r = (rgb shr 16 and 0xFF) / 255f
        val g = (rgb shr 8 and 0xFF) / 255f
        val b = (rgb and 0xFF) / 255f
        val d2 = time * 0.025 * -1.5
        val d4 = 0.5 + cos(d2 + 2.356194490192345) * 0.2
        val d5 = 0.5 + sin(d2 + 2.356194490192345) * 0.2
        val d6 = 0.5 + cos(d2 + Math.PI / 4.0) * 0.2
        val d7 = 0.5 + sin(d2 + Math.PI / 4.0) * 0.2
        val d8 = 0.5 + cos(d2 + 3.9269908169872414) * 0.2
        val d9 = 0.5 + sin(d2 + 3.9269908169872414) * 0.2
        val d10 = 0.5 + cos(d2 + 5.497787143782138) * 0.2
        val d11 = 0.5 + sin(d2 + 5.497787143782138) * 0.2
        val d14 = -1.0 + d1
        val d15 = height.toDouble() * 2.5 + d14
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR)
        worldRenderer.pos(x + d4, y + topOffset, z + d5).tex(1.0, d15).color(r, g, b, 1.0f * alphaMultiplier)
            .endVertex()
        worldRenderer.pos(x + d4, y + bottomOffset, z + d5).tex(1.0, d14).color(r, g, b, 1.0f).endVertex()
        worldRenderer.pos(x + d6, y + bottomOffset, z + d7).tex(0.0, d14).color(r, g, b, 1.0f).endVertex()
        worldRenderer.pos(x + d6, y + topOffset, z + d7).tex(0.0, d15).color(r, g, b, 1.0f * alphaMultiplier)
            .endVertex()
        worldRenderer.pos(x + d10, y + topOffset, z + d11).tex(1.0, d15).color(r, g, b, 1.0f * alphaMultiplier)
            .endVertex()
        worldRenderer.pos(x + d10, y + bottomOffset, z + d11).tex(1.0, d14).color(r, g, b, 1.0f).endVertex()
        worldRenderer.pos(x + d8, y + bottomOffset, z + d9).tex(0.0, d14).color(r, g, b, 1.0f).endVertex()
        worldRenderer.pos(x + d8, y + topOffset, z + d9).tex(0.0, d15).color(r, g, b, 1.0f * alphaMultiplier)
            .endVertex()
        worldRenderer.pos(x + d6, y + topOffset, z + d7).tex(1.0, d15).color(r, g, b, 1.0f * alphaMultiplier)
            .endVertex()
        worldRenderer.pos(x + d6, y + bottomOffset, z + d7).tex(1.0, d14).color(r, g, b, 1.0f).endVertex()
        worldRenderer.pos(x + d10, y + bottomOffset, z + d11).tex(0.0, d14).color(r, g, b, 1.0f).endVertex()
        worldRenderer.pos(x + d10, y + topOffset, z + d11).tex(0.0, d15).color(r, g, b, 1.0f * alphaMultiplier)
            .endVertex()
        worldRenderer.pos(x + d8, y + topOffset, z + d9).tex(1.0, d15).color(r, g, b, 1.0f * alphaMultiplier)
            .endVertex()
        worldRenderer.pos(x + d8, y + bottomOffset, z + d9).tex(1.0, d14).color(r, g, b, 1.0f).endVertex()
        worldRenderer.pos(x + d4, y + bottomOffset, z + d5).tex(0.0, d14).color(r, g, b, 1.0f).endVertex()
        worldRenderer.pos(x + d4, y + topOffset, z + d5).tex(0.0, d15).color(r, g, b, 1.0f * alphaMultiplier)
            .endVertex()
        tessellator.draw()
        GlStateManager.disableCull()
        val d12 = -1.0 + d1
        val d13 = height + d12
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR)
        worldRenderer.pos(x + 0.2, y + topOffset, z + 0.2).tex(1.0, d13).color(r, g, b, 0.25f * alphaMultiplier)
            .endVertex()
        worldRenderer.pos(x + 0.2, y + bottomOffset, z + 0.2).tex(1.0, d12).color(r, g, b, 0.25f).endVertex()
        worldRenderer.pos(x + 0.8, y + bottomOffset, z + 0.2).tex(0.0, d12).color(r, g, b, 0.25f).endVertex()
        worldRenderer.pos(x + 0.8, y + topOffset, z + 0.2).tex(0.0, d13).color(r, g, b, 0.25f * alphaMultiplier)
            .endVertex()
        worldRenderer.pos(x + 0.8, y + topOffset, z + 0.8).tex(1.0, d13).color(r, g, b, 0.25f * alphaMultiplier)
            .endVertex()
        worldRenderer.pos(x + 0.8, y + bottomOffset, z + 0.8).tex(1.0, d12).color(r, g, b, 0.25f).endVertex()
        worldRenderer.pos(x + 0.2, y + bottomOffset, z + 0.8).tex(0.0, d12).color(r, g, b, 0.25f).endVertex()
        worldRenderer.pos(x + 0.2, y + topOffset, z + 0.8).tex(0.0, d13).color(r, g, b, 0.25f * alphaMultiplier)
            .endVertex()
        worldRenderer.pos(x + 0.8, y + topOffset, z + 0.2).tex(1.0, d13).color(r, g, b, 0.25f * alphaMultiplier)
            .endVertex()
        worldRenderer.pos(x + 0.8, y + bottomOffset, z + 0.2).tex(1.0, d12).color(r, g, b, 0.25f).endVertex()
        worldRenderer.pos(x + 0.8, y + bottomOffset, z + 0.8).tex(0.0, d12).color(r, g, b, 0.25f).endVertex()
        worldRenderer.pos(x + 0.8, y + topOffset, z + 0.8).tex(0.0, d13).color(r, g, b, 0.25f * alphaMultiplier)
            .endVertex()
        worldRenderer.pos(x + 0.2, y + topOffset, z + 0.8).tex(1.0, d13).color(r, g, b, 0.25f * alphaMultiplier)
            .endVertex()
        worldRenderer.pos(x + 0.2, y + bottomOffset, z + 0.8).tex(1.0, d12).color(r, g, b, 0.25f).endVertex()
        worldRenderer.pos(x + 0.2, y + bottomOffset, z + 0.2).tex(0.0, d12).color(r, g, b, 0.25f).endVertex()
        worldRenderer.pos(x + 0.2, y + topOffset, z + 0.2).tex(0.0, d13).color(r, g, b, 0.25f * alphaMultiplier)
            .endVertex()
        tessellator.draw()
    }

    fun LorenzRenderWorldEvent.drawWaypointFilled(
        location: LorenzVec,
        color: Color,
        seeThroughBlocks: Boolean = false,
        beacon: Boolean = false,
        extraSize: Double = 0.0,
        extraSizeTopY: Double = extraSize,
        extraSizeBottomY: Double = extraSize,
        minimumAlpha: Float = 0.2f,
        inverseAlphaScale: Boolean = false,
    ) {
        val (viewerX, viewerY, viewerZ) = getViewerPos(partialTicks)
        val x = location.x - viewerX
        val y = location.y - viewerY
        val z = location.z - viewerZ
        val distSq = x * x + y * y + z * z

        if (seeThroughBlocks) {
            GlStateManager.disableDepth()
        }

        GlStateManager.disableCull()
        drawFilledBoundingBox(
            @Suppress("ktlint:standard:argument-list-wrapping")
            AxisAlignedBB(
                x - extraSize, y - extraSizeBottomY, z - extraSize,
                x + 1 + extraSize, y + 1 + extraSizeTopY, z + 1 + extraSize,
            ).expandBlock(),
            color,
            if (inverseAlphaScale) (1.0f - 0.005f * distSq.toFloat()).coerceAtLeast(minimumAlpha)
            else (0.1f + 0.005f * distSq.toFloat()).coerceAtLeast(minimumAlpha),
        )
        GlStateManager.disableTexture2D()
        if (distSq > 5 * 5 && beacon) renderBeaconBeam(x, y + 1, z, color.rgb, 1.0f, partialTicks)
        GlStateManager.disableLighting()
        GlStateManager.enableTexture2D()
        GlStateManager.enableCull()

        if (seeThroughBlocks) {
            GlStateManager.enableDepth()
        }
    }

    fun LorenzRenderWorldEvent.drawString(
        location: LorenzVec,
        text: String,
        seeThroughBlocks: Boolean = false,
        color: Color? = null,
    ) {
        val viewer = Minecraft.getMinecraft().renderViewEntity ?: return
        GlStateManager.alphaFunc(516, 0.1f)
        GlStateManager.pushMatrix()
        val renderManager = Minecraft.getMinecraft().renderManager
        var x = location.x - renderManager.viewerPosX
        var y = location.y - renderManager.viewerPosY - viewer.eyeHeight
        var z = location.z - renderManager.viewerPosZ
        val distSq = x * x + y * y + z * z
        val dist = sqrt(distSq)
        if (distSq > 144) {
            x *= 12 / dist
            y *= 12 / dist
            z *= 12 / dist
        }

        if (seeThroughBlocks) {
            GlStateManager.disableDepth()
            GlStateManager.disableCull()
        }

        GlStateManager.translate(x, y, z)
        GlStateManager.translate(0f, viewer.eyeHeight, 0f)
        drawNametag(text, color)
        GlStateManager.rotate(-renderManager.playerViewY, 0.0f, 1.0f, 0.0f)
        GlStateManager.rotate(renderManager.playerViewX, 1.0f, 0.0f, 0.0f)
        GlStateManager.translate(0f, -0.25f, 0f)
        GlStateManager.rotate(-renderManager.playerViewX, 1.0f, 0.0f, 0.0f)
        GlStateManager.rotate(renderManager.playerViewY, 0.0f, 1.0f, 0.0f)
        GlStateManager.popMatrix()
        GlStateManager.disableLighting()


        if (seeThroughBlocks) {
            GlStateManager.enableDepth()
            GlStateManager.enableCull()
        }
    }

    /**
     * @author Mojang
     */
    fun drawNametag(str: String, color: Color?) {
        val fontRenderer = Minecraft.getMinecraft().fontRendererObj
        val f1 = 0.02666667f
        GlStateManager.pushMatrix()
        GL11.glNormal3f(0.0f, 1.0f, 0.0f)
        GlStateManager.rotate(-Minecraft.getMinecraft().renderManager.playerViewY, 0.0f, 1.0f, 0.0f)
        GlStateManager.rotate(
            Minecraft.getMinecraft().renderManager.playerViewX,
            1.0f,
            0.0f,
            0.0f,
        )
        GlStateManager.scale(-f1, -f1, f1)
        GlStateManager.disableLighting()
        GlStateManager.depthMask(false)
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0)
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        val i = 0
        val j = fontRenderer.getStringWidth(str) / 2
        GlStateManager.disableTexture2D()
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        worldrenderer.pos((-j - 1).toDouble(), (-1 + i).toDouble(), 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex()
        worldrenderer.pos((-j - 1).toDouble(), (8 + i).toDouble(), 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex()
        worldrenderer.pos((j + 1).toDouble(), (8 + i).toDouble(), 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex()
        worldrenderer.pos((j + 1).toDouble(), (-1 + i).toDouble(), 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex()
        tessellator.draw()
        GlStateManager.enableTexture2D()
        val colorCode = color?.rgb ?: 553648127
        fontRenderer.drawString(str, -j, i, colorCode)
        GlStateManager.depthMask(true)
        fontRenderer.drawString(str, -j, i, -1)
        GlStateManager.enableBlend()
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        GlStateManager.popMatrix()
    }

    /**
     * @author Mojang
     */
    fun drawLabel(
        pos: LorenzVec,
        text: String,
        partialTicks: Float,
        shadow: Boolean = false,
        scale: Float = 1f,
        yOff: Float = 0f,
        debug: Boolean = false,
    ) {
        val minecraft = Minecraft.getMinecraft()
        val player = minecraft.thePlayer
        val x =
            pos.x - player.lastTickPosX + (pos.x - player.posX - (pos.x - player.lastTickPosX)) * partialTicks
        val y =
            pos.y - player.lastTickPosY + (pos.y - player.posY - (pos.y - player.lastTickPosY)) * partialTicks
        val z =
            pos.z - player.lastTickPosZ + (pos.z - player.posZ - (pos.z - player.lastTickPosZ)) * partialTicks

        // 7 – 25

        val translate = LorenzVec(x, y, z)
        val length = translate.length().toFloat()

        var finalText = text
        var factor = 1f
        var finalScale = scale
        if (debug) {
            finalText = "$text ${length.toInt()}"
            if (length < 8) {
                factor = 8 / length
            }
            if (length > 15) {
                factor = 15 / length
            }
            finalScale = scale * sqrt(factor.toDouble()).toFloat()
        }

        val f1 = 0.0266666688

        val width = minecraft.fontRendererObj.getStringWidth(finalText) / 2
        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, z)
        GL11.glNormal3f(0f, 1f, 0f)
        val renderManager = minecraft.renderManager
        GlStateManager.rotate(-renderManager.playerViewY, 0f, 1f, 0f)
        GlStateManager.rotate(renderManager.playerViewX, 1f, 0f, 0f)
        GlStateManager.scale(-f1, -f1, -f1)
        GlStateManager.scale(finalScale, finalScale, finalScale)
        GlStateManager.enableBlend()
        GlStateManager.disableLighting()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0)
        GlStateManager.enableTexture2D()
        minecraft.fontRendererObj.drawString(
            finalText,
            (-width).toFloat(),
            yOff,
            LorenzColor.WHITE.toColor().rgb,
            shadow,
        )
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }

    fun interpolate(currentValue: Double, lastValue: Double, multiplier: Double): Double {
        return lastValue + (currentValue - lastValue) * multiplier
    }

    fun Position.transform(): Pair<Int, Int> {
        GlStateManager.translate(getAbsX().toFloat(), getAbsY().toFloat(), 0F)
        GlStateManager.scale(effectiveScale, effectiveScale, 1F)
        val x = ((GuiScreenUtils.mouseX - getAbsX()) / effectiveScale).toInt()
        val y = ((GuiScreenUtils.mouseY - getAbsY()) / effectiveScale).toInt()
        return x to y
    }

    fun Position.renderString(string: String?, offsetX: Int = 0, offsetY: Int = 0, posLabel: String) {
        if (string.isNullOrBlank()) return
        val x = renderString0(string, offsetX, offsetY, isCenter)
        GuiEditManager.add(this, posLabel, x, 10)
    }

    private fun Position.renderString0(string: String, offsetX: Int = 0, offsetY: Int = 0, centered: Boolean): Int {
        val display = "§f$string"
        GlStateManager.pushMatrix()
        transform()
        val minecraft = Minecraft.getMinecraft()
        val renderer = minecraft.renderManager.fontRenderer

        GlStateManager.translate(offsetX + 1.0, offsetY + 1.0, 0.0)

        if (centered) {
            val strLen: Int = renderer.getStringWidth(string)
            val x2 = offsetX - strLen / 2f
            GL11.glTranslatef(x2, 0f, 0f)
            renderer.drawStringWithShadow(display, 0f, 0f, 0)
            GL11.glTranslatef(-x2, 0f, 0f)
        } else {
            renderer.drawStringWithShadow(display, 0f, 0f, 0)
        }


        GlStateManager.popMatrix()

        return renderer.getStringWidth(display)
    }

    fun Position.renderStrings(list: List<String>, extraSpace: Int = 0, posLabel: String) {
        if (list.isEmpty()) return

        var offsetY = 0
        var longestX = 0
        for (s in list) {
            val x = renderString0(s, offsetY = offsetY, centered = false)
            if (x > longestX) {
                longestX = x
            }
            offsetY += 10 + extraSpace
        }
        GuiEditManager.add(this, posLabel, longestX, offsetY)
    }

    fun Position.renderRenderables(
        renderables: List<Renderable>,
        extraSpace: Int = 0,
        posLabel: String,
        addToGuiManager: Boolean = true,
    ) {
        if (renderables.isEmpty()) return
        var longestY = 0
        val longestX = renderables.maxOf { it.width }
        for (line in renderables) {
            GlStateManager.pushMatrix()
            val (x, y) = transform()
            GlStateManager.translate(0f, longestY.toFloat(), 0F)
            Renderable.withMousePosition(x, y) {
                line.renderXAligned(0, longestY, longestX)
            }

            longestY += line.height + extraSpace + 2

            GlStateManager.popMatrix()
        }
        if (addToGuiManager) GuiEditManager.add(this, posLabel, longestX, longestY)
    }

    fun Position.renderRenderable(
        renderable: Renderable?,
        posLabel: String,
        addToGuiManager: Boolean = true,
    ) {
        if (renderable == null) return
        GlStateManager.pushMatrix()
        val (x, y) = transform()
        Renderable.withMousePosition(x, y) {
            renderable.render(0, 0)
        }
        GlStateManager.popMatrix()
        if (addToGuiManager) GuiEditManager.add(this, posLabel, renderable.width, renderable.height)
    }

    /** This function is discouraged to be used. Please use renderRenderables with List<Renderable> instead with horizontal container.*/
    fun Position.renderRenderablesDouble(
        renderables: List<List<Renderable>>,
        extraSpace: Int = 0,
        posLabel: String,
        addToGuiManager: Boolean = true,
    ) {
        if (renderables.isEmpty()) return
        var longestY = 0
        var longestX = 0
        GlStateManager.pushMatrix()
        val (x, y) = transform()
        Renderable.withMousePosition(x, y) {
            for (line in renderables) {
                GlStateManager.pushMatrix()
                GlStateManager.translate(0f, longestY.toFloat(), 0F)
                val lineY = line.maxOf { it.height }
                var lineX = 0
                for (element in line) {
                    element.renderYAligned(lineX, longestY, lineY)
                    GlStateManager.translate(element.width.toFloat(), 0f, 0f)
                    lineX += element.width
                }
                longestY += lineY + extraSpace + 2
                longestX = max(longestX, lineX)
                GlStateManager.popMatrix()
            }
        }
        GlStateManager.popMatrix()
        if (addToGuiManager) GuiEditManager.add(this, posLabel, longestX, longestY)
    }

    /**
     * Accepts a list of lines to print.
     * Each line is a list of things to print. Can print String or ItemStack objects.
     */
    @Deprecated("use List<List<Renderable>>", ReplaceWith("this.renderRenderablesDouble(list,extraSpace,posLabel)"))
    fun Position.renderStringsAndItems(
        list: List<List<Any?>>,
        extraSpace: Int = 0,
        itemScale: Double = NEUItems.itemFontSize,
        posLabel: String,
    ) {
        if (list.isEmpty()) return

        val render = list.map { listEntry ->
            listEntry.map {
                Renderable.fromAny(it, itemScale = itemScale) ?: throw RuntimeException("Unknown render object: $it")
            }
        }

        this.renderRenderablesDouble(render, extraSpace, posLabel, true)
    }

    /**
     * Accepts a single line to print.
     * This line is a list of things to print. Can print String or ItemStack objects.
     */
    @Deprecated("use List<Renderable>", ReplaceWith(""))
    fun Position.renderSingleLineWithItems(
        list: List<Any?>,
        posLabel: String,
    ) {
        if (list.isEmpty()) return
        renderRenderable(
            Renderable.horizontalContainer(
                list.mapNotNull { Renderable.fromAny(it) },
            ),
            posLabel = posLabel,
        )
        // TODO Future write that better
    }

    private fun Position.renderLine(line: List<Any?>, offsetY: Int, itemScale: Double = NEUItems.itemFontSize): Int {
        GlStateManager.pushMatrix()
        val (x, y) = transform()
        GlStateManager.translate(0f, offsetY.toFloat(), 0F)
        var offsetX = 0
        Renderable.withMousePosition(x, y) {
            for (any in line) {
                val renderable = Renderable.fromAny(any, itemScale = itemScale)
                    ?: throw RuntimeException("Unknown render object: $any")
                renderable.render(offsetX, offsetY)
                offsetX += renderable.width
                GlStateManager.translate(renderable.width.toFloat(), 0F, 0F)
            }
        }
        GlStateManager.popMatrix()
        return offsetX
    }

    @Deprecated("use renderable item list", ReplaceWith(""))
    fun MutableList<Any>.addItemIcon(
        item: ItemStack,
        highlight: Boolean = false,
        scale: Double = NEUItems.itemFontSize,
    ) {
        try {
            if (highlight) {
                // Hack to add enchant glint, like Hypixel does it
                item.addEnchantment(EnchantmentsCompat.PROTECTION.enchantment, 0)
            }
            add(Renderable.itemStack(item, scale))
        } catch (e: NullPointerException) {
            ErrorManager.logErrorWithData(
                e,
                "Add item icon to renderable list",
                "item" to item,
            )
        }
    }

    // totally not modified Autumn Client's TargetStrafe
    fun drawCircle(entity: Entity, partialTicks: Float, rad: Double, color: Color) {
        GlStateManager.pushMatrix()
        GL11.glNormal3f(0.0f, 1.0f, 0.0f)

        GlStateManager.enableDepth()
        GlStateManager.enableBlend()
        GlStateManager.depthFunc(GL11.GL_LEQUAL)
        GlStateManager.disableCull()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0)
        GlStateManager.enableAlpha()
        GlStateManager.disableTexture2D()

        GlStateManager.disableDepth()

        var il = 0.0
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer
        while (il < 0.05) {
            GlStateManager.pushMatrix()
            GlStateManager.disableTexture2D()
            GL11.glLineWidth(2F)
            worldRenderer.begin(1, DefaultVertexFormats.POSITION)
            val renderManager = Minecraft.getMinecraft().renderManager
            val x: Double =
                entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks - renderManager.viewerPosX
            val y: Double =
                entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks - renderManager.viewerPosY
            val z: Double =
                entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks - renderManager.viewerPosZ
            val pix2 = Math.PI * 2.0
            for (i in 0..90) {
                color.bindColor()
                worldRenderer.pos(x + rad * cos(i * pix2 / 45.0), y + il, z + rad * sin(i * pix2 / 45.0)).endVertex()
            }
            tessellator.draw()
            GlStateManager.enableTexture2D()
            GlStateManager.popMatrix()
            il += 0.0006
        }

        GlStateManager.enableDepth()

        GlStateManager.enableCull()
        GlStateManager.enableTexture2D()
        GlStateManager.enableDepth()
        GlStateManager.disableBlend()
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        GlStateManager.popMatrix()
    }

    fun LorenzRenderWorldEvent.drawCylinderInWorld(
        color: Color,
        location: LorenzVec,
        radius: Float,
        height: Float,
    ) {
        drawCylinderInWorld(color, location.x, location.y, location.z, radius, height, partialTicks)
    }

    // Todo: Gauge whether or not partialTicks is actually necessary, or if it can be removed
    @Suppress("UnusedParameter")
    fun drawCylinderInWorld(
        color: Color,
        x: Double,
        y: Double,
        z: Double,
        radius: Float,
        height: Float,
        partialTicks: Float,
    ) {
        GlStateManager.pushMatrix()
        GL11.glNormal3f(0.0f, 1.0f, 0.0f)

        GlStateManager.enableDepth()
        GlStateManager.enableBlend()
        GlStateManager.depthFunc(GL11.GL_LEQUAL)
        GlStateManager.disableCull()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0)
        GlStateManager.enableAlpha()
        GlStateManager.disableTexture2D()
        color.bindColor()
        bindCamera()

        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer
        worldRenderer.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION)
        var currentAngle = 0f
        val angleStep = 0.1f
        while (currentAngle < 2 * Math.PI) {
            val xOffset = radius * cos(currentAngle.toDouble()).toFloat()
            val zOffset = radius * sin(currentAngle.toDouble()).toFloat()
            worldRenderer.pos(x + xOffset, y + height, z + zOffset).endVertex()
            worldRenderer.pos(x + xOffset, y + 0, z + zOffset).endVertex()
            currentAngle += angleStep
        }
        worldRenderer.pos(x + radius, y + height, z).endVertex()
        worldRenderer.pos(x + radius, y + 0.0, z).endVertex()
        tessellator.draw()

        GlStateManager.enableCull()
        GlStateManager.enableTexture2D()
        GlStateManager.enableDepth()
        GlStateManager.disableBlend()
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        GlStateManager.popMatrix()
    }

    fun LorenzRenderWorldEvent.drawSphereInWorld(
        color: Color,
        location: LorenzVec,
        radius: Float,
    ) {
        drawSphereInWorld(color, location.x, location.y, location.z, radius)
    }

    fun LorenzRenderWorldEvent.drawSphereInWorld(
        color: Color,
        x: Double,
        y: Double,
        z: Double,
        radius: Float,
    ) {
        GlStateManager.pushMatrix()
        GL11.glNormal3f(0.0f, 1.0f, 0.0f)

        GlStateManager.enableDepth()
        GlStateManager.enableBlend()
        GlStateManager.depthFunc(GL11.GL_LEQUAL)
        GlStateManager.disableCull()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0)
        GlStateManager.enableAlpha()
        GlStateManager.disableTexture2D()
        color.bindColor()
        bindCamera()

        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)

        val segments = 32

        for (phi in 0 until segments) {
            for (theta in 0 until segments * 2) {
                val x1 = x + radius * sin(Math.PI * phi / segments) * cos(2.0 * Math.PI * theta / (segments * 2))
                val y1 = y + radius * cos(Math.PI * phi / segments)
                val z1 = z + radius * sin(Math.PI * phi / segments) * sin(2.0 * Math.PI * theta / (segments * 2))

                val x2 = x + radius * sin(Math.PI * (phi + 1) / segments) * cos(2.0 * Math.PI * theta / (segments * 2))
                val y2 = y + radius * cos(Math.PI * (phi + 1) / segments)
                val z2 = z + radius * sin(Math.PI * (phi + 1) / segments) * sin(2.0 * Math.PI * theta / (segments * 2))

                worldrenderer.pos(x1, y1, z1).endVertex()
                worldrenderer.pos(x2, y2, z2).endVertex()

                val x3 =
                    x + radius * sin(Math.PI * (phi + 1) / segments) * cos(2.0 * Math.PI * (theta + 1) / (segments * 2))
                val y3 = y + radius * cos(Math.PI * (phi + 1) / segments)
                val z3 =
                    z + radius * sin(Math.PI * (phi + 1) / segments) * sin(2.0 * Math.PI * (theta + 1) / (segments * 2))

                val x4 = x + radius * sin(Math.PI * phi / segments) * cos(2.0 * Math.PI * (theta + 1) / (segments * 2))
                val y4 = y + radius * cos(Math.PI * phi / segments)
                val z4 = z + radius * sin(Math.PI * phi / segments) * sin(2.0 * Math.PI * (theta + 1) / (segments * 2))

                worldrenderer.pos(x3, y3, z3).endVertex()
                worldrenderer.pos(x4, y4, z4).endVertex()
            }
        }

        tessellator.draw()

        GlStateManager.enableCull()
        GlStateManager.enableTexture2D()
        GlStateManager.enableDepth()
        GlStateManager.disableBlend()
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        GlStateManager.popMatrix()
    }

    fun LorenzRenderWorldEvent.drawSphereWireframeInWorld(
        color: Color,
        location: LorenzVec,
        radius: Float,
    ) {
        drawSphereWireframeInWorld(color, location.x, location.y, location.z, radius)
    }

    fun LorenzRenderWorldEvent.drawSphereWireframeInWorld(
        color: Color,
        x: Double,
        y: Double,
        z: Double,
        radius: Float,
    ) {
        GlStateManager.pushMatrix()
        GL11.glNormal3f(0.0f, 1.0f, 0.0f)

        GlStateManager.disableTexture2D()
        color.bindColor()
        bindCamera()

        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION)

        val segments = 32

        for (phi in 0 until segments) {
            for (theta in 0 until segments * 2) {
                val x1 = x + radius * sin(Math.PI * phi / segments) * cos(2.0 * Math.PI * theta / (segments * 2))
                val y1 = y + radius * cos(Math.PI * phi / segments)
                val z1 = z + radius * sin(Math.PI * phi / segments) * sin(2.0 * Math.PI * theta / (segments * 2))

                val x2 = x + radius * sin(Math.PI * (phi + 1) / segments) * cos(2.0 * Math.PI * theta / (segments * 2))
                val y2 = y + radius * cos(Math.PI * (phi + 1) / segments)
                val z2 = z + radius * sin(Math.PI * (phi + 1) / segments) * sin(2.0 * Math.PI * theta / (segments * 2))

                val x3 =
                    x + radius * sin(Math.PI * (phi + 1) / segments) * cos(2.0 * Math.PI * (theta + 1) / (segments * 2))
                val y3 = y + radius * cos(Math.PI * (phi + 1) / segments)
                val z3 =
                    z + radius * sin(Math.PI * (phi + 1) / segments) * sin(2.0 * Math.PI * (theta + 1) / (segments * 2))

                val x4 = x + radius * sin(Math.PI * phi / segments) * cos(2.0 * Math.PI * (theta + 1) / (segments * 2))
                val y4 = y + radius * cos(Math.PI * phi / segments)
                val z4 = z + radius * sin(Math.PI * phi / segments) * sin(2.0 * Math.PI * (theta + 1) / (segments * 2))


                worldrenderer.pos(x1, y1, z1).endVertex()
                worldrenderer.pos(x2, y2, z2).endVertex()

                worldrenderer.pos(x2, y2, z2).endVertex()
                worldrenderer.pos(x3, y3, z3).endVertex()

                worldrenderer.pos(x3, y3, z3).endVertex()
                worldrenderer.pos(x4, y4, z4).endVertex()

                worldrenderer.pos(x4, y4, z4).endVertex()
                worldrenderer.pos(x1, y1, z1).endVertex()
            }
        }

        tessellator.draw()

        GlStateManager.enableTexture2D()
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        GlStateManager.popMatrix()
    }

    private fun Color.bindColor() =
        GlStateManager.color(this.red / 255f, this.green / 255f, this.blue / 255f, this.alpha / 255f)

    private fun bindCamera() {
        val renderManager = Minecraft.getMinecraft().renderManager
        val viewer = renderManager.viewerPosX
        val viewY = renderManager.viewerPosY
        val viewZ = renderManager.viewerPosZ
        GlStateManager.translate(-viewer, -viewY, -viewZ)
    }

    fun drawStringScaledMaxWidth(
        str: String?,
        fr: FontRenderer,
        x: Float,
        y: Float,
        shadow: Boolean,
        len: Int,
        color: Int,
    ) {
        val strLen = fr.getStringWidth(str)
        var factor = len / strLen.toFloat()
        factor = 1f.coerceAtMost(factor)
        TextRenderUtils.drawStringScaled(str, fr, x, y, shadow, color, factor)
    }

    fun LorenzRenderWorldEvent.drawDynamicText(
        location: LorenzVec,
        text: String,
        scaleMultiplier: Double,
        yOff: Float = 0f,
        hideTooCloseAt: Double = 4.5,
        smallestDistanceVew: Double = 5.0,
        ignoreBlocks: Boolean = true,
        ignoreY: Boolean = false,
        maxDistance: Int? = null,
    ) {
        val viewer = Minecraft.getMinecraft().renderViewEntity ?: return
        val thePlayer = Minecraft.getMinecraft().thePlayer ?: return

        val x = location.x
        val y = location.y
        val z = location.z

        val renderOffsetX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * partialTicks
        val renderOffsetY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * partialTicks
        val renderOffsetZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * partialTicks
        val eyeHeight = thePlayer.eyeHeight

        val dX = (x - renderOffsetX) * (x - renderOffsetX)
        val dY = (y - (renderOffsetY + eyeHeight)) * (y - (renderOffsetY + eyeHeight))
        val dZ = (z - renderOffsetZ) * (z - renderOffsetZ)
        val distToPlayerSq = dX + dY + dZ
        var distToPlayer = sqrt(distToPlayerSq)
        // TODO this is optional maybe?
        distToPlayer = distToPlayer.coerceAtLeast(smallestDistanceVew)

        if (distToPlayer < hideTooCloseAt) return
        maxDistance?.let {
            if (ignoreBlocks && distToPlayer > it) return
        }

        val distRender = distToPlayer.coerceAtMost(50.0)

        var scale = distRender / 12
        scale *= scaleMultiplier

        val resultX = renderOffsetX + (x + 0.5 - renderOffsetX) / (distToPlayer / distRender)
        val resultY = if (ignoreY) y * distToPlayer / distRender else renderOffsetY + eyeHeight +
            (y + 20 * distToPlayer / 300 - (renderOffsetY + eyeHeight)) / (distToPlayer / distRender)
        val resultZ = renderOffsetZ + (z + 0.5 - renderOffsetZ) / (distToPlayer / distRender)

        val renderLocation = LorenzVec(resultX, resultY, resultZ)

        render(renderLocation, "§f$text", scale, !ignoreBlocks, true, yOff)
    }

    private fun render(
        location: LorenzVec,
        text: String,
        scale: Double,
        depthTest: Boolean,
        shadow: Boolean,
        yOff: Float,
    ) {
        if (!depthTest) {
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glDepthMask(false)
        }
        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0)

        val minecraft = Minecraft.getMinecraft()
        val fontRenderer = minecraft.fontRendererObj
        val renderManager = minecraft.renderManager

        GlStateManager.translate(
            location.x - renderManager.viewerPosX,
            location.y - renderManager.viewerPosY,
            location.z - renderManager.viewerPosZ,
        )
        GlStateManager.color(1f, 1f, 1f, 0.5f)
        GlStateManager.rotate(-renderManager.playerViewY, 0.0f, 1.0f, 0.0f)
        GlStateManager.rotate(renderManager.playerViewX, 1.0f, 0.0f, 0.0f)
        GlStateManager.scale(-scale / 25, -scale / 25, scale / 25)
        val stringWidth = fontRenderer.getStringWidth(text)
        if (shadow) {
            fontRenderer.drawStringWithShadow(
                text,
                (-stringWidth / 2).toFloat(),
                yOff,
                0,
            )
        } else {
            fontRenderer.drawString(
                text,
                -stringWidth / 2,
                0,
                0,
            )
        }
        GlStateManager.color(1f, 1f, 1f)
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
        if (!depthTest) {
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDepthMask(true)
        }
    }

    fun LorenzRenderWorldEvent.draw3DLine(p1: LorenzVec, p2: LorenzVec, color: Color, lineWidth: Int, depth: Boolean) =
        LineDrawer.draw3D(partialTicks) {
            draw3DLine(p1, p2, color, lineWidth, depth)
        }

    fun LorenzRenderWorldEvent.exactLocation(entity: Entity) = exactLocation(entity, partialTicks)

    fun LorenzRenderWorldEvent.exactPlayerEyeLocation(): LorenzVec {
        val player = Minecraft.getMinecraft().thePlayer
        val add = if (player.isSneaking) LorenzVec(0.0, 1.54, 0.0) else LorenzVec(0.0, 1.62, 0.0)
        PatcherFixes.onPlayerEyeLine()
        return exactLocation(player) + add
    }

    fun LorenzRenderWorldEvent.exactBoundingBox(entity: Entity): AxisAlignedBB {
        if (entity.isDead) return entity.entityBoundingBox
        val offset = exactLocation(entity) - entity.getLorenzVec()
        return entity.entityBoundingBox.offset(offset.x, offset.y, offset.z)
    }

    fun LorenzRenderWorldEvent.exactPlayerEyeLocation(player: Entity): LorenzVec {
        val add = if (player.isSneaking) LorenzVec(0.0, 1.54, 0.0) else LorenzVec(0.0, 1.62, 0.0)
        return exactLocation(player) + add
    }

    fun LorenzRenderWorldEvent.drawLineToEye(location: LorenzVec, color: Color, lineWidth: Int, depth: Boolean) {
        draw3DLine(exactPlayerEyeLocation(), location, color, lineWidth, depth)
    }

    fun exactLocation(entity: Entity, partialTicks: Float): LorenzVec {
        if (entity.isDead) return entity.getLorenzVec()
        val x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks
        val y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks
        val z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks
        return LorenzVec(x, y, z)
    }

    fun drawFilledBoundingBox(aabb: AxisAlignedBB, c: Color, alphaMultiplier: Float = 1f) {
        GlStateManager.enableBlend()
        GlStateManager.disableLighting()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0)
        GlStateManager.disableTexture2D()
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer
        GlStateManager.color(c.red / 255f, c.green / 255f, c.blue / 255f, c.alpha / 255f * alphaMultiplier)

        // vertical
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex()
        tessellator.draw()
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex()
        tessellator.draw()
        GlStateManager.color(
            c.red / 255f * 0.8f,
            c.green / 255f * 0.8f,
            c.blue / 255f * 0.8f,
            c.alpha / 255f * alphaMultiplier,
        )

        // x
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex()
        tessellator.draw()
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex()
        tessellator.draw()
        GlStateManager.color(
            c.red / 255f * 0.9f,
            c.green / 255f * 0.9f,
            c.blue / 255f * 0.9f,
            c.alpha / 255f * alphaMultiplier,
        )
        // z
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex()
        tessellator.draw()
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex()
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    // TODO nea please merge with 'drawFilledBoundingBox'
    fun LorenzRenderWorldEvent.drawFilledBoundingBoxNea(
        aabb: AxisAlignedBB,
        c: Color,
        alphaMultiplier: Float = 1f,
        /**
         * If set to `true`, renders the box relative to the camera instead of relative to the world.
         * If set to `false`, will be relativized to [RenderUtils.getViewerPos].
         */
        renderRelativeToCamera: Boolean = false,
        drawVerticalBarriers: Boolean = true,
    ) {
        drawFilledBoundingBoxNea(aabb, c, alphaMultiplier, renderRelativeToCamera, drawVerticalBarriers, partialTicks)
    }

    fun drawWireframeBoundingBoxNea(
        aabb: AxisAlignedBB,
        color: Color,
        partialTicks: Float,
    ) {
        GlStateManager.enableBlend()
        GlStateManager.disableLighting()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0)
        GlStateManager.disableTexture2D()
        GlStateManager.disableCull()
        val vp = getViewerPos(partialTicks)

        @Suppress("ktlint:standard:argument-list-wrapping")
        val effectiveAABB = AxisAlignedBB(
            aabb.minX - vp.x, aabb.minY - vp.y, aabb.minZ - vp.z,
            aabb.maxX - vp.x, aabb.maxY - vp.y, aabb.maxZ - vp.z,
        )
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer

        with(color) {
            GlStateManager.color(red / 255f, green / 255f, blue / 255f, alpha / 255f)
        }
        // Bottom face
        worldRenderer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION)
        with(effectiveAABB) {
            worldRenderer.pos(minX, minY, minZ).endVertex()
            worldRenderer.pos(maxX, minY, minZ).endVertex()
            worldRenderer.pos(maxX, minY, maxZ).endVertex()
            worldRenderer.pos(minX, minY, maxZ).endVertex()
        }
        tessellator.draw()

        // Top face
        worldRenderer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION)
        with(effectiveAABB) {
            worldRenderer.pos(minX, maxY, maxZ).endVertex()
            worldRenderer.pos(maxX, maxY, maxZ).endVertex()
            worldRenderer.pos(maxX, maxY, minZ).endVertex()
            worldRenderer.pos(minX, maxY, minZ).endVertex()
        }
        tessellator.draw()


        worldRenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION)

        with(effectiveAABB) {
            worldRenderer.pos(minX, minY, minZ).endVertex()
            worldRenderer.pos(minX, maxY, minZ).endVertex()

            worldRenderer.pos(minX, minY, maxZ).endVertex()
            worldRenderer.pos(minX, maxY, maxZ).endVertex()

            worldRenderer.pos(maxX, minY, minZ).endVertex()
            worldRenderer.pos(maxX, maxY, minZ).endVertex()

            worldRenderer.pos(maxX, minY, maxZ).endVertex()
            worldRenderer.pos(maxX, maxY, maxZ).endVertex()
        }

        tessellator.draw()

        GlStateManager.enableTexture2D()
        GlStateManager.enableCull()
        GlStateManager.disableBlend()
    }

    fun WorldRenderer.pos(vec: LorenzVec) = this.pos(vec.x, vec.y, vec.z)

    fun draw3DQuad(
        middlePoint: LorenzVec,
        sidePoint1: LorenzVec,
        sidePoint2: LorenzVec,
        c: Color,
        partialTicks: Float = 0F,
    ) = QuadDrawer.draw3D(partialTicks) {
        draw(
            middlePoint,
            sidePoint1,
            sidePoint2,
            c,
        )
    }

    fun LorenzRenderWorldEvent.draw3DPathWithWaypoint(
        path: Graph,
        colorLine: Color,
        lineWidth: Int,
        depth: Boolean,
        startAtEye: Boolean = true,
        textSize: Double = 1.0,
        waypointColor: Color =
            (path.lastOrNull()?.name?.getFirstColorCode()?.toLorenzColor() ?: LorenzColor.WHITE).toColor(),
        bezierPoint: Double = 1.0,
        showNodeNames: Boolean = false,
        markLastBlock: Boolean = true,
    ) {
        if (path.isEmpty()) return
        val points = if (startAtEye) {
            listOf(
                this.exactPlayerEyeLocation() + Minecraft.getMinecraft().thePlayer.getLook(this.partialTicks)
                    .toLorenzVec()
                    /* .rotateXZ(-Math.PI / 72.0) */
                    .times(2),
            )
        } else {
            emptyList()
        } + path.toPositionsList().map { it.add(0.5, 0.5, 0.5) }
        LineDrawer.draw3D(partialTicks) {
            drawPath(
                points,
                colorLine,
                lineWidth,
                depth,
                bezierPoint,
            )
        }
        if (showNodeNames) {
            path.filter { it.name?.isNotEmpty() == true }.forEach {
                this.drawDynamicText(it.position, it.name!!, textSize)
            }
        }
        if (markLastBlock) {
            val last = path.last()
            drawWaypointFilled(last.position, waypointColor, seeThroughBlocks = true)
        }
    }

    class LineDrawer @PublishedApi internal constructor(val tessellator: Tessellator, val inverseView: LorenzVec) {

        val worldRenderer = tessellator.worldRenderer

        fun drawPath(path: List<LorenzVec>, color: Color, lineWidth: Int, depth: Boolean, bezierPoint: Double = 1.0) {
            if (bezierPoint < 0) {
                path.zipWithNext().forEach {
                    draw3DLine(it.first, it.second, color, lineWidth, depth)
                }
            } else {
                val pathLines = path.zipWithNext()
                pathLines.forEachIndexed { index, pathLine ->
                    val reduce = pathLine.second.minus(pathLine.first).normalize().times(bezierPoint)
                    draw3DLine(
                        if (index != 0) pathLine.first + reduce else pathLine.first,
                        if (index != pathLines.lastIndex) pathLine.second - reduce else pathLine.second,
                        color,
                        lineWidth,
                        depth,
                    )
                }
                path.zipWithNext3().forEach {
                    val p1 = it.second.minus(it.second.minus(it.first).normalize().times(bezierPoint))
                    val p3 = it.second.minus(it.second.minus(it.third).normalize().times(bezierPoint))
                    val p2 = it.second
                    drawBezier2(p1, p2, p3, color, lineWidth, depth)
                }
            }
        }

        fun draw3DLine(p1: LorenzVec, p2: LorenzVec, color: Color, lineWidth: Int, depth: Boolean) {
            GL11.glLineWidth(lineWidth.toFloat())
            if (!depth) {
                GL11.glDisable(GL11.GL_DEPTH_TEST)
                GlStateManager.depthMask(false)
            }
            GlStateManager.color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
            worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION)
            worldRenderer.pos(p1.x, p1.y, p1.z).endVertex()
            worldRenderer.pos(p2.x, p2.y, p2.z).endVertex()
            tessellator.draw()
            if (!depth) {
                GL11.glEnable(GL11.GL_DEPTH_TEST)
                GlStateManager.depthMask(true)
            }
        }

        fun draw3DLineFromPlayer(lorenzVec: LorenzVec, color: Color, lineWidth: Int, depth: Boolean) =
            draw3DLine(inverseView.add(y = Minecraft.getMinecraft().thePlayer.eyeHeight.toDouble()), lorenzVec, color, lineWidth, depth)

        fun drawBezier2(
            p1: LorenzVec,
            p2: LorenzVec,
            p3: LorenzVec,
            color: Color,
            lineWidth: Int,
            depth: Boolean,
            segments: Int = 30,
        ) {
            GL11.glLineWidth(lineWidth.toFloat())
            if (!depth) {
                GL11.glDisable(GL11.GL_DEPTH_TEST)
                GlStateManager.depthMask(false)
            }
            GlStateManager.color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
            val ctrlPoints = p1.toFloatArray() + p2.toFloatArray() + p3.toFloatArray()
            bezier2Buffer.clear()
            ctrlPoints.forEach {
                bezier2Buffer.put(it)
            }
            bezier2Buffer.flip()
            GL11.glMap1f(
                GL11.GL_MAP1_VERTEX_3,
                0.0f,
                1.0f,
                3,
                3,
                bezier2Buffer,
            )

            GL11.glEnable(GL11.GL_MAP1_VERTEX_3)

            GL11.glBegin(GL11.GL_LINE_STRIP)
            for (i in 0..segments) {
                GL11.glEvalCoord1f(i.toFloat() / segments.toFloat())
            }
            GL11.glEnd()
            if (!depth) {
                GL11.glEnable(GL11.GL_DEPTH_TEST)
                GlStateManager.depthMask(true)
            }
        }

        companion object {
            inline fun draw3D(
                partialTicks: Float = 0F,
                crossinline draws: LineDrawer.() -> Unit,
            ) {

                GlStateManager.enableBlend()
                GlStateManager.disableLighting()
                GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0)
                GlStateManager.disableTexture2D()
                GlStateManager.disableCull()
                GlStateManager.disableAlpha()

                val tessellator = Tessellator.getInstance()

                GlStateManager.pushMatrix()
                val inverseView = getViewerPos(partialTicks)
                RenderUtils.translate(inverseView.negated())

                draws.invoke(LineDrawer(Tessellator.getInstance(), inverseView))

                GlStateManager.popMatrix()

                GlStateManager.enableAlpha()
                GlStateManager.enableTexture2D()
                GlStateManager.enableCull()
                GlStateManager.disableBlend()
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
            }
        }
    }

    class QuadDrawer @PublishedApi internal constructor(val tessellator: Tessellator) {
        val worldRenderer = tessellator.worldRenderer
        inline fun draw(
            middlePoint: LorenzVec,
            sidePoint1: LorenzVec,
            sidePoint2: LorenzVec,
            c: Color,
        ) {
            GlStateManager.color(c.red / 255f, c.green / 255f, c.blue / 255f, c.alpha / 255f)
            worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
            worldRenderer.pos(sidePoint1).endVertex()
            worldRenderer.pos(middlePoint).endVertex()
            worldRenderer.pos(sidePoint2).endVertex()
            worldRenderer.pos(sidePoint1 + sidePoint2 - middlePoint).endVertex()
            tessellator.draw()
        }

        companion object {
            inline fun draw3D(
                partialTicks: Float = 0F,
                crossinline quads: QuadDrawer.() -> Unit,
            ) {

                GlStateManager.enableBlend()
                GlStateManager.disableLighting()
                GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0)
                GlStateManager.disableTexture2D()
                GlStateManager.disableCull()

                GlStateManager.pushMatrix()
                RenderUtils.translate(getViewerPos(partialTicks).negated())
                getViewerPos(partialTicks)

                quads.invoke(QuadDrawer(Tessellator.getInstance()))

                GlStateManager.popMatrix()

                GlStateManager.enableTexture2D()
                GlStateManager.enableCull()
                GlStateManager.disableBlend()
            }
        }
    }

    fun drawFilledBoundingBoxNea(
        aabb: AxisAlignedBB,
        c: Color,
        alphaMultiplier: Float = 1f,
        /**
         * If set to `true`, renders the box relative to the camera instead of relative to the world.
         * If set to `false`, will be relativized to [RenderUtils.getViewerPos]. Setting this to `false` requires
         * specifying [partialTicks]]
         */
        renderRelativeToCamera: Boolean = true,
        drawVerticalBarriers: Boolean = true,
        partialTicks: Float = 0F,
    ) {
        GlStateManager.enableBlend()
        GlStateManager.disableLighting()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0)
        GlStateManager.disableTexture2D()
        GlStateManager.disableCull()
        val effectiveAABB = if (!renderRelativeToCamera) {
            val vp = getViewerPos(partialTicks)
            AxisAlignedBB(
                aabb.minX - vp.x, aabb.minY - vp.y, aabb.minZ - vp.z,
                aabb.maxX - vp.x, aabb.maxY - vp.y, aabb.maxZ - vp.z,
            )
        } else {
            aabb
        }
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer

        // vertical
        if (drawVerticalBarriers) {
            GlStateManager.color(c.red / 255f, c.green / 255f, c.blue / 255f, c.alpha / 255f * alphaMultiplier)
            worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
            with(effectiveAABB) {
                worldRenderer.pos(minX, minY, minZ).endVertex()
                worldRenderer.pos(maxX, minY, minZ).endVertex()
                worldRenderer.pos(maxX, minY, maxZ).endVertex()
                worldRenderer.pos(minX, minY, maxZ).endVertex()
                tessellator.draw()
                worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
                worldRenderer.pos(minX, maxY, maxZ).endVertex()
                worldRenderer.pos(maxX, maxY, maxZ).endVertex()
                worldRenderer.pos(maxX, maxY, minZ).endVertex()
                worldRenderer.pos(minX, maxY, minZ).endVertex()
                tessellator.draw()
            }
        }
        GlStateManager.color(
            c.red / 255f * 0.8f,
            c.green / 255f * 0.8f,
            c.blue / 255f * 0.8f,
            c.alpha / 255f * alphaMultiplier,
        )

        // x
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
        with(effectiveAABB) {
            worldRenderer.pos(minX, minY, maxZ).endVertex()
            worldRenderer.pos(minX, maxY, maxZ).endVertex()
            worldRenderer.pos(minX, maxY, minZ).endVertex()
            worldRenderer.pos(minX, minY, minZ).endVertex()
            tessellator.draw()
            worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
            worldRenderer.pos(maxX, minY, minZ).endVertex()
            worldRenderer.pos(maxX, maxY, minZ).endVertex()
            worldRenderer.pos(maxX, maxY, maxZ).endVertex()
            worldRenderer.pos(maxX, minY, maxZ).endVertex()
        }
        tessellator.draw()
        GlStateManager.color(
            c.red / 255f * 0.9f,
            c.green / 255f * 0.9f,
            c.blue / 255f * 0.9f,
            c.alpha / 255f * alphaMultiplier,
        )
        // z
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
        with(effectiveAABB) {
            worldRenderer.pos(minX, maxY, minZ).endVertex()
            worldRenderer.pos(maxX, maxY, minZ).endVertex()
            worldRenderer.pos(maxX, minY, minZ).endVertex()
            worldRenderer.pos(minX, minY, minZ).endVertex()
            tessellator.draw()
            worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
            worldRenderer.pos(minX, minY, maxZ).endVertex()
            worldRenderer.pos(maxX, minY, maxZ).endVertex()
            worldRenderer.pos(maxX, maxY, maxZ).endVertex()
            worldRenderer.pos(minX, maxY, maxZ).endVertex()
        }
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.enableCull()
        GlStateManager.disableBlend()
    }

    fun LorenzRenderWorldEvent.outlineTopFace(
        boundingBox: AxisAlignedBB,
        lineWidth: Int,
        color: Color,
        depth: Boolean,
    ) {
        val (cornerOne, cornerTwo, cornerThree, cornerFour) = boundingBox.getCorners(boundingBox.maxY)
        this.draw3DLine(cornerOne, cornerTwo, color, lineWidth, depth)
        this.draw3DLine(cornerTwo, cornerThree, color, lineWidth, depth)
        this.draw3DLine(cornerThree, cornerFour, color, lineWidth, depth)
        this.draw3DLine(cornerFour, cornerOne, color, lineWidth, depth)
    }

    // TODO nea please merge with 'draw3DLine'
    fun LorenzRenderWorldEvent.draw3DLineNea(
        p1: LorenzVec,
        p2: LorenzVec,
        color: Color,
        lineWidth: Int,
        depth: Boolean,
    ) {
        val viewer = Minecraft.getMinecraft().renderViewEntity ?: return
        GlStateManager.disableCull()

        val worldRenderer = Tessellator.getInstance().worldRenderer
        val realX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * partialTicks
        val realY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * partialTicks
        val realZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * partialTicks
        GlStateManager.pushMatrix()
        GlStateManager.translate(-realX, -realY, -realZ)
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.disableAlpha()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0)
        GL11.glLineWidth(lineWidth.toFloat())
        if (!depth) {
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GlStateManager.depthMask(false)
        }
        GlStateManager.color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
        worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION)
        worldRenderer.pos(p1.x, p1.y, p1.z).endVertex()
        worldRenderer.pos(p2.x, p2.y, p2.z).endVertex()
        Tessellator.getInstance().draw()
        GlStateManager.translate(realX, realY, realZ)
        if (!depth) {
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GlStateManager.depthMask(true)
        }
        GlStateManager.disableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        GlStateManager.popMatrix()
        GlStateManager.disableLighting()
        GlStateManager.enableCull()
    }

    fun LorenzRenderWorldEvent.drawHitbox(
        boundingBox: AxisAlignedBB,
        lineWidth: Int,
        color: Color,
        depth: Boolean,
    ) {
        val cornersTop = boundingBox.getCorners(boundingBox.maxY)
        val cornersBottom = boundingBox.getCorners(boundingBox.minY)

        // Draw lines for the top and bottom faces
        for (i in 0..3) {
            this.draw3DLine(cornersTop[i], cornersTop[(i + 1) % 4], color, lineWidth, depth)
            this.draw3DLine(cornersBottom[i], cornersBottom[(i + 1) % 4], color, lineWidth, depth)
        }

        // Draw lines connecting the top and bottom faces
        for (i in 0..3) {
            this.draw3DLine(cornersBottom[i], cornersTop[i], color, lineWidth, depth)
        }
    }

    fun chromaColor(
        timeTillRepeat: Duration,
        offset: Float = 0f,
        saturation: Float = 1F,
        brightness: Float = 0.8F,
        timeOverride: Long = System.currentTimeMillis(),
    ): Color {
        return Color(
            Color.HSBtoRGB(
                ((offset + timeOverride / timeTillRepeat.toDouble(DurationUnit.MILLISECONDS)) % 1).toFloat(),
                saturation,
                brightness,
            ),
        )
    }

    fun GuiRenderItemEvent.RenderOverlayEvent.GuiRenderItemPost.drawSlotText(
        xPos: Int,
        yPos: Int,
        text: String,
        scale: Float,
    ) {
        RenderUtils.drawSlotText(xPos, yPos, text, scale)
    }

    fun GuiContainerEvent.ForegroundDrawnEvent.drawSlotText(
        xPos: Int,
        yPos: Int,
        text: String,
        scale: Float,
    ) {
        RenderUtils.drawSlotText(xPos, yPos, text, scale)
    }

    private fun drawSlotText(
        xPos: Int,
        yPos: Int,
        text: String,
        scale: Float,
    ) {
        val fontRenderer = Minecraft.getMinecraft().fontRendererObj

        GlStateManager.disableLighting()
        GlStateManager.disableDepth()
        GlStateManager.disableBlend()

        GlStateManager.pushMatrix()
        GlStateManager.translate((xPos - fontRenderer.getStringWidth(text)).toFloat(), yPos.toFloat(), 0f)
        GlStateManager.scale(scale, scale, 1f)
        fontRenderer.drawStringWithShadow(text, 0f, 0f, 16777215)

        val reverseScale = 1 / scale

        GlStateManager.scale(reverseScale, reverseScale, 1f)
        GlStateManager.popMatrix()

        GlStateManager.enableLighting()
        GlStateManager.enableDepth()
    }


    /**
     * Method to draw a rounded textured rect.
     *
     * **NOTE:** If you are using [GlStateManager.translate] or [GlStateManager.scale]
     * with this method, ensure they are invoked in the correct order if you use both. That is, [GlStateManager.translate]
     * is called **BEFORE** [GlStateManager.scale], otherwise the textured rect will not be rendered correctly
     *
     * @param filter the texture filter to use
     * @param radius the radius of the corners (default 10), NOTE: If you pass less than 1 it will just draw as a normal textured rect
     * @param smoothness how smooth the corners will appear (default 1). NOTE: This does very
     * little to the smoothness of the corners in reality due to how the final pixel color is calculated.
     * It is best kept at its default.
     */
    fun drawRoundTexturedRect(x: Int, y: Int, width: Int, height: Int, filter: Int, radius: Int = 10, smoothness: Int = 1) {
        // if radius is 0 then just draw a normal textured rect
        if (radius <= 0) {
            GuiRenderUtils.drawTexturedRect(x, y, width, height, filter = filter)
            return
        }

        val scaleFactor = ScaledResolution(Minecraft.getMinecraft()).scaleFactor
        val widthIn = width * scaleFactor
        val heightIn = height * scaleFactor
        val xIn = x * scaleFactor
        val yIn = y * scaleFactor

        RoundedTextureShader.scaleFactor = scaleFactor.toFloat()
        RoundedTextureShader.radius = radius.toFloat()
        RoundedTextureShader.smoothness = smoothness.toFloat()
        RoundedTextureShader.halfSize = floatArrayOf(widthIn / 2f, heightIn / 2f)
        RoundedTextureShader.centerPos = floatArrayOf(xIn + (widthIn / 2f), yIn + (heightIn / 2f))

        GlStateManager.pushMatrix()
        ShaderManager.enableShader(ShaderManager.Shaders.ROUNDED_TEXTURE)

        GuiRenderUtils.drawTexturedRect(x, y, width, height, filter = filter)

        ShaderManager.disableShader()
        GlStateManager.popMatrix()
    }

    /**
     * Method to draw a rounded rectangle.
     *
     * **NOTE:** If you are using [GlStateManager.translate] or [GlStateManager.scale]
     * with this method, ensure they are invoked in the correct order if you use both. That is, [GlStateManager.translate]
     * is called **BEFORE** [GlStateManager.scale], otherwise the rectangle will not be rendered correctly
     *
     * @param color color of rect
     * @param radius the radius of the corners (default 10)
     * @param smoothness how smooth the corners will appear (default 1). NOTE: This does very
     * little to the smoothness of the corners in reality due to how the final pixel color is calculated.
     * It is best kept at its default.
     */
    fun drawRoundRect(x: Int, y: Int, width: Int, height: Int, color: Int, radius: Int = 10, smoothness: Int = 1) {
        val scaleFactor = ScaledResolution(Minecraft.getMinecraft()).scaleFactor
        val widthIn = width * scaleFactor
        val heightIn = height * scaleFactor
        val xIn = x * scaleFactor
        val yIn = y * scaleFactor

        RoundedRectangleShader.scaleFactor = scaleFactor.toFloat()
        RoundedRectangleShader.radius = radius.toFloat()
        RoundedRectangleShader.smoothness = smoothness.toFloat()
        RoundedRectangleShader.halfSize = floatArrayOf(widthIn / 2f, heightIn / 2f)
        RoundedRectangleShader.centerPos = floatArrayOf(xIn + (widthIn / 2f), yIn + (heightIn / 2f))

        GlStateManager.pushMatrix()
        ShaderManager.enableShader(ShaderManager.Shaders.ROUNDED_RECTANGLE)

        Gui.drawRect(x - 5, y - 5, x + width + 5, y + height + 5, color)

        ShaderManager.disableShader()
        GlStateManager.popMatrix()
    }

    /**
     * Method to draw the outline of a rounded rectangle with a color gradient. For a single color just pass
     * in the color to both topColor and bottomColor.
     *
     * This is *not* a method that draws a rounded rectangle **with** an outline, rather, this draws **only** the outline.
     *
     * **NOTE:** The same notices given from [drawRoundRect] should be acknowledged with this method also.
     *
     * @param topColor color of the top of the outline
     * @param bottomColor color of the bottom of the outline
     * @param borderThickness the thickness of the border
     * @param radius radius of the corners of the rectangle (default 10)
     * @param blur the amount to blur the outline (default 0.7f)
     */
    fun drawRoundRectOutline(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        topColor: Int,
        bottomColor: Int,
        borderThickness: Int,
        radius: Int = 10,
        blur: Float = 0.7f,
    ) {
        val scaleFactor = ScaledResolution(Minecraft.getMinecraft()).scaleFactor
        val widthIn = width * scaleFactor
        val heightIn = height * scaleFactor
        val xIn = x * scaleFactor
        val yIn = y * scaleFactor

        val borderAdjustment = borderThickness / 2

        RoundedRectangleOutlineShader.scaleFactor = scaleFactor.toFloat()
        RoundedRectangleOutlineShader.radius = radius.toFloat()
        RoundedRectangleOutlineShader.halfSize = floatArrayOf(widthIn / 2f, heightIn / 2f)
        RoundedRectangleOutlineShader.centerPos = floatArrayOf(xIn + (widthIn / 2f), yIn + (heightIn / 2f))
        RoundedRectangleOutlineShader.borderThickness = borderThickness.toFloat()
        // The blur argument is a bit misleading, the greater the value the more sharp the edges of the
        // outline will be and the smaller the value the blurrier. So we take the difference from 1
        // so the shader can blur the edges accordingly. This is because a 'blurriness' option makes more sense
        // to users than a 'sharpness' option in this context
        RoundedRectangleOutlineShader.borderBlur = max(1 - blur, 0f)

        GlStateManager.pushMatrix()
        ShaderManager.enableShader(ShaderManager.Shaders.ROUNDED_RECT_OUTLINE)

        drawGradientRect(
            x - borderAdjustment,
            y - borderAdjustment,
            x + width + borderAdjustment,
            y + height + borderAdjustment,
            topColor,
            bottomColor,
        )

        ShaderManager.disableShader()
        GlStateManager.popMatrix()
    }

    // todo merge with the one in GuiRenderUtils
    fun drawGradientRect(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        startColor: Int = -0xfeffff0,
        endColor: Int = -0xfeffff0,
    ) {
        val startAlpha = (startColor shr 24 and 255).toFloat() / 255.0f
        val startRed = (startColor shr 16 and 255).toFloat() / 255.0f
        val startGreen = (startColor shr 8 and 255).toFloat() / 255.0f
        val startBlue = (startColor and 255).toFloat() / 255.0f
        val endAlpha = (endColor shr 24 and 255).toFloat() / 255.0f
        val endRed = (endColor shr 16 and 255).toFloat() / 255.0f
        val endGreen = (endColor shr 8 and 255).toFloat() / 255.0f
        val endBlue = (endColor and 255).toFloat() / 255.0f
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.disableAlpha()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0)
        GlStateManager.shadeModel(7425)
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        worldrenderer.pos(right.toDouble(), top.toDouble(), 0.0)
            .color(startRed, startGreen, startBlue, startAlpha).endVertex()
        worldrenderer.pos(left.toDouble(), top.toDouble(), 0.0)
            .color(startRed, startGreen, startBlue, startAlpha).endVertex()
        worldrenderer.pos(left.toDouble(), bottom.toDouble(), 0.0)
            .color(endRed, endGreen, endBlue, endAlpha).endVertex()
        worldrenderer.pos(right.toDouble(), bottom.toDouble(), 0.0)
            .color(endRed, endGreen, endBlue, endAlpha).endVertex()
        tessellator.draw()
        GlStateManager.shadeModel(7424)
        GlStateManager.disableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
    }

    fun getAlpha(): Float {
        colorBuffer.clear()
        GlStateManager.getFloat(GL11.GL_CURRENT_COLOR, colorBuffer)
        if (colorBuffer.limit() < 4) return 1f
        return colorBuffer.get(3)
    }
}
