package at.hannibal2.skyhanni.config.core.config

import at.hannibal2.skyhanni.utils.SimpleTimeMark
import java.awt.Color

class CustomColor {
    private val hue: Float
    private val saturation: Float
    private val brightness: Float
    private val alpha: Int
    private val chroma: Int

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

    // Constructors
    @Suppress("unused")
    constructor(hue: Float, saturation: Float, brightness: Float, alpha: Int, chroma: Int) {
        this.hue = hue
        this.saturation = saturation
        this.brightness = brightness
        this.alpha = alpha
        this.chroma = chroma
    }

    @JvmOverloads constructor(color: Color, alpha: Int = color.alpha, chroma: Int = 0) {
        val (hue, saturation, brightness) = Color.RGBtoHSB(color.red, color.green, color.blue, null)
        this.hue = hue
        this.saturation = saturation
        this.brightness = brightness
        this.alpha = alpha
        this.chroma = chroma
    }

    constructor(csv: String) {
        val list = csv.split(":").mapNotNull { it.toIntOrNull() }
        if (list.size != 5) {
            this.hue = 0.0f
            this.saturation = 0.0f
            this.brightness = 0.0f
            this.alpha = 0
            this.chroma = 0
        } else {
            this.chroma = list[0]
            this.alpha = list[1]

            val array = Color.RGBtoHSB(list[2], list[3], list[4], null)

            this.hue = array[0]
            this.saturation = array[1]
            this.brightness = array[2]
        }
    }

    companion object {
        private val startTime = SimpleTimeMark.now()
        private const val MIN_CHROMA_SECS = 1
        private const val MAX_CHROMA_SECS = 60
        private fun chromaSpeed(speed: Int) = (255 - speed) / 254f * (MAX_CHROMA_SECS - MIN_CHROMA_SECS) + MIN_CHROMA_SECS
    }
}
