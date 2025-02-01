package at.hannibal2.skyhanni.utils.renderables

import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.addString
import at.hannibal2.skyhanni.utils.CollectionUtils.putAt
import at.hannibal2.skyhanni.utils.RenderUtils.HorizontalAlignment
import at.hannibal2.skyhanni.utils.RenderUtils.VerticalAlignment
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.renderables.Renderable.Companion.clickableAndScrollable
import at.hannibal2.skyhanni.utils.renderables.Renderable.Companion.hoverTips
import at.hannibal2.skyhanni.utils.renderables.Renderable.Companion.leftAndRightClickable
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color

private typealias Direction = Renderable.Companion.Direction

internal object RenderableUtils {

    /** Calculates the absolute x position of the columns in a table*/
    fun calculateTableXOffsets(content: List<List<Renderable?>>, xPadding: Int) = run {
        var buffer = 0
        var index = 0
        buildList {
            add(0)
            while (true) {
                buffer += content.map { it.getOrNull(index) }.takeIf { it.any { it != null } }?.maxOfOrNull {
                    it?.width ?: 0
                }?.let { it + xPadding } ?: break
                add(buffer)
                index++
            }
            if (this.size == 1) {
                add(xPadding)
            }
        }
    }

    /** Calculates the absolute y position of the rows in a table*/
    fun calculateTableYOffsets(content: List<List<Renderable?>>, yPadding: Int) = run {
        var buffer = 0
        listOf(0) + (
            content.takeIf { it.isNotEmpty() }?.map { row ->
                buffer += (row.maxOfOrNull { it?.height ?: 0 } ?: 0) + yPadding
                buffer
            } ?: listOf(yPadding)
            )
    }

    fun calculateAlignmentXOffset(width: Int, xSpace: Int, alignment: HorizontalAlignment) = when (alignment) {
        HorizontalAlignment.CENTER -> (xSpace - width) / 2
        HorizontalAlignment.RIGHT -> xSpace - width
        else -> 0
    }

    private fun calculateAlignmentXOffset(renderable: Renderable, xSpace: Int) = when (renderable.horizontalAlign) {
        HorizontalAlignment.LEFT -> 0
        HorizontalAlignment.CENTER -> (xSpace - renderable.width) / 2
        HorizontalAlignment.RIGHT -> xSpace - renderable.width
        else -> 0
    }

    private fun calculateAlignmentYOffset(renderable: Renderable, ySpace: Int) = when (renderable.verticalAlign) {
        VerticalAlignment.TOP -> 0
        VerticalAlignment.CENTER -> (ySpace - renderable.height) / 2
        VerticalAlignment.BOTTOM -> ySpace - renderable.height
        else -> 0
    }

    fun Renderable.renderXYAligned(posX: Int, posY: Int, xSpace: Int, ySpace: Int): Pair<Int, Int> {
        val xOffset = calculateAlignmentXOffset(this, xSpace)
        val yOffset = calculateAlignmentYOffset(this, ySpace)
        GlStateManager.translate(xOffset.toFloat(), yOffset.toFloat(), 0f)
        this.render(posX + xOffset, posY + yOffset)
        GlStateManager.translate(-xOffset.toFloat(), -yOffset.toFloat(), 0f)
        return xOffset to yOffset
    }

    fun Renderable.renderXAligned(posX: Int, posY: Int, xSpace: Int): Int {
        val xOffset = calculateAlignmentXOffset(this, xSpace)
        GlStateManager.translate(xOffset.toFloat(), 0f, 0f)
        this.render(posX + xOffset, posY)
        GlStateManager.translate(-xOffset.toFloat(), 0f, 0f)
        return xOffset
    }

    fun Renderable.renderYAligned(posX: Int, posY: Int, ySpace: Int): Int {
        val yOffset = calculateAlignmentYOffset(this, ySpace)
        GlStateManager.translate(0f, yOffset.toFloat(), 0f)
        this.render(posX, posY + yOffset)
        GlStateManager.translate(0f, -yOffset.toFloat(), 0f)
        return yOffset
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun renderString(text: String, scale: Double = 1.0, color: Color = Color.WHITE, inverseScale: Double = 1 / scale) {
        val fontRenderer = Minecraft.getMinecraft().fontRendererObj
        GlStateManager.translate(1.0, 1.0, 0.0)
        GlStateManager.scale(scale, scale, 1.0)
        fontRenderer.drawStringWithShadow(text, 0f, 0f, color.rgb)
        GlStateManager.scale(inverseScale, inverseScale, 1.0)
        GlStateManager.translate(-1.0, -1.0, 0.0)
    }

    inline fun <T> MutableList<Searchable>.addNullableButton(
        label: String,
        current: T?,
        crossinline onChange: (T?) -> Unit,
        universe: List<T?>,
        nullLabel: String,
        enableUniverseScroll: Boolean = true,
    ) {
        val map = universe.associateWithTo(LinkedHashMap()) { it.toString() }
        map.putAt(0, null, nullLabel)

        val currentName = map[current] ?: error("unknown entry $current in map")
        addButton(
            label = label,
            current = currentName,
            getName = { it ?: nullLabel },
            onChange = { newString ->
                val newKey = map.entries.first { it.value == newString }.key
                onChange(newKey)
            },
            universe = map.values.toList(),
            enableUniverseScroll = enableUniverseScroll,
        )
    }

    inline fun <T> MutableList<Searchable>.addButton(
        label: String,
        current: T,
        crossinline getName: (T?) -> String,
        crossinline onChange: (T?) -> Unit,
        universe: List<T?>,
        enableUniverseScroll: Boolean = true,
    ) {
        add(createButtonNew(label, current, getName, onChange, universe, enableUniverseScroll))
    }

    inline fun <reified T : Enum<T>> MutableList<Renderable>.addRenderableButton(
        label: String,
        current: T?,
        crossinline getName: (T?) -> String,
        crossinline onChange: (T?) -> Unit,
        universe: List<T?> = enumValues<T>().toList(),
        enableUniverseScroll: Boolean = true,
    ) {
        add(createButtonNew(label, current, getName, onChange, universe, enableUniverseScroll).renderable)
    }

    fun <T> List<T?>.circle(current: T?): T? {
        val index = indexOf(current)
        val newIndex = (index + 1) % size // Increment index and wrap around
        return get(newIndex)
    }

    fun <T> List<T?>.circleBackwards(current: T?): T? {
        val index = indexOf(current)
        val newIndex = ((index - 1 + size)) % size // Increment index and wrap around
        return get(newIndex)
    }

    inline fun <T> createButtonNew(
        label: String,
        current: T?,
        crossinline getName: (T?) -> String,
        crossinline onChange: (T?) -> Unit,
        universe: List<T?>,
        enableUniverseScroll: Boolean = true,
    ): Searchable {
        val onClick: (Direction) -> Unit = { direction ->
            if ((System.currentTimeMillis() - ChatUtils.lastButtonClicked) > 150) { // funny thing happen if I don't do that
                val next = when (direction) {
                    Direction.LEFT -> universe.circle(current)
                    Direction.RIGHT -> universe.circleBackwards(current)
                }
                onChange(next)
                SoundUtils.playClickSound()
                ChatUtils.lastButtonClicked = System.currentTimeMillis()
            }
        }

        val currentName = getName(current)
        val tips = buildList {
            add("§a$label")
            add(" ")
            for (entry in universe) {
                val name = getName(entry)
                if (entry == current) {
                    this.add("§e▶ $name")
                } else {
                    this.add("§7  $name")
                }
            }
            add(" ")
            add("§bRight-click to go backwards!")
            add("§eClick to switch $label!")
            if (enableUniverseScroll) {
                add("§8§oYou can also scroll here!")
            }
        }

        return Renderable.line {
            addString("§7$label §a[")
            val displayFormat = hoverTips("§e$currentName", tips, bypassChecks = false, onHover = {})
            when (enableUniverseScroll) {
                true -> clickableAndScrollable(displayFormat, onClick = onClick, bypassChecks = false)
                false -> leftAndRightClickable(displayFormat, onClick = onClick, bypassChecks = false)
            }.let { add(it) }
            addString("§a]")
        }.toSearchable()
    }

    @Deprecated("do not use", ReplaceWith(""))
    inline fun MutableList<Searchable>.addButton(
        prefix: String,
        getName: String,
        crossinline onChange: () -> Unit,
        tips: List<String> = emptyList(),
    ) {
        val onClick = {
            if ((System.currentTimeMillis() - ChatUtils.lastButtonClicked) > 150) { // funny thing happen if I don't do that
                onChange()
                SoundUtils.playClickSound()
                ChatUtils.lastButtonClicked = System.currentTimeMillis()
            }
        }
        add(
            Renderable.line {
                addString(prefix)
                addString("§a[")
                if (tips.isEmpty()) {
                    add(Renderable.link("§e$getName", false, onClick))
                } else {
                    add(Renderable.clickAndHover("§e$getName", tips, false, onClick))
                }
                addString("§a]")
            }.toSearchable(),
        )
    }

    fun MutableList<Renderable>.addCenteredString(string: String) =
        this.add(Renderable.string(string, horizontalAlign = HorizontalAlignment.CENTER))
}

fun MutableList<Renderable>.addLine(builderAction: MutableList<Renderable>.() -> Unit) {
    add(Renderable.horizontalContainer(buildList { builderAction() }))
}

internal abstract class RenderableWrapper internal constructor(protected val content: Renderable) : Renderable {
    override val width = content.width
    override val height = content.height
    override val horizontalAlign = content.horizontalAlign
    override val verticalAlign = content.verticalAlign
}
