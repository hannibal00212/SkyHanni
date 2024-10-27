package at.hannibal2.skyhanni.config.core.elements

import net.minecraft.client.gui.Gui

@Suppress("EmptyFunctionBlock", "UnusedParameter")
abstract class GuiElement : Gui() {
    abstract fun render(x: Int, y: Int)

    abstract val width: Int

    abstract val height: Int

    open fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {}

    fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {}

    fun otherComponentClick() {}

    fun keyTyped(typedChar: Char, keyCode: Int) {}
}
