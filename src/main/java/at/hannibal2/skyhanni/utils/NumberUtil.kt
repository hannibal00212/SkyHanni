package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import java.text.NumberFormat
import java.util.Locale
import java.util.TreeMap
import kotlin.math.pow

object NumberUtil {

    private val config get() = SkyHanniMod.feature

    private val suffixes = TreeMap<Long, String>().apply {
        this[1000L] = "k"
        this[1000000L] = "M"
        this[1000000000L] = "B"
        this[1000000000000L] = "T"
        this[1000000000000000L] = "P"
        this[1000000000000000000L] = "E"
    }

    private val romanSymbols = TreeMap(
        mapOf(
            1000 to "M",
            900 to "CM",
            500 to "D",
            400 to "CD",
            100 to "C",
            90 to "XC",
            50 to "L",
            40 to "XL",
            10 to "X",
            9 to "IX",
            5 to "V",
            4 to "IV",
            1 to "I",
        ),
    )

    // 1234 -> 1.2k
    fun Number.shortFormat(preciseBillions: Boolean = false): String {
        return compactFormat(this, preciseBillions)
    }

    /**
     * This code was modified and taken under CC BY-SA 3.0 license
     * @link https://stackoverflow.com/a/30661479
     * @author assylias
     */
    private fun compactFormat(value: Number, preciseBillions: Boolean = false): String {
        @Suppress("NAME_SHADOWING")
        val value = value.toLong()
        // Long.MIN_VALUE == -Long.MIN_VALUE, so we need an adjustment here
        if (value == Long.MIN_VALUE) return compactFormat(Long.MIN_VALUE + 1, preciseBillions)
        if (value < 0) return "-" + compactFormat(-value, preciseBillions)

        if (value < 1000) return value.toString() // deal with small numbers

        val (divideBy, suffix) = suffixes.floorEntry(value)

        val truncated = value / (divideBy / 10) // the number part of the output times 10

        val truncatedAt = if (suffix == "M") 1000 else if (suffix == "B") 1000000 else 100

        val hasDecimal = truncated < truncatedAt && truncated / 10.0 != (truncated / 10).toDouble()

        return if (value > 1_000_000_000 && hasDecimal && preciseBillions) {
            val decimalPart = (value % 1_000_000_000) / 1_000_000
            "${truncated / 10}.$decimalPart$suffix"
        } else {
            if (hasDecimal) (truncated / 10.0).toString() + suffix else (truncated / 10).toString() + suffix
        }
    }

    /**
     * This code was unmodified and taken under CC BY-SA 3.0 license
     * @link https://stackoverflow.com/a/22186845
     * @author jpdymond
     */
    fun Double.roundTo(precision: Int): Double {
        val scale = 10.0.pow(precision)
        return kotlin.math.round(this * scale) / scale
    }

    fun Float.roundTo(precision: Int): Float = toDouble().roundTo(precision).toFloat()

    fun Number.ordinal(): String {
        val long = this.toLong()
        if (long % 100 in 11..13) return "th"
        return when (long % 10) {
            1L -> "st"
            2L -> "nd"
            3L -> "rd"
            else -> "th"
        }
    }

    fun Number.addSuffix(): String {
        return this.toString() + this.ordinal()
    }

    fun Number.addSeparators(): String {
        return if (!config.dev.numberFormatOverride) NumberFormat.getNumberInstance().format(this)
        else NumberFormat.getNumberInstance(Locale.US).format(this)
    }

    fun String.romanToDecimalIfNecessary() = toIntOrNull() ?: romanToDecimal()

    /**
     * This code was converted to Kotlin and taken under CC BY-SA 3.0 license
     * @link https://stackoverflow.com/a/9073310
     */
    fun String.romanToDecimal(): Int {
        var decimal = 0
        var lastNumber = 0
        val romanNumeral = this.uppercase()
        for (x in romanNumeral.length - 1 downTo 0) {
            when (romanNumeral[x]) {
                'M' -> {
                    decimal = processDecimal(1000, lastNumber, decimal)
                    lastNumber = 1000
                }

                'D' -> {
                    decimal = processDecimal(500, lastNumber, decimal)
                    lastNumber = 500
                }

                'C' -> {
                    decimal = processDecimal(100, lastNumber, decimal)
                    lastNumber = 100
                }

                'L' -> {
                    decimal = processDecimal(50, lastNumber, decimal)
                    lastNumber = 50
                }

                'X' -> {
                    decimal = processDecimal(10, lastNumber, decimal)
                    lastNumber = 10
                }

                'V' -> {
                    decimal = processDecimal(5, lastNumber, decimal)
                    lastNumber = 5
                }

                'I' -> {
                    decimal = processDecimal(1, lastNumber, decimal)
                    lastNumber = 1
                }
            }
        }
        return decimal
    }

    fun Int.toRoman(): String {
        if (this <= 0) error("$this must be positive!")
        val l = romanSymbols.floorKey(this)
        return if (this == l) {
            romanSymbols[this]!!
        } else romanSymbols[l] + (this - l).toRoman()
    }

    fun Number.toStringWithPlus() = (if (this.toDouble() >= 0.0) "+" else "") + this.toString()

    private fun processDecimal(decimal: Int, lastNumber: Int, lastDecimal: Int) = if (lastNumber > decimal) {
        lastDecimal - decimal
    } else {
        lastDecimal + decimal
    }

    private val numberPattern = "^[0-9]*$".toPattern()
    private val formatPattern = "^[0-9,.]*[kmb]?$".toPattern()

    fun String.isInt(): Boolean = isNotEmpty() && numberPattern.matcher(this).matches()

    fun String.isDouble(): Boolean = runCatching { toDouble() }.getOrNull() != null

    fun String.isFormatNumber(): Boolean {
        return isNotEmpty() && formatPattern.matches(this)
    }

    fun percentageColor(percentage: Double) = when {
        percentage > 0.9 -> LorenzColor.DARK_GREEN
        percentage > 0.75 -> LorenzColor.GREEN
        percentage > 0.5 -> LorenzColor.YELLOW
        percentage > 0.25 -> LorenzColor.GOLD
        else -> LorenzColor.RED
    }

    fun percentageColor(have: Long, max: Long): LorenzColor = percentageColor(have.fractionOf(max))

    fun Number.percentWithColorCode(max: Number, round: Int = 1): String {
        val fraction = this.fractionOf(max)
        val color = percentageColor(fraction)
        val amount = (fraction * 100.0).roundTo(round)
        return "${color.getChatColor()}$amount%"
    }

    fun String.formatDouble(): Double =
        formatDoubleOrNull() ?: throw NumberFormatException("formatDouble failed for '$this'")

    fun String.formatLong(): Long =
        formatDoubleOrNull()?.toLong() ?: throw NumberFormatException("formatLong failed for '$this'")

    fun String.formatInt(): Int =
        formatDoubleOrNull()?.toInt() ?: throw NumberFormatException("formatInt failed for '$this'")

    fun String.formatFloat(): Float =
        formatDoubleOrNull()?.toFloat() ?: throw NumberFormatException("formatFloat failed for '$this'")

    fun String.formatDoubleOrUserError(): Double? = formatDoubleOrNull() ?: run {
        ChatUtils.userError("Not a valid number: '$this'")
        return@run null
    }

    fun String.formatLongOrUserError(): Long? = formatDoubleOrNull()?.toLong() ?: run {
        ChatUtils.userError("Not a valid number: '$this'")
        return@run null
    }

    fun String.formatIntOrUserError(): Int? = formatDoubleOrNull()?.toInt() ?: run {
        ChatUtils.userError("Not a valid number: '$this'")
        return@run null
    }

    fun String.formatFloatOrUserError(): Float? = formatDoubleOrNull()?.toFloat() ?: run {
        ChatUtils.userError("Not a valid number: '$this'")
        return@run null
    }

    fun String.formatIntOrNull(): Int? = formatDoubleOrNull()?.toInt()

    fun String.formatDoubleOrNull(): Double? {
        var text = lowercase().replace(",", "")

        val multiplier = if (text.endsWith("k")) {
            text = text.substring(0, text.length - 1)
            1_000.0
        } else if (text.endsWith("m")) {
            text = text.substring(0, text.length - 1)
            1.million
        } else if (text.endsWith("b")) {
            text = text.substring(0, text.length - 1)
            1.billion
        } else 1.0
        return text.toDoubleOrNull()?.let {
            it * multiplier
        }
    }

    // Sometimes we just take an L, never find it and forget to write it down
    val Int.million get() = this * 1_000_000.0
    private val Int.billion get() = this * 1_000_000_000.0
    val Double.million get() = (this * 1_000_000.0).toLong()

    /** @return clamped to [0.0, 1.0]**/
    fun Number.fractionOf(maxValue: Number) = maxValue.toDouble().takeIf { it != 0.0 }?.let { max ->
        this.toDouble() / max
    }?.coerceIn(0.0, 1.0) ?: 1.0

    fun interpolate(now: Float, last: Float, lastUpdate: Long): Float {
        var interp = now
        if (last >= 0 && last != now) {
            var factor: Float = (SimpleTimeMark.now().toMillis() - lastUpdate) / 1000f
            factor = factor.coerceIn(0f, 1f)
            interp = last + (now - last) * factor
        }
        return interp
    }

}
