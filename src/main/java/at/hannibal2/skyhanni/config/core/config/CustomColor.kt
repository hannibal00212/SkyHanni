package at.hannibal2.skyhanni.config.core.config

import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.inPartialSeconds
import java.awt.Color

class CustomColor {
    private val hue: Float
    private val saturation: Float
    private val brightness: Float
    private val alpha: Int
    private val chroma: Int

    private val cachedrgb: Int // Cached rgb value, used for creating the string and for toInt() if the color isn't chroma

    fun toInt(): Int {
        if (chroma == 0) return cachedrgb
        val adjustedHue = (hue + (startTime.passedSince().inPartialSeconds.toFloat() / chromaSpeed(chroma) % 1)).let {
            if (it < 0) it + 1f else it
        }

        return (alpha and 0xFF) shl 24 or (Color.HSBtoRGB(adjustedHue, saturation, brightness) and 0x00FFFFFF)
    }

    fun toColor(): Color = Color(toInt(), true)

    val asString get(): String {
        val red = cachedrgb shr 16 and 0xFF
        val green = cachedrgb shr 8 and 0xFF
        val blue = cachedrgb and 0xFF
        return "$chroma:$alpha:$red:$green:$blue"
    }

    @JvmOverloads constructor(color: Color, alpha: Int = color.alpha, chroma: Int = 0) {
        val (hue, saturation, brightness) = Color.RGBtoHSB(color.red, color.green, color.blue, null)
        this.hue = hue
        this.saturation = saturation
        this.brightness = brightness
        this.alpha = alpha
        this.chroma = chroma
        this.cachedrgb = color.rgb
    }

    constructor(string: String) {
        val list = string.split(":").mapNotNull { it.toIntOrNull() }
        if (list.size != 5) {
            this.hue = 0.0f
            this.saturation = 0.0f
            this.brightness = 0.0f
            this.alpha = 0
            this.chroma = 0
            this.cachedrgb = 0
        } else {
            this.chroma = list[0]
            this.alpha = list[1]

            val red = list[2]
            val green = list[3]
            val blue = list[4]

            val array = Color.RGBtoHSB(red, green, blue, null)

            this.hue = array[0]
            this.saturation = array[1]
            this.brightness = array[2]
            this.cachedrgb = ((alpha and 0xFF) shl 24) or ((red and 0xFF) shl 16) or ((green and 0xFF) shl 8) or (blue and 0xFF)
        }
    }

    companion object {
        private val startTime = SimpleTimeMark.now()
        private const val MIN_CHROMA_SECS = 1
        private const val MAX_CHROMA_SECS = 60
        private fun chromaSpeed(speed: Int) = (255 - speed) / 254f * (MAX_CHROMA_SECS - MIN_CHROMA_SECS) + MIN_CHROMA_SECS
    }
}
