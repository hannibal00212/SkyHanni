package at.hannibal2.skyhanni.utils

import java.awt.Color

/**
 * Taken from NotEnoughUpdates,
 * translated to Kotlin and modified.
 */
object SpecialColor {
    private const val MIN_CHROMA_SECS = 1
    private const val MAX_CHROMA_SECS = 60
    private var startTime = SimpleTimeMark.farPast()

    @Deprecated("", ReplaceWith("this.toChromaColor()"))
    fun specialToChromaRGB(special: String): Int {
        if (startTime.isFarPast()) startTime = SimpleTimeMark.now()

        val (b, g, r, a, chroma) = decompose(special)
        val (hue, sat, bri) = Color.RGBtoHSB(r, g, b, null)

        val adjustedHue = if (chroma > 0) (hue + (startTime.passedSince().inWholeMilliseconds / 1000f / chromaSpeed(chroma) % 1)).let {
            if (it < 0) it + 1f else it
        } else hue

        return (a and 0xFF) shl 24 or (Color.HSBtoRGB(adjustedHue, sat, bri) and 0x00FFFFFF)
    }

    private fun decompose(csv: String) = csv.split(":").mapNotNull { it.toIntOrNull() }.reversed().toIntArray()
    private fun chromaSpeed(speed: Int) = (255 - speed) / 254f * (MAX_CHROMA_SECS - MIN_CHROMA_SECS) + MIN_CHROMA_SECS
}
