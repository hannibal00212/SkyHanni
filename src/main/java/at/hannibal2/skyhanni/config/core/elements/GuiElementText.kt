package at.hannibal2.skyhanni.config.core.elements

import net.minecraft.client.Minecraft

open class GuiElementText(var text: String, private val colour: Int) : GuiElement() {

    override val height: Int
        get() = 18

    override val width: Int
        get() {
            val fr = Minecraft.getMinecraft().fontRendererObj
            return fr.getStringWidth(text)
        }

    override fun render(x: Int, y: Int) {
        val fr = Minecraft.getMinecraft().fontRendererObj

        fr.drawString(text, x, y + 6, colour)
    }
}
