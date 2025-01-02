package at.hannibal2.skyhanni.features.garden.composter

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.enums.OutsideSbFeature
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.model.TabWidget
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.WidgetUpdateEvent
import at.hannibal2.skyhanni.features.fame.ReminderUtils
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NEUItems.getItemStack
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderable
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.fromNow
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object ComposterDisplay {

    private val config get() = GardenAPI.config.composters
    private val storage get() = GardenAPI.storage
    private var display: Renderable? = null
    private var composterEmptyTime: Duration? = null

    private val bucket by lazy { "BUCKET".toInternalName().getItemStack() }
    private var tabListData by ComposterAPI::tabListData

    enum class DataType(rawPattern: String, val icon: String) {
        ORGANIC_MATTER(" Organic Matter: §r(.*)", "WHEAT"),
        FUEL(" Fuel: §r(.*)", "OIL_BARREL"),
        TIME_LEFT(" Time Left: §r(.*)", "WATCH"),
        STORED_COMPOST(" Stored Compost: §r(.*)", "COMPOST");

        val displayItem by lazy { icon.toInternalName().getItemStack() }

        val pattern by lazy { rawPattern.toPattern() }

        fun renderableLine(map: Map<DataType, String>): Renderable? =
            map[this]?.let { labeledItemStack(it, displayItem) }
    }

    @HandleEvent
    fun onTabListUpdate(event: WidgetUpdateEvent) {
        if (!GardenAPI.inGarden()) return
        if (!event.isWidget(TabWidget.COMPOSTER)) return

        readData(event.lines)

        if (tabListData.isNotEmpty()) {
            composterEmptyTime = ComposterAPI.estimateEmptyTimeFromTab()
            display = updateDisplay()
            sendNotify()
        }
    }

    private fun updateDisplay(): Renderable? {
        if (!config.displayEnabled) return null

        val lines = mutableListOf<Renderable>()
        lines.add(Renderable.string("§bComposter"))

        DataType.TIME_LEFT.renderableLine(tabListData)?.let { lines.add(it) }

        val ingredientLine = Renderable.horizontalContainer(
            listOfNotNull(
                DataType.ORGANIC_MATTER.renderableLine(tabListData),
                Renderable.string(" "),
                DataType.FUEL.renderableLine(tabListData),
            )
        )

        lines.add(ingredientLine)

        DataType.STORED_COMPOST.renderableLine(tabListData)?.let { lines.add(it) }
        lines.add(addComposterEmptyTime(composterEmptyTime))

        return Renderable.verticalContainer(lines, spacing = 1)
    }

    private fun addComposterEmptyTime(emptyTime: Duration?): Renderable {
        return if (emptyTime != null) {
            GardenAPI.storage?.composterEmptyTime = emptyTime.fromNow()
            val format = emptyTime.format()
            labeledItemStack("§b$format", bucket)
        } else {
            Renderable.string("§cOpen Composter Upgrades!")
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

        if (ComposterAPI.getOrganicMatter() <= config.notifyLow.organicMatter && storage.informedAboutLowMatter.isInPast()) {
            if (config.notifyLow.title) {
                LorenzUtils.sendTitle("§cYour Organic Matter is low", 4.seconds)
            }
            ChatUtils.chat("§cYour Organic Matter is low!")
            storage.informedAboutLowMatter = 5.0.minutes.fromNow()
        }

        if (ComposterAPI.getFuel() <= config.notifyLow.fuel && storage.informedAboutLowFuel.isInPast()) {
            if (config.notifyLow.title) {
                LorenzUtils.sendTitle("§cYour Fuel is low", 4.seconds)
            }
            ChatUtils.chat("§cYour Fuel is low!")
            storage.informedAboutLowFuel = 5.0.minutes.fromNow()
        }
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!LorenzUtils.inSkyBlock && !OutsideSbFeature.COMPOSTER_TIME.isSelected()) return

        if (GardenAPI.inGarden() && config.displayEnabled) {
            config.displayPos.renderRenderable(display, posLabel = "Composter Display")
        }

        checkWarningsAndOutsideGarden()
    }

    private fun checkWarningsAndOutsideGarden() {
        val format = GardenAPI.storage?.let {
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

        val inSb = LorenzUtils.inSkyBlock && config.displayOutsideGarden
        val outsideSb = !LorenzUtils.inSkyBlock && OutsideSbFeature.COMPOSTER_TIME.isSelected()
        if (!GardenAPI.inGarden() && (inSb || outsideSb)) {
            val outsideGardenDisplay = labeledItemStack("§b$format", bucket)
            config.outsideGardenPos.renderRenderable(outsideGardenDisplay, posLabel = "Composter Outside Garden")
        }
    }

    private fun labeledItemStack(label: String, itemStack: ItemStack): Renderable {
        return Renderable.horizontalContainer(
            listOf(Renderable.itemStack(itemStack), Renderable.string(label))
        )
    }

    private fun warn(warningMessage: String) {
        if (!config.warnAlmostClose) return
        val storage = GardenAPI.storage ?: return

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
