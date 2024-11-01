package at.hannibal2.skyhanni.features.gui.customscoreboard

import at.hannibal2.skyhanni.config.features.gui.customscoreboard.BackgroundConfig
import at.hannibal2.skyhanni.data.GuiEditManager
import at.hannibal2.skyhanni.data.GuiEditManager.getAbsX
import at.hannibal2.skyhanni.data.GuiEditManager.getAbsY
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.backgroundConfig
import at.hannibal2.skyhanni.utils.ColorUtils.toChromaColor
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.compat.GuiScreenUtils
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraft.util.ResourceLocation

object RenderBackground {

    private val textureLocation by lazy { ResourceLocation("skyhanni", "scoreboard.png") }

    internal fun addBackground(renderable: Renderable): Renderable {
        with(backgroundConfig) {
            if (!backgroundConfig.enabled) return renderable

            val backgroundRenderable = createBackground(renderable)

            if (!outline.enabled) return backgroundRenderable

            return Renderable.drawInsideRoundedRectOutline(
                backgroundRenderable,
                0,
                backgroundConfig.roundedCornerSmoothness,
                1,
                outline.colorTop.toChromaColor().rgb,
                outline.colorBottom.toChromaColor().rgb,
                outline.thickness,
                outline.blur,
                horizontalAlign = RenderUtils.HorizontalAlignment.CENTER,
                verticalAlign = RenderUtils.VerticalAlignment.CENTER,
            )
        }
    }

    private fun BackgroundConfig.createBackground(renderable: Renderable): Renderable =
        if (backgroundConfig.useCustomBackgroundImage) {
            Renderable.drawInsideImage(
                renderable,
                textureLocation,
                (backgroundConfig.customBackgroundImageOpacity * 255) / 100,
                borderSize,
                horizontalAlign = RenderUtils.HorizontalAlignment.CENTER,
                verticalAlign = RenderUtils.VerticalAlignment.CENTER,
                radius = backgroundConfig.roundedCornerSmoothness,
            )
        } else {
            Renderable.drawInsideRoundedRect(
                renderable,
                backgroundConfig.color.toChromaColor(),
                borderSize,
                backgroundConfig.roundedCornerSmoothness,
                1,
                horizontalAlign = RenderUtils.HorizontalAlignment.CENTER,
                verticalAlign = RenderUtils.VerticalAlignment.CENTER,
            )
        }

    internal fun updatePosition(renderable: Renderable) {
        if (GuiEditManager.isInGui()) return
        val alignmentConfig = CustomScoreboard.alignmentConfig

        with(alignmentConfig) {
            if (horizontalAlignment == RenderUtils.HorizontalAlignment.DONT_ALIGN &&
                verticalAlignment == RenderUtils.VerticalAlignment.DONT_ALIGN
            ) return
        }

        val position = CustomScoreboard.config.position

        val scaledWidth = GuiScreenUtils.scaledWindowWidth
        val scaledHeight = GuiScreenUtils.scaledWindowHeight
        val elementWidth = (renderable.width * position.effectiveScale).toInt()
        val elementHeight = (renderable.height * position.effectiveScale).toInt()

        with(alignmentConfig) {
            var x = when (horizontalAlignment) {
                RenderUtils.HorizontalAlignment.DONT_ALIGN -> position.getAbsX()
                RenderUtils.HorizontalAlignment.LEFT -> 0 + margin
                RenderUtils.HorizontalAlignment.CENTER -> scaledWidth / 2 - elementWidth / 2
                RenderUtils.HorizontalAlignment.RIGHT -> scaledWidth - elementWidth - margin
                else -> 0
            }
            var y = when (verticalAlignment) {
                RenderUtils.VerticalAlignment.DONT_ALIGN -> position.getAbsY()
                RenderUtils.VerticalAlignment.TOP -> 0 + margin
                RenderUtils.VerticalAlignment.CENTER -> scaledHeight / 2 - elementHeight / 2
                RenderUtils.VerticalAlignment.BOTTOM -> scaledHeight - elementHeight - margin
                else -> 0
            }

            val outlineConfig = backgroundConfig.outline
            if (outlineConfig.enabled) {
                val thickness = outlineConfig.thickness

                when (horizontalAlignment) {
                    RenderUtils.HorizontalAlignment.RIGHT -> x -= thickness / 2
                    RenderUtils.HorizontalAlignment.LEFT -> x += thickness / 2
                    else -> x
                }

                when (verticalAlignment) {
                    RenderUtils.VerticalAlignment.TOP -> y += thickness / 2
                    RenderUtils.VerticalAlignment.BOTTOM -> y -= thickness / 2
                    else -> y
                }
            }
            CustomScoreboard.config.position.moveTo(x, y)
        }
    }
}
