package at.hannibal2.skyhanni.utils

import java.time.Instant

/**
 * SkyBlockTime Utility
 * Originally in NEU; copied and modified with permission.
 * @author hannibal, nea89o
 * Modified further by walker
 */
data class SkyBlockTime(
    val year: Int = 1,
    val month: Int = 1,
    val day: Int = 1,
    val hour: Int = 0,
    val minute: Int = 0,
    val second: Int = 0,
) {

    val monthName get() = monthName(month)
    val dayName get() = "$day${daySuffix(day)}"

    fun toInstant(): Instant? = Instant.ofEpochMilli(toMillis())

    fun toMillis(): Long =
        calculateTimeInSkyBlockMillis(year, month, day, hour, minute, second) + SKYBLOCK_EPOCH_START_MILLIS

    companion object {
        private const val SKYBLOCK_EPOCH_START_MILLIS = 1559829300000L // Day 1, Year 1
        const val SKYBLOCK_YEAR_MILLIS = 124 * 60 * 60 * 1000L
        const val SKYBLOCK_SEASON_MILLIS = SKYBLOCK_YEAR_MILLIS / 4
        private const val SKYBLOCK_MONTH_MILLIS = SKYBLOCK_YEAR_MILLIS / 12
        const val SKYBLOCK_DAY_MILLIS = SKYBLOCK_MONTH_MILLIS / 31
        const val SKYBLOCK_HOUR_MILLIS = SKYBLOCK_DAY_MILLIS / 24
        private const val SKYBLOCK_MINUTE_MILLIS = SKYBLOCK_HOUR_MILLIS / 60
        private const val SKYBLOCK_SECOND_MILLIS = SKYBLOCK_MINUTE_MILLIS / 60

        fun fromInstant(instant: Instant): SkyBlockTime =
            calculateSkyBlockTime(instant.toEpochMilli() - SKYBLOCK_EPOCH_START_MILLIS)

        fun fromSbYear(year: Int): SkyBlockTime =
            fromInstant(Instant.ofEpochMilli(SKYBLOCK_EPOCH_START_MILLIS + (SKYBLOCK_YEAR_MILLIS * year)))

        fun fromSbYearAndMonth(year: Int, month: Int): SkyBlockTime =
            fromInstant(
                Instant.ofEpochMilli(
                    SKYBLOCK_EPOCH_START_MILLIS +
                        (SKYBLOCK_YEAR_MILLIS * year) +
                        (SKYBLOCK_MONTH_MILLIS * month)
                )
            )

        fun now(): SkyBlockTime = fromInstant(Instant.now())

        private fun calculateSkyBlockTime(realMillis: Long): SkyBlockTime {
            var remainingMillis = realMillis
            val year = getUnit(remainingMillis, SKYBLOCK_YEAR_MILLIS)
            remainingMillis %= SKYBLOCK_YEAR_MILLIS
            val month = getUnit(remainingMillis, SKYBLOCK_MONTH_MILLIS) + 1
            remainingMillis %= SKYBLOCK_MONTH_MILLIS
            val day = getUnit(remainingMillis, SKYBLOCK_DAY_MILLIS) + 1
            remainingMillis %= SKYBLOCK_DAY_MILLIS
            val hour = getUnit(remainingMillis, SKYBLOCK_HOUR_MILLIS)
            remainingMillis %= SKYBLOCK_HOUR_MILLIS
            val minute = getUnit(remainingMillis, SKYBLOCK_MINUTE_MILLIS)
            remainingMillis %= SKYBLOCK_MINUTE_MILLIS
            val second = getUnit(remainingMillis, SKYBLOCK_SECOND_MILLIS)
            return SkyBlockTime(year, month, day, hour, minute, second)
        }

        private fun getUnit(millis: Long, factor: Long): Int = (millis / factor).toInt()

        private fun calculateTimeInSkyBlockMillis(
            year: Int,
            month: Int,
            day: Int,
            hour: Int,
            minute: Int,
            second: Int
        ): Long {
            var time = 0L
            time += year * SKYBLOCK_YEAR_MILLIS
            time += (month - 1) * SKYBLOCK_MONTH_MILLIS
            time += (day - 1) * SKYBLOCK_DAY_MILLIS
            time += hour * SKYBLOCK_HOUR_MILLIS
            time += minute * SKYBLOCK_MINUTE_MILLIS
            time += second * SKYBLOCK_SECOND_MILLIS
            return time
        }

        fun monthName(month: Int): String {
            val prefix = when ((month - 1) % 3) {
                0 -> "Early "
                1 -> ""
                2 -> "Late "
                else -> "Undefined!"
            }

            val name = when ((month - 1) / 3) {
                0 -> "Spring"
                1 -> "Summer"
                2 -> "Autumn"
                3 -> "Winter"
                else -> "Undefined!"
            }

            return prefix + name
        }

        fun daySuffix(n: Int): String {
            return if (n in 11..13) {
                "th"
            } else when (n % 10) {
                1 -> "st"
                2 -> "nd"
                3 -> "rd"
                else -> "th"
            }
        }
    }
}


