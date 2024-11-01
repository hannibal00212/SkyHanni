package at.hannibal2.skyhanni.config.core.elements

import java.awt.Color

class GuiElementButton(text: String, colour: Int, private val callback: Runnable) : GuiElementText(text, colour) {

    override val height: Int
        get() = super.height + 5

    override val width: Int
        get() = super.width + 10

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        callback.run()
    }

    override fun render(x: Int, y: Int) {
        drawRect(x, y, x + width, y + super.height, Color.WHITE.rgb)
        drawRect(x + 1, y + 1, x + width - 1, y + super.height - 1, Color.BLACK.rgb)
        super.render(x + 5, y - 1)
    }
}
