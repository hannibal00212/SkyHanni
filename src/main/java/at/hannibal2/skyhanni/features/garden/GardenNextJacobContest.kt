package at.hannibal2.skyhanni.features.garden

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigFileType
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.enums.OutsideSbFeature
import at.hannibal2.skyhanni.config.features.garden.NextJacobContestConfig.ShareContestsEntry
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.TabListUpdateEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI.addCropIcon
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.APIUtils
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ConfigUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.renderSingleLineWithItems
import at.hannibal2.skyhanni.utils.RenderUtils.renderStrings
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.asTimeMark
import at.hannibal2.skyhanni.utils.SkyBlockTime
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.TabListData
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.json.toJsonArray
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import com.google.gson.Gson
import com.google.gson.JsonPrimitive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.Display
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.UIManager
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object GardenNextJacobContest {

    private val dispatcher = Dispatchers.IO
    private var display = emptyList<Any>()
    private var simpleDisplay = emptyList<String>()
    var contests = mutableMapOf<SimpleTimeMark, FarmingContest>()
    private var inCalendar = false

    private val patternGroup = RepoPattern.group("garden.nextcontest")
    val dayPattern by patternGroup.pattern(
        "day",
        "§aDay (?<day>.*)"
    )

    /**
     * REGEX-TEST: Early Spring, Year 351
     * REGEX-TEST: Late Summer, Year 351
     * REGEX-TEST: Autumn, Year 351
     */

    val monthPattern by patternGroup.pattern(
        "month",
        "(?<month>(?:\\w+ )?(?:Summer|Spring|Winter|Autumn)), Year (?<year>\\d+)"
    )
    private val cropPattern by patternGroup.pattern(
        "crop",
        "§(e○|6☘) §7(?<crop>.*)"
    )

    private const val CLOSE_TO_NEW_YEAR_TEXT = "§7Close to new SB year!"
    private const val MAX_CONTESTS_PER_YEAR = 124

    private val contestDuration = 20.minutes

    private var lastWarningTime = SimpleTimeMark.farPast()
    private var loadedContestsYear = -1
    private var nextContestsAvailableAt = -1L

    var lastFetchAttempted = 0L
    var isFetchingContests = false
    var fetchedFromElite = false
    private var isSendingContests = false

    @SubscribeEvent
    fun onDebugDataCollect(event: DebugDataCollectEvent) {
        event.title("Garden Next Jacob Contest")

        if (!GardenAPI.inGarden()) {
            event.addIrrelevant("not in garden")
            return
        }

        event.addIrrelevant {
            add("Current time: ${SimpleTimeMark.now()}")
            add("")

            val display = display.filterIsInstance<String>().joinToString("")
            add("Display: '$display'")
            add("")

            add("Contests:")
            for (contest in contests) {
                val time = contest.key
                val passedSince = time.passedSince()
                val timeUntil = time.timeUntil()
                val farmingContest = contest.value
                val crops = farmingContest.crops
                val recently = 0.seconds..2.hours
                if (passedSince in recently || timeUntil in recently) {
                    add(" Time: $time")
                    if (passedSince.isPositive()) {
                        add("  Passed since: $passedSince")
                    }
                    if (timeUntil.isPositive()) {
                        add("  Time until: $timeUntil")
                    }
                    add("  Crops: $crops")
                }
            }
        }
    }

    @SubscribeEvent
    fun onTabListUpdate(event: TabListUpdateEvent) {
        var next = false
        val newList = mutableListOf<String>()
        var counter = 0
        for (line in event.tabList) {
            if (line == "§e§lJacob's Contest:") {
                newList.add(line)
                next = true
                continue
            }
            if (next) {
                if (line == "") break
                newList.add(line)
                counter++
                if (counter == 4) break
            }
        }

        if (isCloseToNewYear()) {
            newList.add(CLOSE_TO_NEW_YEAR_TEXT)
        } else {
            newList.add("§cOpen calendar for")
            newList.add("§cmore exact data!")
        }

        simpleDisplay = newList
    }

    private fun isCloseToNewYear(): Boolean {
        val now = SkyBlockTime.now()
        val newYear = SkyBlockTime(year = now.year)
        val nextYear = SkyBlockTime(year = now.year + 1)
        val diffA = now.asTimeMark() - newYear.asTimeMark()
        val diffB = nextYear.asTimeMark() - now.asTimeMark()

        return diffA < 30.minutes || diffB < 30.minutes
    }

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return

        if (inCalendar) return
        update()
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (inCalendar) {
            inCalendar = false
            update()
        }
    }

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!config.display) return
        monthPattern.matchMatcher(event.inventoryName) {
            inCalendar = true
            val month = LorenzUtils.getSBMonthByName(group("month"))
            val year = group("year").toInt()

            readCalendar(event.inventoryItems.values, year, month)
        }
    }

    private fun readCalendar(items: Collection<ItemStack>, year: Int, month: Int) {
        if (contests.isNotEmpty() && loadedContestsYear != year) {
            val endTime = contests.values.first().endTime
            val lastYear = endTime.toSkyBlockTime().year
            if (year != lastYear) {
                contests.clear()
            }
            // Contests are available now, make sure system knows this
            if (nextContestsAvailableAt > System.currentTimeMillis()) {
                nextContestsAvailableAt = System.currentTimeMillis() - 1
                fetchContestsIfAble()
            }
            if (nextContestsAvailableAt == -1L) {
                nextContestsAvailableAt = System.currentTimeMillis() - 1
                fetchContestsIfAble()
            }
        }

        // Skip if contests are already loaded for this year
        if (contests.size == MAX_CONTESTS_PER_YEAR) return

        // Manually loading contests
        for (item in items) {
            val lore = item.getLore()
            if (!lore.any { it.contains("§6§eJacob's Farming Contest") }) continue

            val day = dayPattern.matchMatcher(item.name) { group("day").toInt() } ?: continue

            val startTime = SkyBlockTime(year, month, day).asTimeMark()

            val crops = mutableListOf<CropType>()
            for (line in lore) {
                cropPattern.matchMatcher(line) { crops.add(CropType.getByName(group("crop"))) }
            }

            contests[startTime] = FarmingContest(startTime + contestDuration, crops)
        }

        // If contests were just fully saved
        if (contests.size == MAX_CONTESTS_PER_YEAR) {
            nextContestsAvailableAt = SkyBlockTime(SkyBlockTime.now().year + 1, 1, 2).toMillis()

            if (isSendEnabled()) {
                if (!askToSendContests()) {
                    sendContests()
                } else {
                    ChatUtils.clickableChat(
                        "§2Click here to submit this year's farming contests. Thank you for helping everyone out!",
                        onClick = { shareContests() },
                        "§eClick to submit!",
                        oneTimeClick = true
                    )
                }
            }
        }
        update()
        saveConfig()
    }

    private fun saveConfig() {
        val map = SkyHanniMod.jacobContestsData.contestTimes
        map.clear()

        val currentYear = SkyBlockTime.now().year
        for (contest in contests.values) {
            val contestYear = (contest.endTime.toSkyBlockTime()).year
            // Ensure all stored contests are really from the current year
            if (contestYear != currentYear) continue

            map[contest.endTime] = contest.crops
        }
        SkyHanniMod.configManager.saveConfig(ConfigFileType.JACOB_CONTESTS, "Save contests")
    }

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        val savedContests = SkyHanniMod.jacobContestsData.contestTimes
        val year = savedContests.firstNotNullOfOrNull {
            val endTime = it.key

            endTime.toSkyBlockTime().year
        }

        // Clear contests if from previous year
        if (year != SkyBlockTime.now().year) {
            savedContests.clear()
        } else {
            for ((time, crops) in savedContests) {
                contests[time] = FarmingContest(time, crops)
            }
        }
    }

    private fun shareContests() {
        if (contests.size == MAX_CONTESTS_PER_YEAR) {
            sendContests()
        }
        if (!SkyHanniMod.feature.storage.contestSendingAsked && config.shareAutomatically == ShareContestsEntry.ASK) {
            ChatUtils.clickableChat(
                "§2Click here to automatically share future contests!",
                onClick = {
                    config.shareAutomatically = ShareContestsEntry.AUTO
                    SkyHanniMod.feature.storage.contestSendingAsked = true
                    ChatUtils.chat("§2Enabled automatic sharing of future contests!")
                },
                "§eClick to enable autosharing!",
                oneTimeClick = true
            )
        }
    }

    class FarmingContest(val endTime: SimpleTimeMark, val crops: List<CropType>)

    private fun update() {
        nextContestCrops.clear()

        if (nextContestsAvailableAt == -1L) {
            val currentDate = SkyBlockTime.now()
            if (currentDate.month <= 1 && currentDate.day <= 1) {
                nextContestsAvailableAt = SkyBlockTime(SkyBlockTime.now().year + 1, 1, 1).toMillis()
            }
        }

        display = if (isFetchingContests) {
            listOf("§cFetching this years jacob contests...")
        } else {
            fetchContestsIfAble() // Will only run when needed/enabled
            drawDisplay()
        }
    }

    private fun drawDisplay(): List<Any> {
        val list = mutableListOf<Any>()

        if (inCalendar) {
            val size = contests.size
            val percentage = size.toDouble() / MAX_CONTESTS_PER_YEAR
            val formatted = LorenzUtils.formatPercentage(percentage)
            list.add("§eDetected $formatted of farming contests this year")

            return list
        }

        if (contests.isEmpty()) {
            if (isCloseToNewYear()) {
                list.add(CLOSE_TO_NEW_YEAR_TEXT)
            } else {
                list.add("§cOpen calendar to read Jacob contest times!")
            }
            return list
        }

        val nextContest =
            contests.filter { !it.value.endTime.isInPast() }.toSortedMap()
                .firstNotNullOfOrNull { it.value }
        // Show next contest
        if (nextContest != null) return drawNextContest(nextContest, list)

        if (isCloseToNewYear()) {
            list.add(CLOSE_TO_NEW_YEAR_TEXT)
        } else {
            list.add("§cOpen calendar to read Jacob contest times!")
        }

        fetchedFromElite = false
        contests.clear()

        return list
    }

    private fun drawNextContest(
        nextContest: FarmingContest,
        list: MutableList<Any>,
    ): MutableList<Any> {
        var duration = nextContest.endTime.timeUntil()
        if (duration > 4.days) {
            list.add(CLOSE_TO_NEW_YEAR_TEXT)
            return list
        }

        val boostedCrop = calculateBoostedCrop(nextContest)

        val activeContest = duration < contestDuration
        if (activeContest) {
            list.add("§aActive: ")
        } else {
            list.add("§eNext: ")
            duration -= contestDuration
        }
        for (crop in nextContest.crops) {
            list.addCropIcon(crop, 1.0, highlight = (crop == boostedCrop))
            nextContestCrops.add(crop)
        }
        if (!activeContest) {
            warn(duration, nextContest.crops, boostedCrop)
        }
        val format = duration.format()
        list.add("§7(§b$format§7)")

        return list
    }

    private fun calculateBoostedCrop(nextContest: FarmingContest): CropType? {
        for (line in TabListData.getTabList()) {
            val lineStripped = line.removeColor().trim()
            if (!lineStripped.startsWith("☘ ")) continue
            for (crop in nextContest.crops) {
                if (line.removeColor().trim().startsWith("☘ ${crop.cropName}")) {
                    return crop
                }
            }
        }

        return null
    }

    private fun warn(duration: Duration, crops: List<CropType>, boostedCrop: CropType?) {
        if (!config.warn) return
        if (config.warnTime.seconds <= duration) return
        if (!warnForCrop()) return

        // Check that it only gets called once for the current event
        if (lastWarningTime.passedSince() < config.warnTime.seconds) return

        lastWarningTime = SimpleTimeMark.now()
        val cropText = crops.joinToString("§7, ") { (if (it == boostedCrop) "§6" else "§a") + it.cropName }
        ChatUtils.chat("Next farming contest: $cropText")
        LorenzUtils.sendTitle("§eFarming Contest!", 5.seconds)
        SoundUtils.playBeepSound()

        val cropTextNoColor = crops.joinToString(", ") {
            if (it == boostedCrop) "<b>${it.cropName}</b>" else it.cropName
        }
        if (config.warnPopup && !Display.isActive()) {
            SkyHanniMod.coroutineScope.launch {
                openPopupWindow(
                    "<html>Farming Contest soon!<br />" +
                        "Crops: $cropTextNoColor</html>"
                )
            }
        }
    }

    /**
     * Taken and modified from Skytils
     */
    private fun openPopupWindow(message: String) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        } catch (e: java.lang.Exception) {
            ErrorManager.logErrorWithData(
                e, "Failed to open a popup window",
                "message" to message
            )
        }

        val frame = JFrame()
        frame.isUndecorated = true
        frame.isAlwaysOnTop = true
        frame.setLocationRelativeTo(null)
        frame.isVisible = true

        val buttons = mutableListOf<JButton>()
        val close = JButton("Ok")
        close.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(event: MouseEvent) {
                frame.isVisible = false
            }
        })
        buttons.add(close)

        val allOptions = buttons.toTypedArray()
        JOptionPane.showOptionDialog(
            frame,
            message,
            "SkyHanni Jacob Contest Notification",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            allOptions,
            allOptions[0]
        )
    }

    private fun warnForCrop(): Boolean = nextContestCrops.any { it in config.warnFor }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return

        if (display.isEmpty()) {
            config.pos.renderStrings(simpleDisplay, posLabel = "Next Jacob Contest")
        } else {
            config.pos.renderSingleLineWithItems(display, posLabel = "Next Jacob Contest")
        }
    }

    @SubscribeEvent
    fun onBackgroundDraw(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        if (!config.display) return
        if (!inCalendar) return

        if (display.isNotEmpty()) {
            SkyHanniMod.feature.misc.inventoryLoadPos.renderSingleLineWithItems(
                display,
                posLabel = "Load SkyBlock Calendar"
            )
        }
    }

    private fun isEnabled() =
        config.display && (
            (LorenzUtils.inSkyBlock && (GardenAPI.inGarden() || config.showOutsideGarden)) ||
                (OutsideSbFeature.NEXT_JACOB_CONTEST.isSelected() && !LorenzUtils.inSkyBlock)
            )

    private fun isFetchEnabled() = isEnabled() && config.fetchAutomatically
    private fun isSendEnabled() =
        isFetchEnabled() && config.shareAutomatically != ShareContestsEntry.DISABLED

    private fun askToSendContests() =
        config.shareAutomatically == ShareContestsEntry.ASK // (Only call if isSendEnabled())

    private fun fetchContestsIfAble() {
        if (isFetchingContests || contests.size == MAX_CONTESTS_PER_YEAR || !isFetchEnabled()) return
        // Allows retries every 10 minutes when it's after 1 day into the new year
        val currentMills = System.currentTimeMillis()
        if (lastFetchAttempted + 600_000 > currentMills || currentMills < nextContestsAvailableAt) return

        isFetchingContests = true

        SkyHanniMod.coroutineScope.launch {
            fetchUpcomingContests()
            lastFetchAttempted = System.currentTimeMillis()
            isFetchingContests = false
        }
    }

    suspend fun fetchUpcomingContests() {
        try {
            val url = "https://api.elitebot.dev/contests/at/now"
            val result = withContext(dispatcher) { APIUtils.getJSONResponse(url) }.asJsonObject

            val newContests = mutableMapOf<SimpleTimeMark, FarmingContest>()

            val complete = result["complete"].asBoolean
            if (complete) {
                for (entry in result["contests"].asJsonObject.entrySet()) {
                    var timestamp = entry.key.toLongOrNull() ?: continue
                    val timeMark = (timestamp * 1000).asTimeMark()
                    timestamp *= 1_000 // Seconds to milliseconds

                    val crops = entry.value.asJsonArray.map {
                        CropType.getByName(it.asString)
                    }

                    if (crops.size != 3) continue

                    newContests[timeMark + contestDuration] = FarmingContest(timeMark + contestDuration, crops)
                }
            } else {
                ChatUtils.chat(
                    "This year's contests aren't available to fetch automatically yet, " +
                        "please load them from your calendar or wait 10 minutes."
                )
                ChatUtils.clickableChat(
                    "Click here to open your calendar!",
                    onClick = { HypixelCommands.calendar() },
                    "§eClick to run /calendar!"
                )
            }

            if (newContests.count() == MAX_CONTESTS_PER_YEAR) {
                ChatUtils.chat("Successfully loaded this year's contests from elitebot.dev automatically!")

                contests = newContests
                fetchedFromElite = true
                nextContestsAvailableAt = SkyBlockTime(SkyBlockTime.now().year + 1, 1, 2).toMillis()
                loadedContestsYear = SkyBlockTime.now().year

                saveConfig()
            }
        } catch (e: Exception) {
            ErrorManager.logErrorWithData(
                e,
                "Failed to fetch upcoming contests. Please report this error if it continues to occur"
            )

        }
    }

    private fun sendContests() {
        if (isSendingContests || contests.size != MAX_CONTESTS_PER_YEAR || isCloseToNewYear()) return

        isSendingContests = true

        SkyHanniMod.coroutineScope.launch {
            submitContestsToElite()
            isSendingContests = false
        }
    }

    private suspend fun submitContestsToElite() = try {
        val formatted = mutableMapOf<Long, List<String>>()

        for ((endTime, contest) in contests) {
            formatted[endTime.toMillis() / 1000] = contest.crops.map {
                it.cropName
            }
        }

        val url = "https://api.elitebot.dev/contests/at/now"
        val body = Gson().toJson(formatted)

        val result = withContext(dispatcher) { APIUtils.postJSONIsSuccessful(url, body) }

        if (result) {
            ChatUtils.chat("Successfully submitted this years upcoming contests, thank you for helping everyone out!")
        } else {
            ErrorManager.logErrorStateWithData(
                "Something went wrong submitting upcoming contests!",
                "submitContestsToElite not successful"
            )
        }
    } catch (e: Exception) {
        ErrorManager.logErrorWithData(
            e, "Failed to submit upcoming contests. Please report this error if it continues to occur.",
            "contests" to contests
        )
        null
    }

    private val config get() = GardenAPI.config.nextJacobContests
    private val nextContestCrops = mutableListOf<CropType>()

    fun isNextCrop(cropName: CropType) = nextContestCrops.contains(cropName) && config.otherGuis

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "garden.nextJacobContestDisplay", "garden.nextJacobContests.display")
        event.move(3, "garden.nextJacobContestEverywhere", "garden.nextJacobContests.everywhere")
        event.move(3, "garden.nextJacobContestOtherGuis", "garden.nextJacobContests.otherGuis")
        event.move(3, "garden.nextJacobContestsFetchAutomatically", "garden.nextJacobContests.fetchAutomatically")
        event.move(3, "garden.nextJacobContestsShareAutomatically", "garden.nextJacobContests.shareAutomatically")
        event.move(3, "garden.nextJacobContestWarn", "garden.nextJacobContests.warn")
        event.move(3, "garden.nextJacobContestWarnTime", "garden.nextJacobContests.warnTime")
        event.move(3, "garden.nextJacobContestWarnPopup", "garden.nextJacobContests.warnPopup")
        event.move(3, "garden.nextJacobContestPos", "garden.nextJacobContests.pos")

        event.transform(15, "garden.nextJacobContests.shareAutomatically") { element ->
            ConfigUtils.migrateIntToEnum(element, ShareContestsEntry::class.java)
        }
        event.move(18, "garden.nextJacobContests.everywhere", "garden.nextJacobContests.showOutsideGarden")
        event.move(33, "garden.jacobContextTimesPos", "garden.jacobContestTimesPosition")
        event.move(33, "garden.jacobContextTimes", "garden.jacobContestTimes")
        event.move(33, "garden.everywhere", "garden.outsideGarden")
        event.transform(33, "misc.showOutsideSB") { element ->
            element.asJsonArray.map { setting ->
                if (setting.asString == "NEXT_JACOB_CONTEXT") JsonPrimitive("NEXT_JACOB_CONTEST") else setting
            }.toJsonArray()
        }
    }
}
