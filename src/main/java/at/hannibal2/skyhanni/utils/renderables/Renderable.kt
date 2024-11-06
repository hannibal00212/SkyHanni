package at.hannibal2.skyhanni.utils.renderables

import at.hannibal2.skyhanni.config.core.config.gui.GuiPositionEditor
import at.hannibal2.skyhanni.config.features.skillprogress.SkillProgressBarConfig
import at.hannibal2.skyhanni.data.GuiData
import at.hannibal2.skyhanni.data.HighlightOnHoverSlot
import at.hannibal2.skyhanni.data.ToolTipData
import at.hannibal2.skyhanni.data.model.TextInput
import at.hannibal2.skyhanni.features.chroma.ChromaShaderManager
import at.hannibal2.skyhanni.features.chroma.ChromaType
import at.hannibal2.skyhanni.features.misc.DarkenShader
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.utils.CollectionUtils.contains
import at.hannibal2.skyhanni.utils.ColorUtils
import at.hannibal2.skyhanni.utils.ColorUtils.addAlpha
import at.hannibal2.skyhanni.utils.ColorUtils.darker
import at.hannibal2.skyhanni.utils.ColorUtils.withAlpha
import at.hannibal2.skyhanni.utils.GuiRenderUtils
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyClicked
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzLogger
import at.hannibal2.skyhanni.utils.NEUItems
import at.hannibal2.skyhanni.utils.NEUItems.renderOnScreen
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.RenderUtils.HorizontalAlignment
import at.hannibal2.skyhanni.utils.RenderUtils.VerticalAlignment
import at.hannibal2.skyhanni.utils.compat.getTooltipCompat
import at.hannibal2.skyhanni.utils.guide.GuideGUI
import at.hannibal2.skyhanni.utils.renderables.RenderableUtils.calculateTableXOffsets
import at.hannibal2.skyhanni.utils.renderables.RenderableUtils.calculateTableYOffsets
import at.hannibal2.skyhanni.utils.renderables.RenderableUtils.renderXAligned
import at.hannibal2.skyhanni.utils.renderables.RenderableUtils.renderXYAligned
import at.hannibal2.skyhanni.utils.renderables.RenderableUtils.renderYAligned
import at.hannibal2.skyhanni.utils.shader.ShaderManager
import io.github.notenoughupdates.moulconfig.gui.GuiScreenElementWrapper
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiIngameMenu
import net.minecraft.client.gui.inventory.GuiEditSign
import net.minecraft.client.gui.inventory.GuiInventory.drawEntityOnScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.Collections
import java.util.UUID
import kotlin.math.max

interface Renderable {

    val width: Int
    val height: Int

    val horizontalAlign: HorizontalAlignment
    val verticalAlign: VerticalAlignment
    fun isHovered(posX: Int, posY: Int) = currentRenderPassMousePosition?.let { (x, y) ->
        x in (posX..posX + width) && y in (posY..posY + height)
    } ?: false

    fun isBoxHovered(posX: Int, width: Int, posY: Int, height: Int) = currentRenderPassMousePosition?.let { (x, y) ->
        x in (posX..posX + width) && y in (posY..posY + height)
    } ?: false

    /**
     * Pos x and pos y are relative to the mouse position.
     * (the GL matrix stack should already be pre transformed)
     */
    fun render(posX: Int, posY: Int)

    companion object {

        val logger = LorenzLogger("debug/renderable")
        var currentRenderPassMousePosition: Pair<Int, Int>? = null
            set

        fun <T> withMousePosition(posX: Int, posY: Int, block: () -> T): T {
            val last = currentRenderPassMousePosition
            try {
                currentRenderPassMousePosition = Pair(posX, posY)
                return block()
            } finally {
                currentRenderPassMousePosition = last
            }
        }

        fun fromAny(any: Any?, itemScale: Double = NEUItems.itemFontSize): Renderable? = when (any) {
            null -> placeholder(12)
            is Renderable -> any
            is String -> string(any)
            is ItemStack -> itemStack(any, itemScale)
            else -> null
        }

        fun link(text: String, bypassChecks: Boolean = false, onClick: () -> Unit): Renderable =
            link(string(text), onClick, bypassChecks = bypassChecks)

        fun optionalLink(
            text: String,
            onClick: () -> Unit,
            bypassChecks: Boolean = false,
            highlightsOnHoverSlots: List<Int> = emptyList(),
            condition: () -> Boolean = { true },
        ): Renderable = link(string(text), onClick, bypassChecks, highlightsOnHoverSlots = highlightsOnHoverSlots, condition)

        fun link(
            renderable: Renderable,
            onClick: () -> Unit,
            bypassChecks: Boolean = false,
            highlightsOnHoverSlots: List<Int> = emptyList(),
            condition: () -> Boolean = { true },
            underlineColor: Color = Color.WHITE,
        ): Renderable {
            return clickable(
                hoverable(
                    underlined(renderable, underlineColor), renderable, bypassChecks,
                    condition = condition,
                    highlightsOnHoverSlots = highlightsOnHoverSlots,
                ),
                onClick,
                0,
                bypassChecks,
                condition,
            )
        }

        fun clickAndHover(
            text: Any,
            tips: List<Any>,
            bypassChecks: Boolean = false,
            onClick: () -> Unit,
            onHover: () -> Unit = {},
        ): Renderable {
            return clickable(
                hoverTips(text, tips, bypassChecks = bypassChecks, onHover = onHover),
                onClick,
                bypassChecks = bypassChecks,
            )
        }

        fun multiClickAndHover(
            text: Any,
            tips: List<Any>,
            bypassChecks: Boolean = false,
            click: Map<Int, () -> Unit>,
            onHover: () -> Unit = {},
        ): Renderable {
            return multiClickable(
                hoverTips(text, tips, bypassChecks = bypassChecks, onHover = onHover),
                click,
                bypassChecks = bypassChecks,
            )
        }

        fun clickable(
            render: Renderable,
            onClick: () -> Unit,
            button: Int = 0,
            bypassChecks: Boolean = false,
            condition: () -> Boolean = { true },
        ) = object : Renderable {
            override val width = render.width
            override val height = render.height
            override val horizontalAlign = render.horizontalAlign
            override val verticalAlign = render.verticalAlign

            override fun render(posX: Int, posY: Int) {
                if (isHovered(posX, posY) && condition() && shouldAllowLink(true, bypassChecks) && (button - 100).isKeyClicked()) {
                    onClick()
                }
                render.render(posX, posY)
            }
        }

        fun multiClickable(
            render: Renderable,
            click: Map<Int, () -> Unit>,
            bypassChecks: Boolean = false,
            condition: () -> Boolean = { true },
        ) = object : Renderable {
            override val width = render.width
            override val height = render.height
            override val horizontalAlign = render.horizontalAlign
            override val verticalAlign = render.verticalAlign

            override fun render(posX: Int, posY: Int) {
                if (isHovered(posX, posY) && condition() && shouldAllowLink(true, bypassChecks)) for ((button, onClick) in click) {
                    if ((button - 100).isKeyClicked()) onClick()
                }
                render.render(posX, posY)
            }
        }

        fun hoverTips(
            content: Any,
            tips: List<Any>,
            highlightsOnHoverSlots: List<Int> = listOf(),
            stack: ItemStack? = null,
            color: LorenzColor? = null,
            spacedTitle: Boolean = false,
            bypassChecks: Boolean = false,
            snapsToTopIfToLong: Boolean = true,
            condition: () -> Boolean = { true },
            onHover: () -> Unit = {},
        ): Renderable {

            val render = fromAny(content) ?: string("Error")
            return object : Renderable {
                override val width = render.width
                override val height = render.height
                override val horizontalAlign = render.horizontalAlign
                override val verticalAlign = render.verticalAlign

                val tipsRender = tips.mapNotNull { fromAny(it) }

                override fun render(posX: Int, posY: Int) {
                    render.render(posX, posY)
                    val pair = Pair(posX, posY)
                    if (isHovered(posX, posY)) {
                        if (condition() && shouldAllowLink(true, bypassChecks)) {
                            onHover.invoke()
                            HighlightOnHoverSlot.currentSlots[pair] = highlightsOnHoverSlots
                            GlStateManager.pushMatrix()
                            GlStateManager.translate(0F, 0F, 400F)

                            RenderableTooltips.setTooltipForRender(
                                tips = tipsRender,
                                stack = stack,
                                borderColor = color,
                                snapsToTopIfToLong = snapsToTopIfToLong,
                                spacedTitle = spacedTitle,
                            )
                            GlStateManager.popMatrix()
                        }
                    } else {
                        HighlightOnHoverSlot.currentSlots.remove(pair)
                    }
                }
            }
        }

        internal fun shouldAllowLink(debug: Boolean = false, bypassChecks: Boolean): Boolean {
            val guiScreen = Minecraft.getMinecraft().currentScreen

            val isGuiScreen = guiScreen != null
            if (bypassChecks) {
                return isGuiScreen
            }
            val inMenu = Minecraft.getMinecraft().currentScreen !is GuiIngameMenu
            val isGuiPositionEditor = guiScreen !is GuiPositionEditor
            val isNotInSignAndOnSlot = if (guiScreen !is GuiEditSign && guiScreen !is GuideGUI<*>) {
                ToolTipData.lastSlot == null || GuiData.preDrawEventCancelled
            } else true
            val isConfigScreen = guiScreen !is GuiScreenElementWrapper

            val openGui = guiScreen?.javaClass?.name ?: "none"
            val isInNeuPv = openGui == "io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer"
            val neuFocus = NEUItems.neuHasFocus()
            val isInSkytilsPv = openGui == "gg.skytils.skytilsmod.gui.profile.ProfileGui"
            val isInSkytilsSettings =
                openGui.let { it.startsWith("gg.skytils.vigilance.gui.") || it.startsWith("gg.skytils.skytilsmod.gui.") }
            val isInNeuSettings = openGui.startsWith("io.github.moulberry.notenoughupdates.")

            val result =
                isGuiScreen &&
                    isGuiPositionEditor &&
                    inMenu &&
                    isNotInSignAndOnSlot &&
                    isConfigScreen &&
                    !isInNeuPv &&
                    !isInSkytilsPv &&
                    !neuFocus &&
                    !isInSkytilsSettings &&
                    !isInNeuSettings

            if (debug) {
                if (!result) {
                    logger.log("")
                    logger.log("blocked link because:")
                    if (!isGuiScreen) logger.log("isGuiScreen")
                    if (!isGuiPositionEditor) logger.log("isGuiPositionEditor")
                    if (!inMenu) logger.log("inMenu")
                    if (!isNotInSignAndOnSlot) logger.log("isNotInSignAndOnSlot")
                    if (!isConfigScreen) logger.log("isConfigScreen")
                    if (isInNeuPv) logger.log("isInNeuPv")
                    if (neuFocus) logger.log("neuFocus")
                    if (isInSkytilsPv) logger.log("isInSkytilsPv")
                    if (isInSkytilsSettings) logger.log("isInSkytilsSettings")
                    if (isInNeuSettings) logger.log("isInNeuSettings")
                    logger.log("")
                } else {
                    logger.log("allowed click")
                }
            }

            return result
        }

        fun underlined(renderable: Renderable, color: Color = Color.WHITE) = object : Renderable {
            override val width: Int
                get() = renderable.width
            override val height: Int
                get() = renderable.height + 1
            override val horizontalAlign = renderable.horizontalAlign
            override val verticalAlign = renderable.verticalAlign

            override fun render(posX: Int, posY: Int) {
                Gui.drawRect(0, height, width, 11, color.rgb)
                GlStateManager.color(1F, 1F, 1F, 1F)
                renderable.render(posX, posY)
            }
        }

        fun hoverable(
            hovered: Renderable,
            unhovered: Renderable,
            bypassChecks: Boolean = false,
            condition: () -> Boolean = { true },
            highlightsOnHoverSlots: List<Int> = emptyList(),
            onHover: () -> Unit = {},
        ) = object : Renderable {
            override val width = max(hovered.width, unhovered.width)
            override val height = max(hovered.height, unhovered.height)
            override val horizontalAlign get() = if (isHovered) hovered.horizontalAlign else unhovered.horizontalAlign
            override val verticalAlign get() = if (isHovered) hovered.verticalAlign else unhovered.verticalAlign

            var isHovered = false

            override fun render(posX: Int, posY: Int) {
                val pair = Pair(posX, posY)
                isHovered = if (isHovered(posX, posY) && condition() && shouldAllowLink(true, bypassChecks)) {
                    onHover()
                    hovered.render(posX, posY)
                    HighlightOnHoverSlot.currentSlots[pair] = highlightsOnHoverSlots
                    true
                } else {
                    unhovered.render(posX, posY)
                    HighlightOnHoverSlot.currentSlots.remove(pair)
                    false
                }
            }
        }

        /** Bottom Layer must be bigger then the top layer */
        fun doubleLayered(
            bottomLayer: Renderable,
            topLayer: Renderable,
            blockBottomHover: Boolean = true,
        ) = object : Renderable {
            override val width = bottomLayer.width
            override val height = bottomLayer.height
            override val horizontalAlign = bottomLayer.horizontalAlign
            override val verticalAlign = bottomLayer.verticalAlign

            override fun render(posX: Int, posY: Int) {
                val (x, y) = topLayer.renderXYAligned(posX, posY, width, height)
                val (posX, posY) = if (topLayer.isHovered(posX + x, posY + y) && blockBottomHover) {
                    bottomLayer.width + 1 to bottomLayer.height + 1
                } else {
                    posX to posY
                }
                bottomLayer.render(posX, posY)
            }
        }

        fun itemStackWithTip(
            item: ItemStack,
            scale: Double = NEUItems.itemFontSize,
            xSpacing: Int = 2,
            ySpacing: Int = 0,
            rescaleSkulls: Boolean = true,
            horizontalAlign: HorizontalAlignment = HorizontalAlignment.LEFT,
            verticalAlign: VerticalAlignment = VerticalAlignment.TOP,
        ) = hoverTips(
            itemStack(
                item,
                scale,
                xSpacing,
                ySpacing,
                rescaleSkulls,
                horizontalAlign = horizontalAlign,
                verticalAlign = verticalAlign,
            ),
            item.getTooltipCompat(false),
            stack = item,
        )

        fun itemStack(
            item: ItemStack,
            scale: Double = NEUItems.itemFontSize,
            xSpacing: Int = 2,
            ySpacing: Int = 1,
            rescaleSkulls: Boolean = true,
            horizontalAlign: HorizontalAlignment = HorizontalAlignment.LEFT,
            verticalAlign: VerticalAlignment = VerticalAlignment.CENTER,
        ) = object : Renderable {
            override val width = (15.5 * scale + 0.5).toInt() + xSpacing
            override val height = (15.5 * scale + 0.5).toInt() + ySpacing
            override val horizontalAlign = horizontalAlign
            override val verticalAlign = verticalAlign

            override fun render(posX: Int, posY: Int) {
                item.renderOnScreen(xSpacing / 2.0f, 0F, scaleMultiplier = scale, rescaleSkulls)
            }
        }

        fun Renderable.darken(amount: Float = 1.0f) = object : Renderable {
            override val width = this@darken.width
            override val height = this@darken.height
            override val horizontalAlign = this@darken.horizontalAlign
            override val verticalAlign = this@darken.verticalAlign

            override fun render(posX: Int, posY: Int) {
                DarkenShader.darknessLevel = amount
                ShaderManager.enableShader(ShaderManager.Shaders.DARKEN)
                this@darken.render(posX, posY)
                ShaderManager.disableShader()
            }
        }

        fun singeltonString(string: String): List<Renderable> {
            return Collections.singletonList(string(string))
        }

        fun string(
            text: String,
            scale: Double = 1.0,
            color: Color = Color.WHITE,
            horizontalAlign: HorizontalAlignment = HorizontalAlignment.LEFT,
            verticalAlign: VerticalAlignment = VerticalAlignment.CENTER,
        ) = object : Renderable {

            override val width by lazy { (Minecraft.getMinecraft().fontRendererObj.getStringWidth(text) * scale).toInt() + 1 }
            override val height = (9 * scale).toInt() + 1
            override val horizontalAlign = horizontalAlign
            override val verticalAlign = verticalAlign

            val inverseScale = 1 / scale

            override fun render(posX: Int, posY: Int) {
                RenderableUtils.renderString(text, scale, color, inverseScale)
            }
        }

        fun wrappedString(
            text: String,
            width: Int,
            scale: Double = 1.0,
            color: Color = Color.WHITE,
            horizontalAlign: HorizontalAlignment = HorizontalAlignment.LEFT,
            verticalAlign: VerticalAlignment = VerticalAlignment.CENTER,
            internalAlign: HorizontalAlignment = HorizontalAlignment.LEFT,
        ) = object : Renderable {

            val fontRenderer by lazy { Minecraft.getMinecraft().fontRendererObj }

            val map by lazy {
                fontRenderer.listFormattedStringToWidth(
                    text, (width / scale).toInt(),
                ).associateWith { fontRenderer.getStringWidth(it) }
            }

            override val width by lazy { (rawWidth * scale).toInt() + 1 }

            val rawWidth by lazy {
                if (map.size == 1)
                    map.entries.first().value
                else
                    map.maxOf { it.value }
            }

            override val height by lazy { map.size * ((9 * scale).toInt() + 1) }
            override val horizontalAlign = horizontalAlign
            override val verticalAlign = verticalAlign

            val inverseScale = 1 / scale

            override fun render(posX: Int, posY: Int) {
                val fontRenderer = Minecraft.getMinecraft().fontRendererObj
                GlStateManager.translate(1.0, 1.0, 0.0)
                GlStateManager.scale(scale, scale, 1.0)
                map.entries.forEachIndexed { index, (text, size) ->
                    fontRenderer.drawStringWithShadow(
                        text,
                        RenderableUtils.calculateAlignmentXOffset(size, rawWidth, internalAlign).toFloat(),
                        index * 10.0f,
                        color.rgb,
                    )
                }
                GlStateManager.scale(inverseScale, inverseScale, 1.0)
                GlStateManager.translate(-1.0, -1.0, 0.0)
            }
        }

        fun placeholder(width: Int, height: Int = 10) = object : Renderable {
            override val width = width
            override val height = height
            override val horizontalAlign = HorizontalAlignment.LEFT
            override val verticalAlign = VerticalAlignment.TOP

            override fun render(posX: Int, posY: Int) {
            }
        }

        fun searchableTable(
            map: Map<List<Renderable?>, String?>,
            textInput: TextInput,
            key: Int,
            xPadding: Int = 1,
            yPadding: Int = 0,
            useEmptySpace: Boolean = false,
            horizontalAlign: HorizontalAlignment = HorizontalAlignment.LEFT,
            verticalAlign: VerticalAlignment = VerticalAlignment.TOP,
        ) = object : Renderable {
            var content = map.keys.toList()
            val xOffsets: List<Int> = calculateTableXOffsets(content, xPadding)
            val yOffsets: List<Int> = calculateTableYOffsets(content, yPadding)
            override val horizontalAlign = horizontalAlign
            override val verticalAlign = verticalAlign

            override val width = xOffsets.last() - xPadding
            override val height = yOffsets.last() - yPadding

            val emptySpaceX = if (useEmptySpace) 0 else xPadding
            val emptySpaceY = if (useEmptySpace) 0 else yPadding

            init {
                textInput.registerToEvent(key) {
                    // null = ignored, never filtered
                    content = map.filter { it.value?.contains(textInput.textBox, ignoreCase = true) ?: true }.keys.toList()
                }
            }

            override fun render(posX: Int, posY: Int) {
                for ((rowIndex, row) in content.withIndex()) {
                    for ((index, renderable) in row.withIndex()) {
                        GlStateManager.pushMatrix()
                        GlStateManager.translate(xOffsets[index].toFloat(), yOffsets[rowIndex].toFloat(), 0F)
                        renderable?.renderXYAligned(
                            posX + xOffsets[index],
                            posY + yOffsets[rowIndex],
                            xOffsets[index + 1] - xOffsets[index] - emptySpaceX,
                            yOffsets[rowIndex + 1] - yOffsets[rowIndex] - emptySpaceY,
                        )
                        GlStateManager.popMatrix()
                    }
                }
            }
        }

        /**
         * @param content the list of rows the table should render
         */
        fun table(
            content: List<List<Renderable>>,
            xPadding: Int = 1,
            yPadding: Int = 0,
            useEmptySpace: Boolean = false,
            horizontalAlign: HorizontalAlignment = HorizontalAlignment.LEFT,
            verticalAlign: VerticalAlignment = VerticalAlignment.TOP,
        ) = object : Renderable {
            val xOffsets: List<Int> = calculateTableXOffsets(content, xPadding)
            val yOffsets: List<Int> = calculateTableYOffsets(content, yPadding)
            override val horizontalAlign = horizontalAlign
            override val verticalAlign = verticalAlign

            override val width = xOffsets.last() - xPadding
            override val height = yOffsets.last() - yPadding

            val emptySpaceX = if (useEmptySpace) 0 else xPadding
            val emptySpaceY = if (useEmptySpace) 0 else yPadding

            override fun render(posX: Int, posY: Int) {
                for ((rowIndex, row) in content.withIndex()) {
                    for ((index, renderable) in row.withIndex()) {
                        GlStateManager.pushMatrix()
                        GlStateManager.translate(xOffsets[index].toFloat(), yOffsets[rowIndex].toFloat(), 0F)
                        renderable.renderXYAligned(
                            posX + xOffsets[index],
                            posY + yOffsets[rowIndex],
                            xOffsets[index + 1] - xOffsets[index] - emptySpaceX,
                            yOffsets[rowIndex + 1] - yOffsets[rowIndex] - emptySpaceY,
                        )
                        GlStateManager.popMatrix()
                    }
                }
            }
        }

        /**
         * @param searchPrefix text that is static in front of the textbox
         * @param onUpdateSize function that is called if the size changes (since the search text can get bigger than [content])
         * @param textInput The text input, can be external or internal
         * @param shouldRenderTopElseBottom true == Renders on top, false == Renders at the Bottom
         * @param hideIfNoText hides text box if no input is given
         * @param ySpacing space between the search and [content]
         * @param onHover is triggered if [content] or the text box is hovered
         * @param bypassChecks bypass the [shouldAllowLink] logic
         * @param condition condition to being able to input / [onHover] to trigger
         * @param scale text scale of the textbox
         * @param color color of the textbox
         * @param key event key for the [textInput] to register the event, needs clearing if [textInput] is external, default = 0
         */
        fun searchBox(
            content: Renderable,
            searchPrefix: String,
            onUpdateSize: (Renderable) -> Unit,
            textInput: TextInput = TextInput(),
            shouldRenderTopElseBottom: Boolean = true,
            hideIfNoText: Boolean = true,
            ySpacing: Int = 0,
            onHover: (TextInput) -> Unit = {},
            bypassChecks: Boolean = false,
            condition: () -> Boolean = { true },
            scale: Double = 1.0,
            color: Color = Color.WHITE,
            key: Int = 0,
        ) = object : Renderable {
            val textBoxHeight = (9 * scale).toInt() + 1

            val isTextBoxEmpty get() = textInput.textBox.isEmpty()

            override var width: Int = content.width
            override var height: Int = content.height + if (hideIfNoText && isTextBoxEmpty) 0 else (ySpacing + textBoxHeight)
            override val horizontalAlign = content.horizontalAlign
            override val verticalAlign = content.verticalAlign

            val searchWidth: Int
                get() {
                    val fontRenderer = Minecraft.getMinecraft().fontRendererObj
                    val string = searchPrefix + textInput.editTextWithAlwaysCarriage()
                    return (fontRenderer.getStringWidth(string) * scale).toInt() + 1
                }

            init {
                textInput.registerToEvent(key) {
                    var shouldUpdate = false
                    if (hideIfNoText) {
                        if (isTextBoxEmpty) {
                            if (height != content.height) {
                                height = content.height
                                shouldUpdate = true
                            }
                        } else {
                            if (height == content.height) {
                                height = content.height + ySpacing + textBoxHeight
                                shouldUpdate = true
                            }
                        }
                    }
                    val searchWidth = searchWidth
                    if (searchWidth > width) {
                        width = searchWidth
                        shouldUpdate = true
                    } else {
                        if (width > content.width) {
                            width = maxOf(content.width, searchWidth)
                            shouldUpdate = true
                        }
                    }
                    if (shouldUpdate) {
                        onUpdateSize(this)
                    }
                }
            }

            override fun render(posX: Int, posY: Int) {
                if (shouldRenderTopElseBottom && !(hideIfNoText && isTextBoxEmpty)) {
                    RenderableUtils.renderString(searchPrefix + textInput.editText(), scale, color)
                    GlStateManager.translate(0f, (ySpacing + textBoxHeight).toFloat(), 0f)
                }
                if (isHovered(posX, posY) && condition() && shouldAllowLink(true, bypassChecks)) {
                    onHover(textInput)
                    textInput.makeActive()
                    textInput.handle()
                    val yOff: Int
                    if (shouldRenderTopElseBottom) {
                        yOff = 0
                    } else {
                        yOff = content.height + ySpacing
                    }
                    if (isBoxHovered(posX, width, posY + yOff, textBoxHeight) && (-99).isKeyClicked()) {
                        textInput.clear()
                    }
                } else {
                    textInput.disable()
                }
                if (hideIfNoText && isTextBoxEmpty) {
                    content.render(posX, posY)
                } else if (!shouldRenderTopElseBottom) {
                    content.render(posX, posY)
                    GlStateManager.translate(0f, (ySpacing).toFloat(), 0f)
                    if (!(hideIfNoText && textInput.textBox.isEmpty())) {
                        RenderableUtils.renderString(searchPrefix + textInput.editText(), scale, color)
                    }
                    GlStateManager.translate(0f, -(ySpacing).toFloat(), 0f)
                } else {
                    content.render(posX, posY + textBoxHeight + ySpacing)
                    GlStateManager.translate(0f, -(ySpacing + textBoxHeight).toFloat(), 0f)
                }
            }

        }

        fun verticalEditTable(
            content: List<List<Renderable?>>,
            xPadding: Int = 1,
            yPadding: Int = 0,
            useEmptySpace: Boolean = false,
            onDrop: (Int, Int) -> Unit,
            onHover: (Int) -> Unit = {},
            onStartGrab: (Int) -> Unit = {},
            onEndGrab: (Int) -> Unit = {},
            bypassChecks: Boolean = false,
            condition: () -> Boolean = { true },
            horizontalAlign: HorizontalAlignment = HorizontalAlignment.LEFT,
            verticalAlign: VerticalAlignment = VerticalAlignment.TOP,
        ) = object : Renderable {
            val xOffsets: List<Int> = calculateTableXOffsets(content, xPadding)
            val yOffsets: List<Int> = calculateTableYOffsets(content, yPadding)
            override val horizontalAlign = horizontalAlign
            override val verticalAlign = verticalAlign

            override val width = xOffsets.last() - xPadding
            override val height = yOffsets.last() - yPadding

            val emptySpaceX = if (useEmptySpace) 0 else xPadding
            val emptySpaceY = if (useEmptySpace) 0 else yPadding

            var holdingIndex = -1
            var isSnappingIn = false
            var ySpace = 0

            val uuid = UUID.randomUUID()

            override fun render(posX: Int, posY: Int) {
                isSnappingIn = false
                ySpace = 0
                GlStateManager.pushMatrix()
                var rowIndex = 0
                var contentRowIndex = 0
                while (rowIndex < content.size) {
                    if (contentRowIndex == holdingIndex) {
                        contentRowIndex++
                        continue
                    }
                    if (isBoxHovered(
                            posX,
                            width,
                            posY + yOffsets[rowIndex],
                            yOffsets[rowIndex + 1] - yOffsets[rowIndex] - emptySpaceY - 1,
                        ) && condition() && shouldAllowLink(true, bypassChecks)
                    ) {
                        onHover(rowIndex)
                        DragNDrop.dragOnPress(Drag(rowIndex, uuid)) {
                            holdingIndex = contentRowIndex
                            onStartGrab(rowIndex)
                        }
                        var dropped = false
                        val preYSpace = ySpace
                        DragNDrop.handelDroppable(
                            object : Droppable {

                                override fun handle(drop: Any?) {
                                    val element = drop as? Drag ?: return
                                    onEndGrab(holdingIndex)
                                    holdingIndex = -1
                                    onDrop(element.rowIndex, rowIndex)
                                }

                                override fun preDrop(drop: Any?) {
                                    val element = drop as? Drag ?: return
                                    val index = element.rowIndex
                                    content[index].drawRow(index, posX, posY + ySpace, ySpace.toFloat(), true)
                                    dropped = true
                                    isSnappingIn = true
                                }

                                override fun validTarget(item: Any?): Boolean = item is Drag && item.uuid == uuid

                            },
                        )
                        val boxY: Pair<Int, Int>
                        if (dropped) {
                            rowIndex++
                            boxY = preYSpace to ySpace
                        } else {
                            boxY = ySpace to if (contentRowIndex == content.size) height
                            else ySpace + yOffsets[contentRowIndex + 1] - yOffsets[contentRowIndex]
                        }
                        Gui.drawRect(
                            posX,
                            posY + boxY.first,
                            posX + width,
                            posY + boxY.second - emptySpaceY,
                            Color.GRAY.withAlpha(if (dropped) 50 else 35),
                        )
                    }
                    content.getOrNull(contentRowIndex)?.drawRow(contentRowIndex, posX, posY + ySpace, ySpace.toFloat(), true)
                    rowIndex++
                    contentRowIndex++
                }
                GlStateManager.popMatrix()
            }

            private fun List<Renderable?>.drawRow(rowIndex: Int, x: Int, y: Int, yOff: Float, setYOffset: Boolean) {
                val slotHeight = yOffsets[rowIndex + 1] - yOffsets[rowIndex] - emptySpaceY
                for ((index, renderable) in this.withIndex()) {
                    GlStateManager.pushMatrix()
                    GlStateManager.translate(xOffsets[index].toFloat(), yOff, 0F)
                    renderable?.renderXYAligned(
                        x + xOffsets[index],
                        y,
                        xOffsets[index + 1] - xOffsets[index] - emptySpaceX,
                        slotHeight,
                    )
                    GlStateManager.popMatrix()
                }
                if (setYOffset) {
                    ySpace += slotHeight
                }
            }

            inner class Drag(val rowIndex: Int, val uuid: UUID) : DragItem<Drag> {

                override fun get() = this

                override fun onDiscard() {
                    onEndGrab(holdingIndex)
                    holdingIndex = -1
                }

                override fun onRender(mouseX: Int, mouseY: Int) {
                    if (!isSnappingIn) {
                        GlStateManager.translate(3f, 0f, 0f)
                        content[rowIndex].drawRow(rowIndex, 0, 0, 0f, false)
                        Gui.drawRect(0, 0, width, yOffsets[rowIndex + 1] - yOffsets[rowIndex] - emptySpaceY, Color.GRAY.withAlpha(30))
                        GlStateManager.translate(-3f, 0f, 0f)
                    }
                }

            }
        }

        fun progressBar(
            percent: Double,
            startColor: Color = Color(255, 0, 0),
            endColor: Color = Color(0, 255, 0),
            useChroma: Boolean = false,
            texture: SkillProgressBarConfig.TexturedBar.UsedTexture? = null,
            width: Int = 182,
            height: Int = 5,
            horizontalAlign: HorizontalAlignment = HorizontalAlignment.LEFT,
            verticalAlign: VerticalAlignment = VerticalAlignment.TOP,
        ) = object : Renderable {
            override val width = width
            override val height = height
            override val horizontalAlign = horizontalAlign
            override val verticalAlign = verticalAlign

            private val progress = if (texture == null) {
                (1.0 + percent * (width - 2.0)).toInt()
            } else {
                percent.toInt()
            }

            private val color = if (texture == null) {
                ColorUtils.blendRGB(startColor, endColor, percent)
            } else {
                startColor
            }

            override fun render(posX: Int, posY: Int) {
                if (texture == null) {
                    Gui.drawRect(0, 0, width, height, 0xFF43464B.toInt())

                    if (useChroma) {
                        ChromaShaderManager.begin(ChromaType.STANDARD)
                    }

                    val factor = 0.2
                    val bgColor = if (useChroma) Color.GRAY.darker() else color
                    Gui.drawRect(1, 1, width - 1, height - 1, bgColor.darker(factor).rgb)
                    Gui.drawRect(1, 1, progress, height - 1, color.rgb)

                    if (useChroma) {
                        ChromaShaderManager.end()
                    }
                } else {
                    val (textureX, textureY) = if (texture == SkillProgressBarConfig.TexturedBar.UsedTexture.MATCH_PACK) Pair(
                        0, 64,
                    ) else Pair(0, 0)

                    Minecraft.getMinecraft().renderEngine.bindTexture(ResourceLocation(texture.path))
                    Minecraft.getMinecraft().ingameGUI.drawTexturedModalRect(
                        posX, posY, textureX, textureY, width, height,
                    )

                    if (useChroma) {
                        ChromaShaderManager.begin(ChromaType.TEXTURED)
                        GlStateManager.color(1f, 1f, 1f, 1f)
                    } else {
                        GlStateManager.color(color.red / 255f, color.green / 255f, color.blue / 255f, 1f)
                    }
                    Minecraft.getMinecraft().ingameGUI.drawTexturedModalRect(
                        posX, posY, textureX, textureY + height, progress, height,
                    )

                    if (useChroma) {
                        ChromaShaderManager.end()
                    }
                }
            }
        }

        // TODO use this to render current boosted crop in next jacob contest crops
        fun Renderable.renderBounds(color: Color = LorenzColor.GREEN.toColor().addAlpha(100)) = object : Renderable {
            override val width = this@renderBounds.width
            override val height = this@renderBounds.height
            override val horizontalAlign = this@renderBounds.horizontalAlign
            override val verticalAlign = this@renderBounds.verticalAlign

            override fun render(posX: Int, posY: Int) {
                Gui.drawRect(0, 0, width, height, color.rgb)
                this@renderBounds.render(posX, posY)
            }

        }

        fun fixedSizeLine(
            content: Renderable,
            width: Int,
            horizontalAlign: HorizontalAlignment = HorizontalAlignment.LEFT,
            verticalAlign: VerticalAlignment = VerticalAlignment.TOP,
        ) = object : Renderable {
            val render = content

            override val width = width
            override val height = render.height
            override val horizontalAlign = horizontalAlign
            override val verticalAlign = verticalAlign
            override fun render(posX: Int, posY: Int) {
                render.renderXAligned(posX, posY, width)
            }
        }

        fun fixedSizeLine(
            content: List<Renderable>,
            width: Int,
            horizontalAlign: HorizontalAlignment = HorizontalAlignment.LEFT,
            verticalAlign: VerticalAlignment = VerticalAlignment.TOP,
        ) = object : Renderable {
            val render = content

            override val width = width
            override val height = render.maxOfOrNull { it.height } ?: 0
            override val horizontalAlign = horizontalAlign
            override val verticalAlign = verticalAlign

            val emptySpace = width - render.sumOf { it.width }
            val spacing = emptySpace / render.size

            override fun render(posX: Int, posY: Int) {
                var xOffset = posX
                render.forEach {
                    val x = it.width + spacing
                    it.renderXYAligned(xOffset, posY, x, height)
                    xOffset += x
                    GlStateManager.translate(x.toFloat(), 0f, 0f)
                }
                GlStateManager.translate(-(xOffset - posX).toFloat(), 0f, 0f)
            }
        }

        fun fixedSizeColumn(
            content: Renderable,
            height: Int,
            horizontalAlign: HorizontalAlignment = HorizontalAlignment.LEFT,
            verticalAlign: VerticalAlignment = VerticalAlignment.TOP,
        ) = object : Renderable {
            val render = content

            override val width = render.width
            override val height = height
            override val horizontalAlign = horizontalAlign
            override val verticalAlign = verticalAlign
            override fun render(posX: Int, posY: Int) {
                render.renderYAligned(posX, posY, height)
            }
        }

        fun fixedSizeBox(
            content: Renderable,
            height: Int,
            width: Int,
            horizontalAlign: HorizontalAlignment = HorizontalAlignment.LEFT,
            verticalAlign: VerticalAlignment = VerticalAlignment.TOP,
        ) = object : Renderable {
            val render = content

            override val width = width
            override val height = height
            override val horizontalAlign = horizontalAlign
            override val verticalAlign = verticalAlign
            override fun render(posX: Int, posY: Int) {
                render.renderXYAligned(posX, posY, height, width)
            }
        }

        fun horizontalContainer(
            content: List<Renderable>,
            spacing: Int = 0,
            horizontalAlign: HorizontalAlignment = HorizontalAlignment.LEFT,
            verticalAlign: VerticalAlignment = VerticalAlignment.TOP,
        ) = object : Renderable {
            val renderables = content

            override val width = renderables.sumOf { it.width } + spacing * (renderables.size - 1)
            override val height = renderables.maxOfOrNull { it.height } ?: 0
            override val horizontalAlign = horizontalAlign
            override val verticalAlign = verticalAlign

            override fun render(posX: Int, posY: Int) {
                var xOffset = posX
                renderables.forEach {
                    it.renderYAligned(xOffset, posY, height)
                    xOffset += it.width + spacing
                    GlStateManager.translate((it.width + spacing).toFloat(), 0f, 0f)
                }
                GlStateManager.translate(-width.toFloat() - spacing.toFloat(), 0f, 0f)
            }
        }

        fun verticalContainer(
            content: List<Renderable>,
            spacing: Int = 0,
            horizontalAlign: HorizontalAlignment = HorizontalAlignment.LEFT,
            verticalAlign: VerticalAlignment = VerticalAlignment.TOP,
        ) = object : Renderable {
            val renderables = content

            override val width = renderables.maxOfOrNull { it.width } ?: 0
            override val height = renderables.sumOf { it.height } + spacing * (renderables.size - 1)
            override val horizontalAlign = horizontalAlign
            override val verticalAlign = verticalAlign

            override fun render(posX: Int, posY: Int) {
                var yOffset = posY
                renderables.forEach {
                    it.renderXAligned(posX, yOffset, width)
                    yOffset += it.height + spacing
                    GlStateManager.translate(0f, (it.height + spacing).toFloat(), 0f)
                }
                GlStateManager.translate(0f, -height.toFloat() - spacing.toFloat(), 0f)
            }
        }

        fun paddingContainer(
            content: Renderable,
            topSpacing: Int = 0,
            bottomSpacing: Int = 0,
            leftSpacing: Int = 0,
            rightSpacing: Int = 0,
        ) = object : Renderable {
            override val width = content.width + leftSpacing + rightSpacing
            override val height = content.height + topSpacing + bottomSpacing
            override val horizontalAlign = content.horizontalAlign
            override val verticalAlign = content.verticalAlign

            override fun render(posX: Int, posY: Int) {
                GlStateManager.translate(leftSpacing.toFloat(), topSpacing.toFloat(), 0f)
                content.render(posX + leftSpacing, posY + topSpacing)
                GlStateManager.translate(-leftSpacing.toFloat(), -topSpacing.toFloat(), 0f)
            }
        }

        fun verticalSearchableContainer(
            content: Map<Renderable, String?>,
            spacing: Int = 0,
            textInput: TextInput,
            key: Int,
            horizontalAlign: HorizontalAlignment = HorizontalAlignment.LEFT,
            verticalAlign: VerticalAlignment = VerticalAlignment.TOP,
        ) = object : Renderable {
            var renderables = filterList(content, textInput.textBox)

            override val width = renderables.maxOfOrNull { it.width } ?: 0
            override val height = renderables.sumOf { it.height } + spacing * (renderables.size - 1)
            override val horizontalAlign = horizontalAlign
            override val verticalAlign = verticalAlign

            init {
                textInput.registerToEvent(key) {
                    // null = ignored, never filtered
                    renderables = filterList(content, textInput.textBox)
                }
            }

            override fun render(posX: Int, posY: Int) {
                var yOffset = posY
                renderables.forEach {
                    it.renderXAligned(posX, yOffset, width)
                    yOffset += it.height + spacing
                    GlStateManager.translate(0f, (it.height + spacing).toFloat(), 0f)
                }
                GlStateManager.translate(0f, -height.toFloat() - spacing.toFloat(), 0f)
            }
        }

        fun scrollList(
            list: List<Renderable>,
            height: Int,
            scrollValue: ScrollValue = ScrollValue(),
            velocity: Double = 2.0,
            button: Int? = null,
            bypassChecks: Boolean = false,
            horizontalAlign: HorizontalAlignment = HorizontalAlignment.LEFT,
            verticalAlign: VerticalAlignment = VerticalAlignment.TOP,
        ) = object : Renderable {
            override val width = list.maxOf { it.width }
            override val height = height
            override val horizontalAlign = horizontalAlign
            override val verticalAlign = verticalAlign

            private val virtualHeight = list.sumOf { it.height }

            private val scroll = ScrollInput.Companion.Vertical(
                scrollValue,
                0,
                virtualHeight - height,
                velocity,
                button,
            )

            private val end get() = scroll.asInt() + height

            override fun render(posX: Int, posY: Int) {
                scroll.update(
                    isHovered(posX, posY) && shouldAllowLink(true, bypassChecks),
                )

                var renderY = 0
                var virtualY = 0
                var found = false
                for (renderable in list) {
                    if ((virtualY..virtualY + renderable.height) in scroll.asInt()..end) {
                        renderable.renderXAligned(posX, posY + renderY, width)
                        GlStateManager.translate(0f, renderable.height.toFloat(), 0f)
                        renderY += renderable.height
                        found = true
                    } else if (found) {
                        found = false
                        if (renderY + renderable.height <= height) {
                            renderable.renderXAligned(posX, posY + renderY, width)
                        }
                        continue
                    }
                    virtualY += renderable.height
                }
                GlStateManager.translate(0f, -renderY.toFloat(), 0f)
            }
        }

        fun searchableScrollList(
            content: Map<Renderable, String?>,
            height: Int,
            scrollValue: ScrollValue = ScrollValue(),
            velocity: Double = 2.0,
            button: Int? = null,
            textInput: TextInput,
            key: Int,
            bypassChecks: Boolean = false,
            horizontalAlign: HorizontalAlignment = HorizontalAlignment.LEFT,
            verticalAlign: VerticalAlignment = VerticalAlignment.TOP,
        ) = object : Renderable {
            private var list = filterList(content, textInput.textBox)
            override val width = list.maxOf { it.width }
            override val height = height
            override val horizontalAlign = horizontalAlign
            override val verticalAlign = verticalAlign

            init {
                textInput.registerToEvent(key) {
                    // null = ignored, never filtered
                    list = filterList(content, textInput.textBox)
                    scroll = createScroll()
                }
            }

            // correct
            private val virtualHeight get() = list.sumOf { it.height }

            private var scroll = createScroll()

            private fun createScroll() = ScrollInput.Companion.Vertical(
                scrollValue,
                0,
                virtualHeight - height,
                velocity,
                button,
            )

            private val end get() = scroll.asInt() + height

            override fun render(posX: Int, posY: Int) {
                scroll.update(
                    isHovered(posX, posY) && shouldAllowLink(true, bypassChecks),
                )

                var renderY = 0
                var virtualY = 0
                var found = false
                for (renderable in list) {
                    if ((virtualY..virtualY + renderable.height) in scroll.asInt()..end) {
                        renderable.renderXAligned(posX, posY + renderY, width)
                        GlStateManager.translate(0f, renderable.height.toFloat(), 0f)
                        renderY += renderable.height
                        found = true
                    } else if (found) {
                        found = false
                        if (renderY + renderable.height <= height) {
                            renderable.renderXAligned(posX, posY + renderY, width)
                        }
                        continue
                    }
                    virtualY += renderable.height
                }
                GlStateManager.translate(0f, -renderY.toFloat(), 0f)
            }
        }

        private fun filterList(content: Map<Renderable, String?>, textBox: String): Set<Renderable> {
            val map = content.filter { it.value?.contains(textBox, ignoreCase = true) ?: true }
            val set = map.keys.toMutableSet()
            if (map.filter { it.value != null }.isEmpty()) {
                if (textBox.isNotEmpty()) {
                    set.add(string("§cNo search results!"))
                }
            }
            return set
        }

        fun scrollTable(
            content: List<List<Renderable?>>,
            height: Int,
            scrollValue: ScrollValue = ScrollValue(),
            velocity: Double = 2.0,
            button: Int? = null,
            xPadding: Int = 1,
            yPadding: Int = 0,
            hasHeader: Boolean = false,
            bypassChecks: Boolean = false,
            horizontalAlign: HorizontalAlignment = HorizontalAlignment.LEFT,
            verticalAlign: VerticalAlignment = VerticalAlignment.TOP,
        ) = object : Renderable {

            val xOffsets: List<Int> = calculateTableXOffsets(content, xPadding)
            val yOffsets: List<Int> = calculateTableYOffsets(content, yPadding)

            override val width = xOffsets.last() - xPadding
            override val height = height
            override val horizontalAlign = horizontalAlign
            override val verticalAlign = verticalAlign

            private val virtualHeight = yOffsets.last() - yPadding

            private val end get() = scroll.asInt() + height - yPadding - 1

            private val scroll = ScrollInput.Companion.Vertical(
                scrollValue,
                if (hasHeader) yOffsets[1] else 0,
                virtualHeight - height,
                velocity,
                button,
            )

            override fun render(posX: Int, posY: Int) {
                scroll.update(
                    isHovered(posX, posY) && shouldAllowLink(true, bypassChecks),
                )

                var renderY = 0
                if (hasHeader) {
                    for ((index, renderable) in content[0].withIndex()) {
                        GlStateManager.translate(xOffsets[index].toFloat(), 0f, 0f)
                        renderable?.renderXYAligned(
                            posX + xOffsets[index],
                            posY + renderY,
                            xOffsets[index + 1] - xOffsets[index],
                            yOffsets[1],
                        )
                        GlStateManager.translate(-xOffsets[index].toFloat(), 0f, 0f)
                    }
                    val yShift = yOffsets[1] - yOffsets[0]
                    GlStateManager.translate(0f, yShift.toFloat(), 0f)
                    renderY += yShift
                }
                @Suppress("SpacingAroundCurly")
                val range = yOffsets.indexOfFirst { it >= scroll.asInt() }..<(
                    yOffsets.indexOfFirst { it >= end }.takeIf { it > 0 }
                        ?: yOffsets.size
                    ) - 1

                val range2 = if (range.last + 3 <= yOffsets.size && yOffsets[range.last + 2] - yOffsets[range.first] <= height - renderY) {
                    range.first..range.last() + 1
                } else {
                    range
                }

                for (rowIndex in range2) {
                    for ((index, renderable) in content[rowIndex].withIndex()) {
                        GlStateManager.translate(xOffsets[index].toFloat(), 0f, 0f)
                        renderable?.renderXYAligned(
                            posX + xOffsets[index],
                            posY + renderY,
                            xOffsets[index + 1] - xOffsets[index],
                            yOffsets[rowIndex + 1] - yOffsets[rowIndex],
                        )
                        GlStateManager.translate(-xOffsets[index].toFloat(), 0f, 0f)
                    }
                    val yShift = yOffsets[rowIndex + 1] - yOffsets[rowIndex]
                    GlStateManager.translate(0f, yShift.toFloat(), 0f)
                    renderY += yShift
                }
                GlStateManager.translate(0f, -renderY.toFloat(), 0f)
            }
        }

        fun drawInsideRoundedRect(
            input: Renderable,
            color: Color,
            padding: Int = 2,
            radius: Int = 10,
            smoothness: Int = 2,
            horizontalAlign: HorizontalAlignment = HorizontalAlignment.LEFT,
            verticalAlign: VerticalAlignment = VerticalAlignment.TOP,
        ) = object : Renderable {
            override val width = input.width + padding * 2
            override val height = input.height + padding * 2
            override val horizontalAlign = horizontalAlign
            override val verticalAlign = verticalAlign

            override fun render(posX: Int, posY: Int) {
                RenderUtils.drawRoundRect(0, 0, width, height, color.rgb, radius, smoothness)
                GlStateManager.translate(padding.toFloat(), padding.toFloat(), 0f)
                input.render(posX + padding, posY + padding)
                GlStateManager.translate(-padding.toFloat(), -padding.toFloat(), 0f)
            }
        }

        fun drawInsideRoundedRectOutline(
            input: Renderable,
            padding: Int = 2,
            radius: Int = 10,
            smoothness: Int = 2,
            topOutlineColor: Int,
            bottomOutlineColor: Int,
            borderOutlineThickness: Int,
            blur: Float = 0.7f,
            horizontalAlign: HorizontalAlignment = HorizontalAlignment.LEFT,
            verticalAlign: VerticalAlignment = VerticalAlignment.TOP,
        ) = object : Renderable {
            override val width = input.width + padding * 2
            override val height = input.height + padding * 2
            override val horizontalAlign = horizontalAlign
            override val verticalAlign = verticalAlign

            override fun render(posX: Int, posY: Int) {
                GlStateManager.translate(padding.toFloat(), padding.toFloat(), 0f)
                input.render(posX + padding, posY + padding)
                GlStateManager.translate(-padding.toFloat(), -padding.toFloat(), 0f)

                RenderUtils.drawRoundRectOutline(
                    0,
                    0,
                    width,
                    height,
                    topOutlineColor,
                    bottomOutlineColor,
                    borderOutlineThickness,
                    radius,
                    blur,
                )
            }
        }

        fun drawInsideImage(
            input: Renderable,
            texture: ResourceLocation,
            alpha: Int = 255,
            padding: Int = 2,
            horizontalAlign: HorizontalAlignment = HorizontalAlignment.LEFT,
            verticalAlign: VerticalAlignment = VerticalAlignment.TOP,
            radius: Int = 0,
        ) = object : Renderable {
            override val width = input.width + padding * 2
            override val height = input.height + padding * 2
            override val horizontalAlign = horizontalAlign
            override val verticalAlign = verticalAlign

            override fun render(posX: Int, posY: Int) {
                Minecraft.getMinecraft().textureManager.bindTexture(texture)
                GlStateManager.color(1f, 1f, 1f, alpha / 255f)
                RenderUtils.drawRoundTexturedRect(0, 0, width, height, GL11.GL_NEAREST, radius)
                GlStateManager.color(1f, 1f, 1f, 1f)

                GlStateManager.translate(padding.toFloat(), padding.toFloat(), 0f)
                input.render(posX + padding, posY + padding)
                GlStateManager.translate(-padding.toFloat(), -padding.toFloat(), 0f)
            }
        }

        fun drawInsideFixedSizedImage(
            input: Renderable,
            texture: ResourceLocation,
            width: Int = input.width,
            height: Int = input.height,
            alpha: Int = 255,
            padding: Int = 2,
            uMin: Float = 0f,
            uMax: Float = 1f,
            vMin: Float = 0f,
            vMax: Float = 1f,
            horizontalAlign: HorizontalAlignment = HorizontalAlignment.LEFT,
            verticalAlign: VerticalAlignment = VerticalAlignment.TOP,
        ) = object : Renderable {
            override val width = width
            override val height = height
            override val horizontalAlign = horizontalAlign
            override val verticalAlign = verticalAlign

            override fun render(posX: Int, posY: Int) {
                Minecraft.getMinecraft().textureManager.bindTexture(texture)

                GlStateManager.color(1f, 1f, 1f, alpha / 255f)
                GuiRenderUtils.drawTexturedRect(0, 0, width, height, uMin, uMax, vMin, vMax)
                GlStateManager.color(1f, 1f, 1f, 1f)

                GlStateManager.translate(padding.toFloat(), padding.toFloat(), 0f)
                input.render(posX + padding, posY + padding)
                GlStateManager.translate(-padding.toFloat(), -padding.toFloat(), 0f)
            }
        }

        fun image(
            texture: ResourceLocation,
            width: Int,
            height: Int,
            alpha: Int = 255,
            uMin: Float = 0f,
            uMax: Float = 1f,
            vMin: Float = 0f,
            vMax: Float = 1f,
            horizontalAlign: HorizontalAlignment = HorizontalAlignment.LEFT,
            verticalAlign: VerticalAlignment = VerticalAlignment.TOP,
        ) = object : Renderable {
            override val width = width
            override val height = height
            override val horizontalAlign = horizontalAlign
            override val verticalAlign = verticalAlign

            override fun render(posX: Int, posY: Int) {
                Minecraft.getMinecraft().textureManager.bindTexture(texture)
                GlStateManager.color(1f, 1f, 1f, alpha / 255f)
                GuiRenderUtils.drawTexturedRect(0, 0, width, height, uMin, uMax, vMin, vMax)
                GlStateManager.color(1f, 1f, 1f, 1f)
            }
        }

        fun drawInsideRoundedRectWithOutline(
            input: Renderable,
            color: Color,
            padding: Int = 2,
            radius: Int = 10,
            smoothness: Int = 2,
            topOutlineColor: Int,
            bottomOutlineColor: Int,
            borderOutlineThickness: Int,
            blur: Float = 0.7f,
            horizontalAlign: HorizontalAlignment = HorizontalAlignment.LEFT,
            verticalAlign: VerticalAlignment = VerticalAlignment.TOP,
        ) = object : Renderable {
            override val width = input.width + padding * 2
            override val height = input.height + padding * 2
            override val horizontalAlign = horizontalAlign
            override val verticalAlign = verticalAlign

            override fun render(posX: Int, posY: Int) {
                RenderUtils.drawRoundRect(0, 0, width, height, color.rgb, radius, smoothness)
                RenderUtils.drawRoundRectOutline(
                    0,
                    0,
                    width,
                    height,
                    topOutlineColor,
                    bottomOutlineColor,
                    borderOutlineThickness,
                    radius,
                    blur,
                )

                GlStateManager.translate(padding.toFloat(), padding.toFloat(), 0f)
                input.render(posX + padding, posY + padding)
                GlStateManager.translate(-padding.toFloat(), -padding.toFloat(), 0f)
            }
        }

        fun fakePlayer(
            player: EntityPlayer,
            followMouse: Boolean = false,
            eyesX: Float = 0f,
            eyesY: Float = 0f,
            width: Int = 50,
            height: Int = 100,
            entityScale: Int = 30,
            padding: Int = 5,
            color: Int? = null,
            colorCondition: () -> Boolean = { true },
        ) = object : Renderable {
            override val width = width + 2 * padding
            override val height = height + 2 * padding
            override val horizontalAlign = HorizontalAlignment.LEFT
            override val verticalAlign = VerticalAlignment.TOP
            val playerWidth = entityScale
            val playerHeight = entityScale * 2
            val playerX = width / 2 + padding
            val playerY = height / 2 + playerHeight / 2 + padding

            override fun render(posX: Int, posY: Int) {
                GlStateManager.color(1f, 1f, 1f, 1f)
                if (color != null) RenderLivingEntityHelper.setEntityColor(player, color, colorCondition)
                val mouse = currentRenderPassMousePosition ?: return
                val mouseXRelativeToPlayer = if (followMouse) (posX + playerX - mouse.first).toFloat() else eyesX
                val mouseYRelativeToPlayer = if (followMouse) (posY + playerY - mouse.second - 1.62 * entityScale).toFloat() else eyesY
                GlStateManager.translate(0f, 0f, 100f)
                drawEntityOnScreen(
                    playerX,
                    playerY,
                    entityScale,
                    mouseXRelativeToPlayer,
                    mouseYRelativeToPlayer,
                    player,
                )
                GlStateManager.translate(0f, 0f, -100f)
            }
        }
    }
}
