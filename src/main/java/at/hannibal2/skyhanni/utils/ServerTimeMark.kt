package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.data.MinecraftData
import at.hannibal2.skyhanni.utils.TimeUtils.inWholeTicks
import at.hannibal2.skyhanni.utils.TimeUtils.ticks
import kotlin.time.Duration

/**
 * This is a Helper Class similar to [SimpleTimeMark], but for a rough estimate of Server Ticks instead of real time.
 *
 * This can provide a more accurate estimate of certain timers for ingame events, because some are based off of
 * the server's tps instead of real time, and therefore are affected by server lag.
 */
@JvmInline
value class ServerTimeMark private constructor(val ticks: Long) : Comparable<ServerTimeMark> {

    operator fun minus(other: ServerTimeMark): Duration =
        (ticks - other.ticks).ticks

    operator fun plus(other: Duration) =
        ServerTimeMark(ticks + other.inWholeTicks)

    operator fun minus(other: Duration): ServerTimeMark = plus(-other)

    fun passedSince(): Duration = now() - this

    fun timeUntil(): Duration = -passedSince()

    fun isInPast(): Boolean = timeUntil().isNegative()

    fun isInFuture(): Boolean = timeUntil().isPositive()

    fun isFarPast(): Boolean = this == FAR_PAST

    fun isFarFuture(): Boolean = ticks == FAR_FUTURE_TICKS

    override fun compareTo(other: ServerTimeMark): Int = ticks.compareTo(other.ticks)

    override fun toString(): String = when (this) {
        FAR_PAST -> "The Far Past"
        FAR_FUTURE -> "The Far Future"
        else -> "ServerTimeMark(ticks=$ticks, now=${MinecraftData.totalServerTicks})"
    }

    companion object {

        fun now() = ServerTimeMark(MinecraftData.totalServerTicks)

        private const val FAR_PAST_TICKS = Long.MIN_VALUE
        private const val FAR_FUTURE_TICKS = Long.MAX_VALUE

        val FAR_PAST = ServerTimeMark(FAR_PAST_TICKS)
        val FAR_FUTURE = ServerTimeMark(FAR_FUTURE_TICKS)

    }

}
