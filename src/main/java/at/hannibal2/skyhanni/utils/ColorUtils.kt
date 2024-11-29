package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.utils.SpecialColor.toSpecialColor
import at.hannibal2.skyhanni.utils.SpecialColor.toSpecialColorInt
import java.awt.Color

object ColorUtils {

    @Deprecated("Use toSpecialColor() instead", ReplaceWith("this.toSpecialColor()"))
    fun String.toChromaColor() = this.toSpecialColor()

    @Deprecated("Use toSpecialColorInt() instead", ReplaceWith("this.toSpecialColorInt()"))
    fun String.toChromaColorInt() = this.toSpecialColorInt()

    fun String.getFirstColorCode() = takeIf { it.firstOrNull() == '§' }?.getOrNull(1)

    fun getRed(color: Int) = color shr 16 and 0xFF

    fun getGreen(color: Int) = color shr 8 and 0xFF

    fun getBlue(color: Int) = color and 0xFF

    fun getAlpha(color: Int) = color shr 24 and 0xFF

    fun blendRGB(start: Color, end: Color, percent: Double) = Color(
        (start.red * (1 - percent) + end.red * percent).toInt(),
        (start.green * (1 - percent) + end.green * percent).toInt(),
        (start.blue * (1 - percent) + end.blue * percent).toInt(),
    )

    fun Color.getExtendedColorCode(hasAlpha: Boolean = false): String = ExtendedChatColor(rgb, hasAlpha).toString()

    /** Darkens a color by a [factor]. The lower the [factor], the darker the color. */
    fun Color.darker(factor: Double = 0.7) = Color(
        (red * factor).toInt().coerceIn(0, 255),
        (green * factor).toInt().coerceIn(0, 255),
        (blue * factor).toInt().coerceIn(0, 255),
        alpha,
    )

    val TRANSPARENT_COLOR = Color(0, 0, 0, 0)

    fun Color.addAlpha(alpha: Int): Color = Color(red, green, blue, alpha)

    fun getColorFromHex(hex: String): Int = runCatching { Color(Integer.decode(hex)) }.getOrNull()?.rgb ?: 0
}
