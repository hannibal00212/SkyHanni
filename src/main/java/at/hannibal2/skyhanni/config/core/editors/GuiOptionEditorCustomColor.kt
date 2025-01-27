package at.hannibal2.skyhanni.config.core.editors

import at.hannibal2.skyhanni.config.core.config.CustomColor
import io.github.notenoughupdates.moulconfig.GuiTextures
import io.github.notenoughupdates.moulconfig.gui.GuiComponent
import io.github.notenoughupdates.moulconfig.gui.GuiImmediateContext
import io.github.notenoughupdates.moulconfig.gui.MouseEvent
import io.github.notenoughupdates.moulconfig.gui.component.ColorSelectComponent
import io.github.notenoughupdates.moulconfig.gui.editors.ComponentEditor
import io.github.notenoughupdates.moulconfig.processor.ProcessedOption

/**
 * Code adapted from MoulConfig
 */
class GuiOptionEditorCustomColor(option: ProcessedOption) : ComponentEditor(option) {
    private val component: GuiComponent = wrapComponent(
        object : GuiComponent() {
            override fun getWidth(): Int = 48

            override fun getHeight(): Int = 16

            override fun render(context: GuiImmediateContext) {
                val argb = get().toInt()
                val r = (argb shr 16) and 0xFF
                val g = (argb shr 8) and 0xFF
                val b = argb and 0xFF

                context.renderContext.apply {
                    color(r / 255f, g / 255f, b / 255f, 1f)
                    bindTexture(GuiTextures.BUTTON_WHITE)
                    drawTexturedRect(0f, 0f, context.width.toFloat(), context.height.toFloat())
                    color(1f, 1f, 1f, 1f)
                }
            }

            override fun mouseEvent(mouseEvent: MouseEvent, context: GuiImmediateContext): Boolean {
                if (mouseEvent !is MouseEvent.Click || !mouseEvent.mouseState || mouseEvent.mouseButton != 0 || !context.isHovered)
                    return false
                openOverlay(
                    ColorSelectComponent(0, 0, get().asString, ::set, ::closeOverlay),
                    context.absoluteMouseX, context.absoluteMouseY,
                )
                return true
            }
        },
    )

    private fun get(): CustomColor = option.get() as CustomColor

    private fun set(newString: String) {
        option.set(CustomColor(newString))
    }


    override fun getDelegate(): GuiComponent = component
}
