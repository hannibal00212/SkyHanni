package at.hannibal2.skyhanni.utils.tracker

import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.TimeUtils.toMonthString
import at.hannibal2.skyhanni.utils.TimeUtils.toWeekString
import at.hannibal2.skyhanni.utils.tracker.SkyHanniTracker.DisplayMode
import com.google.gson.annotations.Expose
import java.time.LocalDate
import java.util.EnumMap

abstract class TimedTrackerData<Data : TrackerData>(
    private val createNewSession: () -> Data,
) : TrackerData() {
    override fun reset() {
        sessions = EnumMap(DisplayMode::class.java)
    }

    init {
        ChatUtils.debug("Initialized")
    }

    fun getOrPutEntry(displayMode: DisplayMode, date: LocalDate = LocalDate.now()): Data {
        val key = when (displayMode) {
            DisplayMode.TOTAL, DisplayMode.SESSION -> displayMode.name.lowercase()
            DisplayMode.MAYOR -> return createNewSession()
            DisplayMode.DAY -> date.toString()
            DisplayMode.WEEK -> date.toWeekString()
            DisplayMode.MONTH -> date.toMonthString()
            DisplayMode.YEAR -> date.year.toString()
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
            DisplayMode.WEEK -> date.toWeekString()
            DisplayMode.MONTH -> date.toMonthString()
            DisplayMode.YEAR -> date.year.toString()
        }
        return getEntries(displayMode)?.get(key)
    }

    @Expose
    private var sessions: MutableMap<DisplayMode, MutableMap<String, Data>> = EnumMap(DisplayMode::class.java)

}
