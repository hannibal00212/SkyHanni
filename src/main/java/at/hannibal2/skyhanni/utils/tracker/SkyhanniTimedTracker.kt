package at.hannibal2.skyhanni.utils.tracker

import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.utils.CollectionUtils.addString
import at.hannibal2.skyhanni.utils.KeyboardManager
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.TimeUtils.monthFormatter
import at.hannibal2.skyhanni.utils.TimeUtils.weekFormatter
import at.hannibal2.skyhanni.utils.TimeUtils.weekTextFormatter
import at.hannibal2.skyhanni.utils.TimeUtils.yearFormatter
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.Searchable
import at.hannibal2.skyhanni.utils.renderables.buildSearchBox
import at.hannibal2.skyhanni.utils.renderables.toRenderable
import java.time.LocalDate

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

    private var date = LocalDate.now()
    private var week = date.format(weekFormatter).weekToLocalDate()
    private var month = date.format(monthFormatter).monthToLocalDate()
    private var year = date.format(yearFormatter).yearToLocalDate()

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

    fun changeDate(oldDate: LocalDate, newDate: LocalDate) {
        if (date == oldDate) {
            date = newDate
            update()
        }
        if (week == oldDate.format(weekFormatter).weekToLocalDate()) {
            week = newDate.format(weekFormatter).weekToLocalDate()
            update()
        }
        if (month == oldDate.format(monthFormatter).monthToLocalDate()) {
            month = newDate.format(monthFormatter).monthToLocalDate()
            update()
        }
        if (year == oldDate.format(yearFormatter).yearToLocalDate()) {
            year = newDate.format(yearFormatter).yearToLocalDate()
            update()
        }
    }


    fun buildDate() = Renderable.verticalContainer(
        buildList {
            val displayText: String = when (displayMode) {
                DisplayMode.DAY -> {
                    val dateString = if (date == LocalDate.now()) "Today" else date.toString()
                    "§7Date: §a$dateString"
                }
                DisplayMode.WEEK -> {
                    val dateString =
                        if (week.format(weekFormatter) == LocalDate.now().format(weekFormatter))
                            "This Week" else week.format(weekTextFormatter)
                    "§7Week: §a$dateString"
                }

                DisplayMode.MONTH -> {
                    val dateString = if (month.format(monthFormatter) == LocalDate.now().format(monthFormatter))
                        "This Month" else month.format(monthFormatter)
                    "§7Month: §a$dateString"
                }

                DisplayMode.YEAR -> {
                    val dateString = if (year.year == LocalDate.now().year)
                        "This Year" else year.format(yearFormatter)
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

    private fun String.weekToLocalDate(): LocalDate = LocalDate.parse(this, weekFormatter)

    private fun String.monthToLocalDate(): LocalDate = LocalDate.parse(this, monthFormatter)

    private fun String.yearToLocalDate(): LocalDate = LocalDate.parse(this, yearFormatter)

    private fun buildDateSwitcherView(): List<Renderable>? {
        val statsStorage = ProfileStorageData.profileSpecific?.getData() ?: return null
        val entries = statsStorage.getEntries(getDisplayMode())?.keys ?: return null

        var previous: LocalDate? = null
        var next: LocalDate? = null

        when (getDisplayMode()) {
            DisplayMode.DAY -> {
                previous = entries.filter { it.dayToLocalDate() < date }.maxOrNull()?.dayToLocalDate()
                next = entries.filter { it.dayToLocalDate() > date }.minOrNull()?.dayToLocalDate()
            }
            DisplayMode.WEEK -> {
                previous = entries.filter { it.weekToLocalDate() < week }.maxOrNull()?.weekToLocalDate()
                next = entries.filter { it.weekToLocalDate() > week }.minOrNull()?.weekToLocalDate()
            }
            DisplayMode.MONTH -> {
                previous = entries.filter { it.monthToLocalDate() < month }.maxOrNull()?.monthToLocalDate()
                next = entries.filter { it.monthToLocalDate() > month }.minOrNull()?.monthToLocalDate()
            }
            DisplayMode.YEAR -> {
                previous = entries.filter { it.yearToLocalDate() < year }.maxOrNull()?.yearToLocalDate()
                next = entries.filter { it.yearToLocalDate() > year }.minOrNull()?.yearToLocalDate()
            }
            else -> {}
        }
        if (previous == null && next == null) return null
        val display = buildDateSwitcherButtons(previous, next)
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
                    DisplayMode.WEEK -> next < LocalDate.now().format(weekFormatter).weekToLocalDate()
                    DisplayMode.MONTH -> next < LocalDate.now().format(monthFormatter).monthToLocalDate()
                    DisplayMode.YEAR -> next < LocalDate.now().format(yearFormatter).yearToLocalDate()
                    else -> next < LocalDate.now()
                }
            ) {
                Renderable.optionalLink(
                    "§a[ §r§f§l->> §r§a]",
                    onClick = {
                        when (getDisplayMode()) {
                            DisplayMode.WEEK -> week = LocalDate.now().format(weekFormatter).weekToLocalDate()
                            DisplayMode.MONTH -> month = LocalDate.now().format(monthFormatter).monthToLocalDate()
                            DisplayMode.YEAR -> year = LocalDate.now().format(yearFormatter).yearToLocalDate()
                            else -> date = LocalDate.now()
                        }
                        update()
                    },
                )
            } else null
        )
    }
}
