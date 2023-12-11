package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.StringUtils.matchMatcher
import io.github.moulberry.notenoughupdates.util.SkyBlockTime
import java.time.LocalDate
import java.time.ZoneId
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

object TimeUtils {
    private val pattern =
        "(?:(?<y>\\d+) ?y(?:\\w* ?)?)?(?:(?<d>\\d+) ?d(?:\\w* ?)?)?(?:(?<h>\\d+) ?h(?:\\w* ?)?)?(?:(?<m>\\d+) ?m(?:\\w* ?)?)?(?:(?<s>\\d+) ?s(?:\\w* ?)?)?".toPattern()

    fun formatDuration(
        duration: Duration,
        biggestUnit: TimeUnit = TimeUnit.YEAR,
        showMilliSeconds: Boolean = false,
        longName: Boolean = false,
        maxUnits: Int = -1
    ): String = duration.format(biggestUnit, showMilliSeconds, longName, maxUnits)

    fun Duration.format(
        biggestUnit: TimeUnit = TimeUnit.YEAR,
        showMilliSeconds: Boolean = false,
        longName: Boolean = false,
        maxUnits: Int = -1
    ): String = formatDuration(
        inWholeMilliseconds - 999, biggestUnit, showMilliSeconds, longName, maxUnits
    )

    fun formatDuration(
        millis: Long,
        biggestUnit: TimeUnit = TimeUnit.YEAR,
        showMilliSeconds: Boolean = false,
        longName: Boolean = false,
        maxUnits: Int = -1
    ): String {
        // TODO: if this weird offset gets removed, also remove that subtraction from formatDuration(kotlin.time.Duration)
        var milliseconds = millis + 999
        val map = mutableMapOf<TimeUnit, Int>()
        for (unit in TimeUnit.entries) {
            if (unit.ordinal >= biggestUnit.ordinal) {
                val factor = unit.factor
                map[unit] = (milliseconds / factor).toInt()
                milliseconds %= factor
            }
        }

        val builder = StringBuilder()
        var count = 0
        for ((unit, value) in map.entries) {
            if (value > 0 || builder.isNotEmpty() || unit == TimeUnit.SECOND) {
                builder.append(value.addSeparators())
                val name = if (longName) {
                    " " + unit.longName + if (value > 1) "s" else ""
                } else {
                    unit.shortName
                }

                if (unit == TimeUnit.SECOND) {
                    if (showMilliSeconds) {
                        val formatMillis = milliseconds / 100
                        builder.append(".")
                        builder.append(formatMillis)
                    }
                    builder.append(name)
                } else {
                    builder.append("$name ")
                }

                count++
                if (maxUnits != -1 && count == maxUnits) break
            }
        }
        return builder.toString().trim()
    }

    @Deprecated("Do no longer use long for time", ReplaceWith("getDuration()"))
    fun getMillis(string: String) = getDuration(string).inWholeMilliseconds

    fun getDuration(string: String) = getMillis_(string.replace("m", "m ").replace("  ", " ").trim())

    private fun getMillis_(string: String) = pattern.matchMatcher(string.lowercase().trim()) {
        val years = group("y")?.toLong() ?: 0L
        val days = group("d")?.toLong() ?: 0L
        val hours = group("h")?.toLong() ?: 0L
        val minutes = group("m")?.toLong() ?: 0L
        val seconds = group("s")?.toLong() ?: 0L

        var millis = 0L
        millis += seconds * 1000
        millis += minutes * 60 * 1000
        millis += hours * 60 * 60 * 1000
        millis += days * 24 * 60 * 60 * 1000
        millis += (years * 365.25 * 24 * 60 * 60 * 1000).toLong()

        millis.toDuration(DurationUnit.MILLISECONDS)
    } ?: tryAlternativeFormat(string)

    private fun tryAlternativeFormat(string: String): Duration {
        val split = string.split(":")
        return when (split.size) {
            3 -> {
                val hours = split[0].toInt() * 1000 * 60 * 60
                val minutes = split[1].toInt() * 1000 * 60
                val seconds = split[2].toInt() * 1000
                seconds + minutes + hours
            }

            2 -> {
                val minutes = split[0].toInt() * 1000 * 60
                val seconds = split[1].toInt() * 1000
                seconds + minutes
            }

            1 -> {
                split[0].toInt() * 1000
            }

            else -> {
                throw RuntimeException("Invalid format: '$string'")
            }
        }.toLong().toDuration(DurationUnit.MILLISECONDS)
    }

    fun SkyBlockTime.formatted(): String {
        val hour = if (this.hour > 12) this.hour - 12 else this.hour
        val timeOfDay = if (this.hour > 11) "pm" else "am" // hooray for 12-hour clocks
        var minute = this.minute.toString()
        if (minute.length != 2) {
            minute = minute.padStart(2, '0')
        }

        val month = SkyBlockTime.monthName(this.month)
        val day = this.day
        val daySuffix = SkyBlockTime.daySuffix(day)
        val year = this.year
        return "$month $day$daySuffix, Year $year $hour:${minute}$timeOfDay" // Early Winter 1st Year 300, 12:03pm
    }

    fun getCurrentLocalDate(): LocalDate = LocalDate.now(ZoneId.of("UTC"))
}

private const val FACTOR_SECONDS = 1000L
private const val FACTOR_MINUTES = FACTOR_SECONDS * 60
private const val FACTOR_HOURS = FACTOR_MINUTES * 60
private const val FACTOR_DAYS = FACTOR_HOURS * 24
private const val FACTOR_YEARS = (FACTOR_DAYS * 365.25).toLong()

enum class TimeUnit(val factor: Long, val shortName: String, val longName: String) {
    YEAR(FACTOR_YEARS, "y", "Year"),
    DAY(FACTOR_DAYS, "d", "Day"),
    HOUR(FACTOR_HOURS, "h", "Hour"),
    MINUTE(FACTOR_MINUTES, "m", "Minute"),
    SECOND(FACTOR_SECONDS, "s", "Second"),
    ;
}
