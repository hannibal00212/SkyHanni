package at.hannibal2.skyhanni.utils

import java.awt.Color

class ExtendedChatColor(
    val rgb: Int,
    val hasAlpha: Boolean = false,
) {
    override fun toString(): String {
        val stringBuilder = StringBuilder()
        val hexCode = rgb.toUInt().toString(16)
            .padStart(8, '0')
            .drop(if (hasAlpha) 0 else 2)
        stringBuilder.append("§#")
        for (code in hexCode) {
            stringBuilder.append('§').append(code)
        }
        stringBuilder.append("§/")
        return stringBuilder.toString()
    }

    companion object {

        fun testCommand() {
            val string = StringBuilder()
            for (i in (0 until 100)) {
                val color = Color.HSBtoRGB(i / 100F, 1f, 1f)
                val extendedChatColor = ExtendedChatColor(color, false)
                string.append("$extendedChatColor§m ")
            }
            ChatUtils.chat(string.toString())
        }
    }
}
