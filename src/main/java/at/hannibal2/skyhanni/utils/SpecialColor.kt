package at.hannibal2.skyhanni.utils

import java.awt.Color

/**
 * Taken from NotEnoughUpdates,
 * translated to Kotlin and slightly modified.
 */
object SpecialColor {
    private const val RADIX = 10
    private const val MIN_CHROMA_SECS = 1
    private const val MAX_CHROMA_SECS = 60

    private var startTime: Long = -1

    @Deprecated("", ReplaceWith("this.toChromaColor()"))
    fun specialToChromaRGB(special: String): Int {
        if (startTime < 0) startTime = System.currentTimeMillis()

        val (r, g, b, a, chroma) = decompose(special)
        var (hue, saturation, brightness) = Color.RGBtoHSB(r, g, b, null)

        if (chroma > 0) {
            hue += ((System.currentTimeMillis() - startTime) / 1000f / getSecondsForSpeed(chroma) % 1)
            if (hue < 0) hue += 1f
        }

        return (a and 0xFF) shl 24 or (Color.HSBtoRGB(hue, saturation, brightness) and 0x00FFFFFF)
    }

    private fun decompose(csv: String) = csv.split(":").dropLastWhile { it.isEmpty() }.toTypedArray().map { it.toInt(RADIX) }.toIntArray()

    private fun getSecondsForSpeed(speed: Int) = (255 - speed) / 254f * (MAX_CHROMA_SECS - MIN_CHROMA_SECS) + MIN_CHROMA_SECS

}
