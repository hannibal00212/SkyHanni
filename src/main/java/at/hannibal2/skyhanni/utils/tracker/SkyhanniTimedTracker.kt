package at.hannibal2.skyhanni.utils.tracker

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.DateChangeEvent
import at.hannibal2.skyhanni.utils.CollectionUtils.addString
import at.hannibal2.skyhanni.utils.KeyboardManager
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.TimeUtils.toMonthString
import at.hannibal2.skyhanni.utils.TimeUtils.toWeekString
import at.hannibal2.skyhanni.utils.TimeUtils.toWeekStringFormatted
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.Searchable
import at.hannibal2.skyhanni.utils.renderables.buildSearchBox
import at.hannibal2.skyhanni.utils.renderables.toRenderable
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

@Suppress("SpreadOperator")
class SkyhanniTimedTracker<Data : TrackerData>(
    name: String,
    createNewSession: () -> Data,
    private var storage: (ProfileSpecificStorage) -> TimedTrackerData<Data>,
    drawDisplay: (Data) -> List<Searchable>,
    vararg extraStorage: Pair<DisplayMode, (ProfileSpecificStorage) -> Data>,
) : SkyHanniTracker<Data>(
    name,
    createNewSession,
    { throw UnsupportedOperationException("getStorage not used") },
    *extraStorage,
    drawDisplay = drawDisplay
) {

    private val availableTrackers = arrayOf(
        DisplayMode.TOTAL,
        DisplayMode.SESSION,
        DisplayMode.DAY,
        DisplayMode.WEEK,
        DisplayMode.MONTH,
        DisplayMode.YEAR,
    ) + extraDisplayModes.keys

    @HandleEvent
    fun onDateChange(event: DateChangeEvent) {
        if (date == event.oldDate) {
            date = event.newDate
        }
        if (week == event.oldDate.toWeekString().weekToLocalDate()) {
            week = event.newDate.toWeekString().weekToLocalDate()
        }
        if (month == event.oldDate.toMonthString().monthToLocalDate()) {
            month = event.newDate.toMonthString().monthToLocalDate()
        }
        if (year == event.oldDate.year.toString().yearToLocalDate()) {
            year = event.newDate.year.toString().yearToLocalDate()
        }
    }

    private var date = LocalDate.now()
    private var week = LocalDate.now().toWeekString().weekToLocalDate()
    private var month = LocalDate.now().toMonthString().monthToLocalDate()
    private var year = LocalDate.now().year.toString().yearToLocalDate()

    private fun getNextDisplay(): DisplayMode {
        return availableTrackers[(availableTrackers.indexOf(displayMode) + 1) % availableTrackers.size]
    }

    private fun getPreviousDisplay(): DisplayMode {
        return availableTrackers[(availableTrackers.indexOf(displayMode) - 1 + availableTrackers.size) % availableTrackers.size]
    }

    private fun ProfileSpecificStorage.getData() = storage(this)
    private fun ProfileSpecificStorage.getDisplay(displayMode: DisplayMode, date: LocalDate = LocalDate.now()) =
        this.getData().getOrPutEntry(displayMode, date)

    override fun getSharedTracker() = ProfileStorageData.profileSpecific?.let { ps ->
        SharedTracker(
            availableTrackers.associateWith { ps.getDisplay(it) }
        )
    }

    private fun buildLore(): List<String> {
        val currentTracker = "§7Current Mode: §a${displayMode?.displayName ?: "none"}"
        val nextTracker = "§7Next Mode: §a${getNextDisplay().displayName} §e(Click)"
        val lastTracker = "§7Previous Mode: §a${getPreviousDisplay().displayName} §e(Ctrl + Click)"
        return listOf(currentTracker, nextTracker, lastTracker)
    }

    override fun buildDisplayModeView() = Renderable.clickAndHover(
        "§7Display Mode: §a[§e${displayMode?.displayName ?: "§anone"}§a]", buildLore(),
        onClick = {
            displayMode = if (KeyboardManager.isModifierKeyDown()) {
                getPreviousDisplay()
            } else {
                getNextDisplay()
            }
            storedTrackers[name] = displayMode
            update()
        }
    )

    override fun getDisplay() = ProfileStorageData.profileSpecific?.let { ps ->
        val data = when (getDisplayMode()) {
            DisplayMode.WEEK -> ps.getDisplay(getDisplayMode(), week)
            DisplayMode.MONTH -> ps.getDisplay(getDisplayMode(), month)
            DisplayMode.YEAR -> ps.getDisplay(getDisplayMode(), year)
            else -> ps.getDisplay(getDisplayMode(), date)
        }
        val searchables = drawDisplay(data)
        if (config.trackerSearchEnabled.get()) buildFinalDisplay(searchables.buildSearchBox(textInput))
        else buildFinalDisplay(Renderable.verticalContainer(searchables.toRenderable()))
    }.orEmpty()


    fun buildDate() = Renderable.verticalContainer(
        buildList {
            val displayText: String = when (displayMode) {
                DisplayMode.DAY -> {
                    val dateString = if (date == LocalDate.now()) "Today" else date.toString()
                    "§7Date: §a$dateString"
                }
                DisplayMode.WEEK -> {
                    val dateString =
                        if (week.toWeekString() == LocalDate.now().toWeekString()) "This Week" else week.toWeekStringFormatted()
                    "§7Week: §a$dateString"
                }

                DisplayMode.MONTH -> {
                    val dateString = if (month.toMonthString() == LocalDate.now().toMonthString()) "This Month" else month.toMonthString()
                    "§7Month: §a$dateString"
                }

                DisplayMode.YEAR -> {
                    val dateString = if (year.year == LocalDate.now().year) "This Year" else year.year.toString()
                    "§7Year: §a$dateString"
                }

                else -> {
                    "§7Mode: §a${displayMode?.displayName ?: "none"}"
                }
            }
            addString(displayText)
        }
    )

    override fun buildFinalDisplay(searchBox: Renderable) = buildList {
        if (inventoryOpen) {
            buildDateSwitcherView()?.let { dateSwitcherView ->
                add(
                    Renderable.horizontalContainer(
                        dateSwitcherView,
                        spacing = 5,
                        horizontalAlign = RenderUtils.HorizontalAlignment.CENTER,
                    ),
                )
            }
        }
        add(searchBox)
        if (isEmpty()) return@buildList
        if (inventoryOpen) {
            add(buildDisplayModeView())
            if (getDisplayMode() == DisplayMode.SESSION) {
                add(buildSessionResetButton())
            }
        }
    }

    private fun String.dayToLocalDate(): LocalDate = LocalDate.parse(this)

    private fun String.weekToLocalDate(): LocalDate {
        val str = this.split("-")
        val year = str[0].toIntOrNull() ?: throw IllegalArgumentException("invalid year")
        val week = str[1].toIntOrNull() ?: throw IllegalArgumentException("invalid week")
        val weekFields = WeekFields.of(Locale.getDefault())

        return LocalDate.now()
            .withYear(year)
            .with(weekFields.weekOfYear(), week.toLong())
            .with(weekFields.dayOfWeek(), 1)
    }

    private fun String.monthToLocalDate(): LocalDate {
        val str = this.split("-")
        val year = str[0].toIntOrNull() ?: throw IllegalArgumentException("invalid year")
        val month = str[1].toIntOrNull() ?: throw IllegalArgumentException("invalid month")
        return LocalDate.of(year, month, 1)
    }

    private fun String.yearToLocalDate(): LocalDate {
        val year = this.trim().toIntOrNull() ?: throw IllegalArgumentException("invalid year")
        return LocalDate.ofYearDay(year, 1)
    }

    @Suppress("ReturnCount")
    private fun buildDateSwitcherView(): List<Renderable>? {
        val statsStorage = ProfileStorageData.profileSpecific?.getData() ?: return null
        val entries = statsStorage.getEntries(getDisplayMode())?.keys ?: return null
        val display = when (getDisplayMode()) {
            DisplayMode.DAY -> {
                val previousDay = entries.filter { it.dayToLocalDate() < date }.maxOrNull()?.dayToLocalDate()
                val nextDay = entries.filter { it.dayToLocalDate() > date }.minOrNull()?.dayToLocalDate()
                if (previousDay == null && nextDay == null) return null
                buildDateSwitcherButtons(previousDay, nextDay)
            }
            DisplayMode.WEEK -> {
                val previousWeek = entries.filter { it.weekToLocalDate() < week }.maxOrNull()?.weekToLocalDate()
                val nextWeek = entries.filter { it.weekToLocalDate() > week }.minOrNull()?.weekToLocalDate()
                if (previousWeek == null && nextWeek == null) return null
                buildDateSwitcherButtons(previousWeek, nextWeek)
            }
            DisplayMode.MONTH -> {
                val previousMonth = entries.filter { it.monthToLocalDate() < month }.maxOrNull()?.monthToLocalDate()
                val nextMonth = entries.filter { it.monthToLocalDate() > month }.minOrNull()?.monthToLocalDate()
                if (previousMonth == null && nextMonth == null) return null
                buildDateSwitcherButtons(previousMonth, nextMonth)
            }
            DisplayMode.YEAR -> {
                val previousYear = entries.filter { it.yearToLocalDate() < year }.maxOrNull()?.yearToLocalDate()
                val nextYear = entries.filter { it.yearToLocalDate() > year }.minOrNull()?.yearToLocalDate()
                if (previousYear == null && nextYear == null) return null
                buildDateSwitcherButtons(previousYear, nextYear)
            }
            else -> return null
        }
        return display
    }

    private fun buildDateSwitcherButtons(
        previous: LocalDate?,
        next: LocalDate?,
    ): List<Renderable> {
        return listOfNotNull(
            previous?.let {
                Renderable.optionalLink(
                    "§a[ §r§f§l<- §a]",
                    onClick = {
                        when (getDisplayMode()) {
                            DisplayMode.WEEK -> week = it
                            DisplayMode.MONTH -> month = it
                            DisplayMode.YEAR -> year = it
                            else -> date = it
                        }
                        update()
                    },
                )
            },
            next?.let {
                Renderable.optionalLink(
                    "§a[ §r§f§l-> §r§a]",
                    onClick = {
                        when (getDisplayMode()) {
                            DisplayMode.WEEK -> week = it
                            DisplayMode.MONTH -> month = it
                            DisplayMode.YEAR -> year = it
                            else -> date = it
                        }
                        update()
                    },
                )
            },
            if (next != null &&
                when (getDisplayMode()) {
                    DisplayMode.WEEK -> next < LocalDate.now().toWeekString().weekToLocalDate()
                    DisplayMode.MONTH -> next < LocalDate.now().toMonthString().monthToLocalDate()
                    DisplayMode.YEAR -> next < LocalDate.now().year.toString().yearToLocalDate()
                    else -> next < LocalDate.now()
                }
            ) {
                Renderable.optionalLink(
                    "§a[ §r§f§l->> §r§a]",
                    onClick = {
                        when (getDisplayMode()) {
                            DisplayMode.WEEK -> week = LocalDate.now()
                            DisplayMode.MONTH -> month = LocalDate.now()
                            DisplayMode.YEAR -> year = LocalDate.now()
                            else -> date = LocalDate.now()
                        }
                        update()
                    },
                )
            } else null
        )
    }
}
