package at.hannibal2.skyhanni.features.misc.massconfiguration

import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.RenderableTooltips
import io.github.notenoughupdates.moulconfig.internal.GlScissorStack
import io.github.notenoughupdates.moulconfig.internal.RenderUtils
import io.github.notenoughupdates.moulconfig.internal.TextRenderUtils
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.input.Mouse
import kotlin.math.max
import kotlin.math.min

class DefaultConfigOptionGui(
    private val orderedOptions: Map<Category, List<FeatureToggleableOption>>,
    old: String,
    new: String,
) :
    GuiScreen() {

    val title = if (old == "null") {
        if (new == "null")
            "§5SkyHanni Default Options"
        else
            "§5SkyHanni Options In Version $new"
    } else {
        if (new == "null")
            "§5SkyHanni Options since $old"
        else
            "§5SkyHanni Options $old → $new"
    }

    private val xSize = 400
    private val ySize = 300
    private val barSize = 40
    private val padding = 10
    private var wasMouseDown = false
    private val cardHeight = 30

    private var currentScrollOffset = 0

    private val resetSuggestionState =
        orderedOptions.keys.associateWith { ResetSuggestionState.LEAVE_DEFAULTS }.toMutableMap()

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.drawScreen(mouseX, mouseY, partialTicks)
        drawDefaultBackground()
        RenderUtils.drawFloatingRectDark((width - xSize) / 2, (height - ySize) / 2, xSize, ySize)
        val scaledResolution = ScaledResolution(mc)
        var hoveringTextToDraw: List<String>? = null
        val x = mouseX - ((width - xSize) / 2) - padding
        val isMouseDown = Mouse.isButtonDown(0)
        val shouldClick = isMouseDown && !wasMouseDown
        wasMouseDown = isMouseDown
        val isMouseInScrollArea =
            x in 0..xSize && mouseY in ((height - ySize) / 2) + barSize..((height + ySize) / 2 - barSize)
        var y = mouseY - ((height - ySize) / 2 + barSize) + currentScrollOffset

        GlStateManager.pushMatrix()
        GlStateManager.translate(width / 2F, (height - ySize) / 2F, 0F)
        GlStateManager.scale(2f, 2f, 1f)
        TextRenderUtils.drawStringCenteredScaledMaxWidth(
            title,
            mc.fontRendererObj,
            0F,
            mc.fontRendererObj.FONT_HEIGHT.toFloat(),
            false,
            xSize / 2 - padding,
            -1,
        )
        GlStateManager.popMatrix()

        GlStateManager.pushMatrix()
        GlStateManager.translate(
            (width - xSize) / 2F + padding,
            (height + ySize) / 2F - mc.fontRendererObj.FONT_HEIGHT * 2,
            0F,
        )
        var i = 0
        fun button(title: String, tooltip: List<String>, func: () -> Unit) {
            val width = mc.fontRendererObj.getStringWidth(title)
            var overMouse = false
            if (mouseX - ((this.width - xSize) / 2 + padding) in i..(i + width) &&
                mouseY - (height + ySize) / 2 in -barSize..0
            ) {
                overMouse = true
                hoveringTextToDraw = tooltip
                if (shouldClick) {
                    func()
                }
            }
            RenderUtils.drawFloatingRectDark(i - 1, -3, width + 4, 14)
            mc.fontRendererObj.drawString(
                title,
                2 + i.toFloat(),
                0F,
                if (overMouse) 0xFF00FF00.toInt() else -1,
                overMouse,
            )
            i += width + 12
        }
        button("Apply choices", listOf()) {
            DefaultConfigFeatures.applyCategorySelections(resetSuggestionState, orderedOptions)
            mc.displayGuiScreen(null)
        }
        button("Turn all on", listOf()) {
            for (entry in resetSuggestionState.entries) {
                entry.setValue(ResetSuggestionState.TURN_ALL_ON)
                orderedOptions[entry.key]!!.forEach { it.toggleOverride = null }
            }
        }
        button("Turn all off", listOf()) {
            for (entry in resetSuggestionState.entries) {
                entry.setValue(ResetSuggestionState.TURN_ALL_OFF)
                orderedOptions[entry.key]!!.forEach { it.toggleOverride = null }
            }
        }
        button("Leave all untouched", listOf()) {
            for (entry in resetSuggestionState.entries) {
                entry.setValue(ResetSuggestionState.LEAVE_DEFAULTS)
                orderedOptions[entry.key]!!.forEach { it.toggleOverride = null }
            }
        }
        button("Cancel", listOf()) {
            mc.displayGuiScreen(null)
        }
        GlStateManager.popMatrix()

        GlStateManager.pushMatrix()
        GlScissorStack.push(
            (width - xSize) / 2,
            (height - ySize) / 2 + barSize,
            (width + xSize) / 2,
            (height + ySize) / 2 - barSize,
            scaledResolution,
        )
        GlStateManager.translate(
            (width - xSize) / 2F + padding,
            (height - ySize) / 2F + barSize - currentScrollOffset,
            0F,
        )

        for ((cat) in orderedOptions.entries) {
            val suggestionState = resetSuggestionState[cat]!!

            drawRect(0, 0, xSize - padding * 2, 1, 0xFF808080.toInt())
            drawRect(0, 30, xSize - padding * 2, cardHeight + 1, 0xFF808080.toInt())
            drawRect(0, 0, 1, cardHeight, 0xFF808080.toInt())
            drawRect(xSize - padding * 2 - 1, 0, xSize - padding * 2, cardHeight, 0xFF808080.toInt())

            mc.fontRendererObj.drawString("§e${cat.name} ${suggestionState.label}", 4, 4, -1)
            mc.fontRendererObj.drawSplitString("§7${cat.description}", 4, 14, xSize - padding * 2 - 8, -1)

            if (isMouseInScrollArea && y in 0..cardHeight) {
                hoveringTextToDraw = listOf(
                    "§e${cat.name}",
                    "§7${cat.description}",
                    "§7Current plan: ${suggestionState.label}",
                    "§aClick to toggle!",
                    "§7Hold shift to show all options",
                )

                if (isShiftKeyDown()) {
                    hoveringTextToDraw = listOf(
                        "§e${cat.name}",
                        "§7${cat.description}",
                    ) + orderedOptions[cat]!!.map { "§7 - §a" + it.name }
                }

                if (shouldClick) {
                    resetSuggestionState[cat] = suggestionState.next
                    orderedOptions[cat]!!.forEach { it.toggleOverride = null }
                }
            }

            y -= cardHeight
            GlStateManager.translate(0F, cardHeight.toFloat(), 0F)
        }

        GlStateManager.popMatrix()
        GlScissorStack.pop(scaledResolution)
        hoveringTextToDraw?.let { tooltip ->
            RenderableTooltips.setTooltipForRender(tooltip.map { Renderable.string(it) })
        }
    }

    private fun scroll(s: Int) {
        currentScrollOffset =
            max(0, min(s, (orderedOptions.size + 1) * cardHeight - ySize + barSize + padding * 2))
    }

    override fun handleMouseInput() {
        super.handleMouseInput()
        if (Mouse.getEventDWheel() != 0)
            scroll(currentScrollOffset - Mouse.getEventDWheel())
    }
}
