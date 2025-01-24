package at.hannibal2.skyhanni.utils.guide

import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraft.client.renderer.GlStateManager

abstract class GuideRenderablePage(
    val paddingX: Int = 0,
    val paddingY: Int = 0
) : GuidePage() {

    protected var renderable: Renderable? = null

    final override fun drawPage(mouseX: Int, mouseY: Int) {
        GlStateManager.translate(paddingX.toFloat(), paddingY.toFloat(), 0f)
        renderable?.render(paddingX, paddingY)
        GlStateManager.translate(-paddingX.toFloat(), -paddingY.toFloat(), 0f)
    }

    override fun onLeave() {
        renderable = null
    }

}
