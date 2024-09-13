package at.hannibal2.skyhanni.utils.renderables

import at.hannibal2.skyhanni.utils.RenderUtils.HorizontalAlignment
import at.hannibal2.skyhanni.utils.RenderUtils.VerticalAlignment
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import kotlin.math.ceil

object RenderableInventory {

    private val inventoryTextures by lazy { ResourceLocation("skyhanni", "gui/slot.png") }
    private const val TEXTURE_WIDTH = 90
    private const val TEXTURE_HEIGHT = 54

    private enum class SlotsUv(val uMin: Int, val uMax: Int, val vMin: Int, val vMax: Int) {
        TOP_LEFT_CORNER(14, 18, 32, 36),
        TOP_BORDER(18, 36, 32, 36),
        TOP_RIGHT_CORNER(36, 40, 32, 36),
        LEFT_BORDER(68, 72, 18, 36),
        CENTER(0, 18, 0, 18),
        RIGHT_BORDER(72, 76, 18, 36),
        BOTTOM_LEFT_CORNER(14, 18, 36, 40),
        BOTTOM_BORDER(18, 36, 36, 40),
        BOTTOM_RIGHT_CORNER(36, 40, 36, 40),
        HALF_EMPTY(54, 63, 0, 18),
        EMPTY(18, 36, 0, 18),
        ;

        fun getUvCoords(): FloatArray {
            return floatArrayOf(
                (uMin.toFloat() / TEXTURE_WIDTH),
                (uMax.toFloat() / TEXTURE_WIDTH),
                (vMin.toFloat() / TEXTURE_HEIGHT),
                (vMax.toFloat() / TEXTURE_HEIGHT),
            )
        }

        fun height() = vMax - vMin
        fun width() = uMax - uMin
    }

    private fun createUvList(size: Int, maxSize: Int) = buildList {
        val length = if (size >= maxSize) maxSize else size
        val lastLength = if (size % maxSize == 0) maxSize else size % maxSize
        val isWeird = lastLength % 2 != length % 2

        val rows = ceil(size.toDouble() / maxSize).toInt()

        for (i in 0 until rows + 2) {
            val row = mutableListOf<SlotsUv>()
            if (i == 0) { // top border
                row.add(SlotsUv.TOP_LEFT_CORNER)
                row.addAll(List(length) { SlotsUv.TOP_BORDER })
                row.add(SlotsUv.TOP_RIGHT_CORNER)
            } else if (i == rows + 1) { // last border
                row.add(SlotsUv.BOTTOM_LEFT_CORNER)
                row.addAll(List(length) { SlotsUv.BOTTOM_BORDER })
                row.add(SlotsUv.BOTTOM_RIGHT_CORNER)
            } else if (i == rows && rows != 1) { // last row
                val spaces = if (isWeird) (length - lastLength) - 1 else length - lastLength
                row.add(SlotsUv.LEFT_BORDER)
                row.addAll(List(spaces / 2) { SlotsUv.EMPTY })
                if (isWeird) row.add(SlotsUv.HALF_EMPTY)
                row.addAll(List(lastLength) { SlotsUv.CENTER })
                if (isWeird) row.add(SlotsUv.HALF_EMPTY)
                row.addAll(List(spaces / 2) { SlotsUv.EMPTY })
                row.add(SlotsUv.RIGHT_BORDER)
            } else {
                row.add(SlotsUv.LEFT_BORDER)
                row.addAll(List(length) { SlotsUv.CENTER })
                row.add(SlotsUv.RIGHT_BORDER)
            }
            add(row.toList())
        }
    }

    fun fakeInventory(
        items: List<ItemStack?>,
        maxRowSize: Int,
        scale: Double,
        horizontalAlign: HorizontalAlignment = HorizontalAlignment.LEFT,
        verticalAlign: VerticalAlignment = VerticalAlignment.TOP,
    ): Renderable {
        val uvList = createUvList(items.size, maxRowSize)
        var index = 0
        val finalList = uvList.map { uvRow ->
            uvRow.map { uv ->
                val uvArray = uv.getUvCoords()
                val renderable = if (uv == SlotsUv.CENTER) {
                    (items[index]?.let { item ->
                        Renderable.itemStack(
                            item,
                            scale,
                            0,
                            0,
                            false,
                        )
                    } ?: Renderable.placeholder((16 * scale).toInt(), (16 * scale).toInt())).also { index++ }
                } else Renderable.placeholder(0, 0)
                Renderable.drawInsideFixedSizedImage(
                    renderable,
                    inventoryTextures,
                    (uv.width() * scale).toInt(),
                    (uv.height() * scale).toInt(),
                    padding = scale.toInt(),
                    uMin = uvArray[0],
                    uMax = uvArray[1],
                    vMin = uvArray[2],
                    vMax = uvArray[3],
                )
            }
        }

        return Renderable.verticalContainer(
            finalList.map { Renderable.horizontalContainer(it, 0) },
            0,
            horizontalAlign = horizontalAlign,
            verticalAlign = verticalAlign,
        )
    }
}
