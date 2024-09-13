package at.hannibal2.skyhanni.features.nether.reputationhelper

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.features.crimsonisle.ReputationHelperConfig.ShowLocationEntry
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.data.jsonobjects.repo.CrimsonIsleReputationJson
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.SackChangeEvent
import at.hannibal2.skyhanni.features.nether.reputationhelper.dailyquest.DailyQuestHelper
import at.hannibal2.skyhanni.features.nether.reputationhelper.dailyquest.QuestLoader
import at.hannibal2.skyhanni.features.nether.reputationhelper.kuudra.DailyKuudraBossHelper
import at.hannibal2.skyhanni.features.nether.reputationhelper.miniboss.DailyMiniBossHelper
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.addAsSingletonList
import at.hannibal2.skyhanni.utils.ConditionalUtils.afterChange
import at.hannibal2.skyhanni.utils.ConfigUtils
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NEUItems
import at.hannibal2.skyhanni.utils.RenderUtils.renderStringsAndItems
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TabListData
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class CrimsonIsleReputationHelper(skyHanniMod: SkyHanniMod) {

    private val config get() = SkyHanniMod.feature.crimsonIsle.reputationHelper

    val questHelper = DailyQuestHelper(this)
    val miniBossHelper = DailyMiniBossHelper(this)
    val kuudraBossHelper = DailyKuudraBossHelper(this)

    var factionType = FactionType.NONE

    private var lastUpdate = SimpleTimeMark.farPast()

    private var display = emptyList<List<Any>>()
    private var dirty = true
    var tabListQuestsMissing = false

    /**
     *  c - Barbarian Not Accepted
     *  d - Mage Not Accepted
     *  e - Accepted
     *  a - Completed
     */
    val tabListQuestPattern by RepoPattern.pattern(
        "crimson.reputation.tablist",
        " §r§[cdea].*",
    )

    init {
        skyHanniMod.loadModule(questHelper)
        skyHanniMod.loadModule(miniBossHelper)
        skyHanniMod.loadModule(kuudraBossHelper)
    }

    @SubscribeEvent
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

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        ProfileStorageData.profileSpecific?.crimsonIsle?.let {
            miniBossHelper.loadData(it)
            kuudraBossHelper.loadData(it)
            questHelper.load(it)
        }
    }

    @SubscribeEvent
    fun onSackChange(event: SackChangeEvent) {
        dirty = true
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

        if (event.repeatSeconds(3)) {
            val list = TabListData.getTabList().filter { it.contains("Reputation:") }
            for (line in list) {
                factionType = if (line.contains("Mage")) {
                    FactionType.MAGE
                } else if (line.contains("Barbarian")) {
                    FactionType.BARBARIAN
                } else {
                    FactionType.NONE
                }
            }
        }
    }

    @SubscribeEvent
    fun onConfigInit(event: ConfigLoadEvent) {
        config.hideComplete.afterChange {
            updateRender()
        }
    }

    private fun updateRender() {
        val newList = mutableListOf<List<Any>>()

        // TODO test
        if (factionType == FactionType.NONE) return

        newList.addAsSingletonList("§e§lReputation Helper")
        if (tabListQuestsMissing) {
            newList.addAsSingletonList("§cFaction Quests Widget not found!")
            newList.addAsSingletonList("§7Open §e/tab §7and enable it!")
        } else {
            questHelper.render(newList)
            miniBossHelper.render(newList)
            kuudraBossHelper.render(newList)
        }


        display = newList
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!config.enabled.get()) return
        if (!IslandType.CRIMSON_ISLE.isInIsland()) return

        if (config.useHotkey && !isHotkeyHeld()) {
            return
        }

        config.position.renderStringsAndItems(
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

    @SubscribeEvent
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
