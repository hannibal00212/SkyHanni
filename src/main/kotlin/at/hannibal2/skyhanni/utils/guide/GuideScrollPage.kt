package at.hannibal2.skyhanni.utils.guide

import at.hannibal2.skyhanni.utils.CollectionUtils.tableStretchXPadding
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.ScrollValue

abstract class GuideScrollPage(
    val sizeX: Int,
    val sizeY: Int,
    paddingX: Int = 0,
    paddingY: Int = 0,
    val marginY: Int = 5,
    val velocity: Double = 3.0,
    val hasHeader: Boolean = true,
) : GuideRenderablePage(paddingX, paddingY) {

    private val scroll = ScrollValue()

    fun update(content: List<List<Renderable>>) {
        renderable = Renderable.scrollTable(
            content = content,
            height = sizeY - paddingY * 2,
            scrollValue = scroll,
            velocity = velocity,
            xPadding = content.tableStretchXPadding(sizeX - paddingX * 2),
            yPadding = marginY,
            hasHeader = hasHeader,
            button = 0
        )
    }
}
