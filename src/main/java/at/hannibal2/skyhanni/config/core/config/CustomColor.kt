package at.hannibal2.skyhanni.config.core.config

import at.hannibal2.skyhanni.utils.SimpleTimeMark
import java.awt.Color

class CustomColor(
    private val hue: Float,
    private val saturation: Float,
    private val brightness: Float,
    private val alpha: Int,
    private val chroma: Int,
) {

    fun toInt(): Int {
        val adjustedHue = if (chroma <= 0) hue
        else (hue + (startTime.passedSince().inWholeMilliseconds / 1000f / chromaSpeed(chroma) % 1)).let {
            if (it < 0) it + 1f else it
        }

        return (alpha and 0xFF) shl 24 or (Color.HSBtoRGB(adjustedHue, saturation, brightness) and 0x00FFFFFF)
    }

    fun toColor(): Color = Color(toInt(), true)

    val asString get(): String {
        val color = Color.HSBtoRGB(hue, saturation, brightness)
        val red = color shr 16 and 0xFF
        val green = color shr 8 and 0xFF
        val blue = color and 0xFF
        return "$chroma:$alpha:$red:$green:$blue"
    }

    companion object {
        // Sneaky workaround to make it look like it has a constructor with a string parameter, because actually making a constructor
        // like that is more complicated than its worth it. Does not work when called from java code.
        operator fun invoke(csv: String): CustomColor = fromString(csv)

        private val DEFAULT = CustomColor(0.0f, 0.0f, 0.0f, 0, 0)
        private val startTime = SimpleTimeMark.now()
        private const val MIN_CHROMA_SECS = 1
        private const val MAX_CHROMA_SECS = 60
        private fun chromaSpeed(speed: Int) = (255 - speed) / 254f * (MAX_CHROMA_SECS - MIN_CHROMA_SECS) + MIN_CHROMA_SECS

        @JvmStatic
        fun fromString(csv: String): CustomColor {
            val list = csv.split(":").mapNotNull { it.toIntOrNull() }
            if (list.size != 5) return DEFAULT
            val chroma = list[0]
            val alpha = list[1]
            val (hue, saturation, brightness) = Color.RGBtoHSB(list[2], list[3], list[4], null)
            return CustomColor(hue, saturation, brightness, alpha, chroma)
        }
    }

}
