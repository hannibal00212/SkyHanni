package at.hannibal2.skyhanni.features.nether.reputationhelper

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.features.crimsonisle.ReputationHelperConfig.ShowLocationEntry
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.data.jsonobjects.repo.CrimsonIsleReputationJson
import at.hannibal2.skyhanni.data.model.TabWidget
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.SackChangeEvent
import at.hannibal2.skyhanni.events.WidgetUpdateEvent
import at.hannibal2.skyhanni.features.nether.reputationhelper.dailyquest.DailyQuestHelper
import at.hannibal2.skyhanni.features.nether.reputationhelper.dailyquest.QuestLoader
import at.hannibal2.skyhanni.features.nether.reputationhelper.kuudra.DailyKuudraBossHelper
import at.hannibal2.skyhanni.features.nether.reputationhelper.miniboss.DailyMiniBossHelper
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.addString
import at.hannibal2.skyhanni.utils.ConditionalUtils.afterChange
import at.hannibal2.skyhanni.utils.ConfigUtils
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NEUItems
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class CrimsonIsleReputationHelper(skyHanniMod: SkyHanniMod) {

    private val config get() = SkyHanniMod.feature.crimsonIsle.reputationHelper

    val questHelper = DailyQuestHelper(this)
    val miniBossHelper = DailyMiniBossHelper(this)
    val kuudraBossHelper = DailyKuudraBossHelper(this)

    var factionType: FactionType? = null

    private var display = emptyList<Renderable>()
    private var dirty = true
    var tabListQuestsMissing = false

    /**
     * REGEX-TEST:  §r§c✖ Rescue Mission
     * REGEX-TEST:  §r§a✔ Digested Mushrooms §r§8x20
     * REGEX-TEST:  §r§c✖ Slugfish §r§8x1
     */
    val tabListQuestPattern by RepoPattern.pattern(
        "crimson.reputationhelper.tablist.quest",
        " (?:§.*)?(?<status>[✖✔]) (?<name>.+?)(?: (?:§.)*?x(?<amount>\\d+))?",
    )

    init {
        skyHanniMod.loadModule(questHelper)
        skyHanniMod.loadModule(miniBossHelper)
        skyHanniMod.loadModule(kuudraBossHelper)
    }

    @HandleEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<CrimsonIsleReputationJson>("CrimsonIsleReputation")
        miniBossHelper.onRepoReload(data.MINIBOSS)
        kuudraBossHelper.onRepoReload(data.KUUDRA)

        QuestLoader.quests.clear()
        QuestLoader.loadQuests(data.FISHING, "FISHING")
        QuestLoader.loadQuests(data.RESCUE, "RESCUE")
        QuestLoader.loadQuests(data.FETCH, "FETCH")
        QuestLoader.loadQuests(data.DOJO, "DOJO")

        update()
    }

    @HandleEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        ProfileStorageData.profileSpecific?.crimsonIsle?.let {
            miniBossHelper.loadData(it)
            kuudraBossHelper.loadData(it)
            questHelper.load(it)
        }

        config.hideComplete.afterChange {
            updateRender()
        }
    }

    @HandleEvent
    fun onSackChange(event: SackChangeEvent) {
        dirty = true
    }

    @HandleEvent
    fun onWidgetUpdate(event: WidgetUpdateEvent) {
        if (!event.isWidget(TabWidget.REPUTATION)) return

        TabWidget.REPUTATION.matchMatcherFirstLine {
            factionType = FactionType.fromName(group("faction"))
        }
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!IslandType.CRIMSON_ISLE.isInIsland()) return
        if (!config.enabled.get()) return
        if (!dirty && display.isEmpty()) {
            dirty = true
        }
        if (dirty) {
            dirty = false
            updateRender()
        }
    }

    private fun updateRender() {
        display = buildList {
            addString("§e§lReputation Helper")
            if (factionType == null) {
                addString("§cFaction not found!")
                return
            }

            if (tabListQuestsMissing) {
                addString("§cFaction Quests Widget not found!")
                addString("§7Open §e/tab §7and enable it!")
            } else {
                questHelper.run {
                    addQuests()
                }
                miniBossHelper.run {
                    addDailyMiniBoss()
                }
                kuudraBossHelper.run {
                    addKuudraBoss()
                }
            }
        }
    }

    @HandleEvent(priority = HandleEvent.LOWEST)
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!config.enabled.get()) return
        if (!IslandType.CRIMSON_ISLE.isInIsland()) return

        if (config.useHotkey && !isHotkeyHeld()) {
            return
        }

        config.position.renderRenderables(
            display,
            posLabel = "Crimson Isle Reputation Helper",
        )
    }

    fun isHotkeyHeld(): Boolean {
        val isAllowedGui = Minecraft.getMinecraft().currentScreen.let {
            it == null || it is GuiInventory
        }
        if (!isAllowedGui) return false
        if (NEUItems.neuHasFocus()) return false

        return config.hotkey.isKeyHeld()
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(2, "misc.crimsonIsleReputationHelper", "crimsonIsle.reputationHelper.enabled")
        event.move(2, "misc.reputationHelperUseHotkey", "crimsonIsle.reputationHelper.useHotkey")
        event.move(2, "misc.reputationHelperHotkey", "crimsonIsle.reputationHelper.hotkey")
        event.move(2, "misc.crimsonIsleReputationHelperPos", "crimsonIsle.reputationHelper.position")
        event.move(2, "misc.crimsonIsleReputationShowLocation", "crimsonIsle.reputationHelper.showLocation")

        event.transform(15, "crimsonIsle.reputationHelper.showLocation") { element ->
            ConfigUtils.migrateIntToEnum(element, ShowLocationEntry::class.java)
        }
    }

    fun update() {
        ProfileStorageData.profileSpecific?.crimsonIsle?.let {
            questHelper.saveConfig(it)
            miniBossHelper.saveConfig(it)
            kuudraBossHelper.saveConfig(it)
        }

        dirty = true
    }

    fun reset() {
        ChatUtils.chat("Reset Reputation Helper.")

        questHelper.reset()
        miniBossHelper.reset()
        kuudraBossHelper.reset()
        update()
    }

    fun readLocationData(locations: List<Double>): LorenzVec? {
        if (locations.isEmpty()) return null
        val (x, y, z) = locations
        return LorenzVec(x, y, z).add(-1, 0, -1)
    }

    fun showLocations() = when (config.showLocation) {
        ShowLocationEntry.ALWAYS -> true
        ShowLocationEntry.ONLY_HOTKEY -> isHotkeyHeld()
        else -> false
    }
}
