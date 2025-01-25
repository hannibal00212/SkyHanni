package at.hannibal2.skyhanni.features.garden.composter

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.enums.OutsideSBFeature
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.model.TabWidget
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.WidgetUpdateEvent
import at.hannibal2.skyhanni.features.fame.ReminderUtils
import at.hannibal2.skyhanni.features.garden.GardenApi
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.addAsSingletonList
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NeuItems.getItemStack
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.renderStringsAndItems
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.fromNow
import at.hannibal2.skyhanni.utils.TimeUtils.format
import java.util.Collections
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object ComposterDisplay {

    private val config get() = GardenApi.config.composters
    private val storage get() = GardenApi.storage
    private var display = emptyList<List<Any>>()
    private var composterEmptyTime: Duration? = null

    private val bucket by lazy { "BUCKET".toInternalName().getItemStack() }
    private var tabListData by ComposterApi::tabListData

    enum class DataType(rawPattern: String, val icon: String) {
        ORGANIC_MATTER(" Organic Matter: §r(.*)", "WHEAT"),
        FUEL(" Fuel: §r(.*)", "OIL_BARREL"),
        TIME_LEFT(" Time Left: §r(.*)", "WATCH"),
        STORED_COMPOST(" Stored Compost: §r(.*)", "COMPOST");

        val displayItem by lazy { icon.toInternalName().getItemStack() }

        val pattern = rawPattern.toPattern()

        fun addToList(map: Map<DataType, String>): List<Any> {
            return map[this]?.let { listOf(displayItem, it) }.orEmpty()
        }
    }

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onTabListUpdate(event: WidgetUpdateEvent) {
        if (!event.isWidget(TabWidget.COMPOSTER)) return

        readData(event.lines)

        if (tabListData.isNotEmpty()) {
            composterEmptyTime = ComposterApi.estimateEmptyTimeFromTab()
            updateDisplay()
            sendNotify()
        }
    }

    private fun updateDisplay() {
        if (!config.displayEnabled) return
        val newDisplay = mutableListOf<List<Any>>()
        newDisplay.addAsSingletonList("§bComposter")

        newDisplay.add(DataType.TIME_LEFT.addToList(tabListData))

        val list = mutableListOf<Any>()
        list.addAll(DataType.ORGANIC_MATTER.addToList(tabListData))
        list.add(" ")
        list.addAll(DataType.FUEL.addToList(tabListData))
        newDisplay.add(list)

        newDisplay.add(DataType.STORED_COMPOST.addToList(tabListData))
        newDisplay.add(addComposterEmptyTime(composterEmptyTime))

        display = newDisplay
    }

    private fun addComposterEmptyTime(emptyTime: Duration?): List<Any> {
        return if (emptyTime != null) {
            GardenApi.storage?.composterEmptyTime = emptyTime.fromNow()
            val format = emptyTime.format()
            listOf(bucket, "§b$format")
        } else {
            listOf("§cOpen Composter Upgrades!")
        }
    }

    private fun readData(tabList: List<String>) {
        var next = false
        val newData = mutableMapOf<DataType, String>()

        for (line in tabList) {
            if (line == "§b§lComposter:") {
                next = true
                continue
            }
            if (next) {
                if (line == "") break
                for (type in DataType.entries) {
                    type.pattern.matchMatcher(line) {
                        newData[type] = group(1)
                    }
                }
            }
        }

        for (type in DataType.entries) {
            if (!newData.containsKey(type)) {
                tabListData = emptyMap()
                return
            }
        }

        tabListData = newData
    }

    private fun sendNotify() {
        if (!config.notifyLow.enabled) return
        if (ReminderUtils.isBusy()) return

        val storage = storage ?: return

        if (ComposterApi.getOrganicMatter() <= config.notifyLow.organicMatter && storage.informedAboutLowMatter.isInPast()) {
            if (config.notifyLow.title) {
                LorenzUtils.sendTitle("§cYour Organic Matter is low", 4.seconds)
            }
            ChatUtils.chat("§cYour Organic Matter is low!")
            storage.informedAboutLowMatter = 5.0.minutes.fromNow()
        }

        if (ComposterApi.getFuel() <= config.notifyLow.fuel && storage.informedAboutLowFuel.isInPast()) {
            if (config.notifyLow.title) {
                LorenzUtils.sendTitle("§cYour Fuel is low", 4.seconds)
            }
            ChatUtils.chat("§cYour Fuel is low!")
            storage.informedAboutLowFuel = 5.0.minutes.fromNow()
        }
    }

    @HandleEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        @Suppress("InSkyBlockEarlyReturn")
        if (!LorenzUtils.inSkyBlock && !OutsideSBFeature.COMPOSTER_TIME.isSelected()) return

        if (GardenApi.inGarden() && config.displayEnabled) {
            config.displayPos.renderStringsAndItems(display, posLabel = "Composter Display")
        }

        checkWarningsAndOutsideGarden()
    }

    private fun checkWarningsAndOutsideGarden() {
        val format = GardenApi.storage?.let {
            if (!it.composterEmptyTime.isFarPast()) {
                val duration = it.composterEmptyTime.timeUntil()
                if (duration > 0.0.seconds) {
                    if (duration < 20.0.minutes) {
                        warn("Your composter in the garden is almost empty!")
                    }
                    duration.format(maxUnits = 3)
                } else {
                    warn("Your composter is empty!")
                    "§cComposter is empty!"
                }
            } else "?"
        } ?: "§cJoin SkyBlock to show composter timer."

        val inSB = LorenzUtils.inSkyBlock && config.displayOutsideGarden
        val outsideSB = !LorenzUtils.inSkyBlock && OutsideSBFeature.COMPOSTER_TIME.isSelected()
        if (!GardenApi.inGarden() && (inSB || outsideSB)) {
            val list = Collections.singletonList(listOf(bucket, "§b$format"))
            config.outsideGardenPos.renderStringsAndItems(list, posLabel = "Composter Outside Garden")
        }
    }

    private fun warn(warningMessage: String) {
        if (!config.warnAlmostClose) return
        val storage = GardenApi.storage ?: return

        if (ReminderUtils.isBusy()) return

        if (storage.lastComposterEmptyWarningTime.passedSince() >= 2.0.minutes) return
        storage.lastComposterEmptyWarningTime = SimpleTimeMark.now()
        if (IslandType.GARDEN.isInIsland()) {
            ChatUtils.chat(warningMessage)
        } else {
            ChatUtils.clickToActionOrDisable(
                warningMessage,
                config::warnAlmostClose,
                actionName = "warp to the Garden",
                action = { HypixelCommands.warp("garden") },
            )
        }
        LorenzUtils.sendTitle("§eComposter Warning!", 3.seconds)
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "garden.composterDisplayEnabled", "garden.composters.displayEnabled")
        event.move(3, "garden.composterDisplayOutsideGarden", "garden.composters.displayOutsideGarden")
        event.move(3, "garden.composterWarnAlmostClose", "garden.composters.warnAlmostClose")
        event.move(3, "garden.composterDisplayPos", "garden.composters.displayPos")
        event.move(3, "garden.composterOutsideGardenPos", "garden.composters.outsideGardenPos")
        event.move(3, "garden.composterNotifyLowEnabled", "garden.composters.notifyLow.enabled")
        event.move(3, "garden.composterNotifyLowEnabled", "garden.composters.notifyLow.enabled")
        event.move(3, "garden.composterNotifyLowTitle", "garden.composters.notifyLow.title")
        event.move(3, "garden.composterNotifyLowOrganicMatter", "garden.composters.notifyLow.organicMatter")
        event.move(3, "garden.composterNotifyLowFuel", "garden.composters.notifyLow.fuel")
    }
}
