package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.SkyHanniMod
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@JvmInline
value class SimpleTimeMark(private val millis: Long) : Comparable<SimpleTimeMark> {

    operator fun minus(other: SimpleTimeMark) =
        (millis - other.millis).milliseconds

    operator fun plus(other: Duration) =
        SimpleTimeMark(millis + other.inWholeMilliseconds)

    operator fun minus(other: Duration) = plus(-other)

    fun passedSince() = now() - this

    fun timeUntil() = -passedSince()

    fun isInPast() = timeUntil().isNegative()

    fun isInFuture() = timeUntil().isPositive()

    fun isFarPast() = millis == 0L

    fun isFarFuture() = millis == Long.MAX_VALUE

    fun isFarPastOrFuture() = isFarPast() || isFarFuture()

    fun takeIfInitialized() = if (isFarPastOrFuture()) null else this

    override fun compareTo(other: SimpleTimeMark): Int = millis.compareTo(other.millis)

    override fun toString(): String = when (this) {
        farPast() -> "The Far Past"
        farFuture() -> "The Far Future"
        else -> Instant.ofEpochMilli(millis).toString()
    }

    fun formattedDate(pattern: String): String {
        val newPattern = if (SkyHanniMod.feature.gui.timeFormat24h) {
            pattern.replace("h", "H").replace("a", "")
        } else {
            pattern
        }

        val instant = Instant.ofEpochMilli(millis)
        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern(newPattern.trim())
        return localDateTime.format(formatter)
    }

    fun toMillis() = millis

    fun toSkyBlockTime() = SkyBlockTime.fromInstant(Instant.ofEpochMilli(millis))

    fun elapsedMinutes() = passedSince().inWholeMinutes

    companion object {

        fun now() = SimpleTimeMark(System.currentTimeMillis())

        @JvmStatic
        @JvmName("farPast")
        fun farPast() = SimpleTimeMark(0)
        fun farFuture() = SimpleTimeMark(Long.MAX_VALUE)

        fun Duration.fromNow() = now() + this

        fun Long.asTimeMark() = SimpleTimeMark(this)
        fun SkyBlockTime.asTimeMark() = SimpleTimeMark(toMillis())
    }
}
