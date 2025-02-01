package at.hannibal2.skyhanni.utils.mc

typealias Font = net.minecraft.client.gui.FontRenderer

object McFont {

    val font: Font get() = McClient.minecraft.fontRendererObj
    val height: Int get() = font.FONT_HEIGHT

    fun width(text: String?) = font.getStringWidth(text)
    fun width(char: Char) = font.getCharWidth(char)
    fun draw(text: String?, x: Float, y: Float, color: Int, shadow: Boolean = true) = font.drawString(text, x, y, color, shadow)
    fun getColorCode(color: Char) = font.getColorCode(color)
}
