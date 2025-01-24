package at.hannibal2.skyhanni.utils.guide

import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.RenderableUtils.renderXYAligned
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.item.ItemStack

const val SELECTED_COLOR = 0x50000000
const val NOT_SELECTED_COLOR = 0x50303030
const val TAB_SPACING = 5
const val TAB_SHORT_SIDE = 25
const val TAB_LONG_SIDE = 28

abstract class GuideGUI<pageEnum : Enum<*>>(defaultScreen: pageEnum) : GuiScreen() {

    abstract val sizeX: Int
    abstract val sizeY: Int
    lateinit var pageList: Map<pageEnum, GuidePage>
    lateinit var horizontalTabs: List<GuideTab>
    lateinit var verticalTabs: List<GuideTab>
    protected var currentPage: pageEnum = defaultScreen
        set(value) {
            pageList[field]?.onLeave()
            pageList[value]?.onEnter()
            field = value
        }

    val lastVerticalTabWrapper = object : tabWrapper {
        override var tab: GuideTab? = null
    }
    val lastHorizontalTabWrapper = object : tabWrapper {
        override var tab: GuideTab? = null
    }

    fun hTab(item: ItemStack, tip: Renderable, onClick: (GuideTab) -> Unit) =
        GuideTab(item, tip, false, lastHorizontalTabWrapper, onClick)

    fun vTab(item: ItemStack, tip: Renderable, onClick: (GuideTab) -> Unit) =
        GuideTab(item, tip, true, lastVerticalTabWrapper, onClick)

    interface tabWrapper {
        var tab: GuideTab?
    }

    fun refreshPage() {
        pageList[currentPage]?.refresh()
    }

    private fun renderHorizontalTabs() {
        var offset = Pair(TAB_SPACING.toFloat() * 3f, -TAB_LONG_SIDE.toFloat())
        GlStateManager.translate(offset.first, offset.second, 0f)
        for (tab in horizontalTabs) {
            tab.render(offset.first.toInt(), offset.second.toInt())
            val xShift = (TAB_SHORT_SIDE + TAB_SPACING).toFloat()
            offset = offset.first + xShift to offset.second
            GlStateManager.translate(xShift, 0f, 0f)
        }
        GlStateManager.translate(-offset.first, -offset.second, 0f)
    }

    private fun renderVerticalTabs() {
        var offset = Pair(-TAB_LONG_SIDE.toFloat(), TAB_SPACING.toFloat() * 3f)
        GlStateManager.translate(offset.first, offset.second, 0f)
        for (tab in verticalTabs) {
            tab.render(offset.first.toInt(), offset.second.toInt())
            val yShift = (TAB_SHORT_SIDE + TAB_SPACING).toFloat()
            offset = offset.first to offset.second + yShift
            GlStateManager.translate(0f, yShift, 0f)
        }
        GlStateManager.translate(-offset.first, -offset.second, 0f)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) = try {
        super.drawScreen(mouseX, mouseY, partialTicks)
        drawDefaultBackground()
        val guiLeft = (width - sizeX) / 2
        val guiTop = (height - sizeY) / 2

        /*
        val mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth
        val mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1
         */

        val relativeMouseX = mouseX - guiLeft
        val relativeMouseY = mouseY - guiTop

        GlStateManager.pushMatrix()
        GlStateManager.translate(guiLeft.toFloat(), guiTop.toFloat(), 0f)
        drawRect(0, 0, sizeX, sizeY, 0x50000000)

        Renderable.withMousePosition(relativeMouseX, relativeMouseY) {
            renderHorizontalTabs()
            renderVerticalTabs()

            Renderable.string(
                "§7SkyHanni ",
                horizontalAlign = RenderUtils.HorizontalAlignment.RIGHT,
                verticalAlign = RenderUtils.VerticalAlignment.BOTTOM
            ).renderXYAligned(0, 0, sizeX, sizeY)

            val page = pageList[currentPage]
            page?.drawPage(relativeMouseX, relativeMouseY)

            GlStateManager.translate(-guiLeft.toFloat(), -guiTop.toFloat(), 0f)
        }

        GlStateManager.popMatrix()

    } catch (e: Exception) {
        GlStateManager.popMatrix()
        ErrorManager.logErrorWithData(
            e, "Something broke in GuideGUI",
            "Guide" to this.javaClass.typeName,
            "Page" to currentPage.name
        )
    }
}

