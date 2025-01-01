package at.hannibal2.skyhanni.utils.tracker

import at.hannibal2.skyhanni.utils.TimeUtils.monthFormatter
import at.hannibal2.skyhanni.utils.TimeUtils.weekFormatter
import at.hannibal2.skyhanni.utils.TimeUtils.yearFormatter
import at.hannibal2.skyhanni.utils.tracker.SkyHanniTracker.DisplayMode
import com.google.gson.annotations.Expose
import java.time.LocalDate
import java.util.*

abstract class TimedTrackerData<Data : TrackerData>(
    private val createNewSession: () -> Data,
) : TrackerData() {
    override fun reset() {
        sessions = EnumMap(DisplayMode::class.java)
    }

    fun getOrPutEntry(displayMode: DisplayMode, date: LocalDate = LocalDate.now()): Data {
        val key = when (displayMode) {
            DisplayMode.TOTAL, DisplayMode.SESSION -> displayMode.name.lowercase()
            DisplayMode.MAYOR -> return createNewSession()
            DisplayMode.DAY -> date.toString()
            DisplayMode.WEEK -> date.format(weekFormatter)
            DisplayMode.MONTH -> date.format(monthFormatter)
            DisplayMode.YEAR -> date.format(yearFormatter)
        }
        val display = sessions.getOrPut(displayMode) { mutableMapOf() }
        return display.getOrPut(key) { createNewSession() }
    }

    fun getEntries(displayMode: DisplayMode): MutableMap<String, Data>? {
        return sessions[displayMode]
    }

    fun getEntry(displayMode: DisplayMode, date: LocalDate = LocalDate.now()): Data? {
        val key = when (displayMode) {
            DisplayMode.TOTAL, DisplayMode.SESSION -> displayMode.name.lowercase()
            DisplayMode.MAYOR -> return null
            DisplayMode.DAY -> date.toString()
            DisplayMode.WEEK -> date.format(weekFormatter)
            DisplayMode.MONTH -> date.format(monthFormatter)
            DisplayMode.YEAR -> date.format(yearFormatter)
        }
        return getEntries(displayMode)?.get(key)
    }

    @Expose
    private var sessions: MutableMap<DisplayMode, MutableMap<String, Data>> = EnumMap(DisplayMode::class.java)

}
