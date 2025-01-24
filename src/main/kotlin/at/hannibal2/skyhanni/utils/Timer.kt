package at.hannibal2.skyhanni.utils

import com.google.gson.annotations.Expose
import kotlin.time.Duration

class Timer(
    @Expose
    var duration: Duration,

    @Expose
    private var started: SimpleTimeMark = SimpleTimeMark.now(),

    startPaused: Boolean = false,
) : Comparable<Timer> {

    @Expose
    private var paused: SimpleTimeMark? = null

    init {
        if (startPaused) {
            paused = started
        }
    }

    val ended get() = !remaining.isPositive()
    val remaining get() = duration - elapsed
    val elapsed get() = paused?.let { it - started } ?: started.passedSince()

    fun pause() {
        paused = SimpleTimeMark.now()
    }

    fun resume() {
        paused?.let {
            started = it
            duration = it - started
            paused = null
        }
    }

    override fun compareTo(other: Timer): Int = remaining.compareTo(other.remaining)
}
