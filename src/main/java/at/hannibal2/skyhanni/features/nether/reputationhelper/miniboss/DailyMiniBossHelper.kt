package at.hannibal2.skyhanni.features.nether.reputationhelper.miniboss

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.jsonobjects.repo.ReputationQuest
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.features.combat.damageindicator.DamageIndicatorManager
import at.hannibal2.skyhanni.features.nether.reputationhelper.CrimsonIsleReputationHelper
import at.hannibal2.skyhanni.features.nether.reputationhelper.dailyquest.DailyQuestHelper
import at.hannibal2.skyhanni.features.nether.reputationhelper.dailyquest.quest.MiniBossQuest
import at.hannibal2.skyhanni.features.nether.reputationhelper.dailyquest.quest.QuestState
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.addItemStack
import at.hannibal2.skyhanni.utils.CollectionUtils.addString
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.NeuItems.getItemStack
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.addLine

@SkyHanniModule
object DailyMiniBossHelper {

    val miniBosses = mutableListOf<CrimsonMiniBoss>()
    private val config get() = SkyHanniMod.feature.crimsonIsle.reputationHelper

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        if (!isEnabled()) return

        val message = event.message
        for (miniBoss in miniBosses) {
            miniBoss.pattern.matchMatcher(message) {
                finished(miniBoss)
            }
        }
    }

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!isEnabled()) return
        if (!CrimsonIsleReputationHelper.showLocations()) return

        val playerLocation = LocationUtils.playerLocation()
        for (miniBoss in miniBosses) {
            if (miniBoss.doneToday && !needMiniBossQuest(miniBoss)) continue
            val location = miniBoss.location ?: continue
            if (DamageIndicatorManager.getNearestDistanceTo(location) < 40 && playerLocation.distance(location) < 40) continue

            event.drawWaypointFilled(location, LorenzColor.WHITE.toColor())
            event.drawDynamicText(location, miniBoss.displayName, 1.5)
        }
    }

    private fun needMiniBossQuest(miniBoss: CrimsonMiniBoss) =
        DailyQuestHelper.getQuest<MiniBossQuest>()?.let {
            it.miniBoss == miniBoss && it.state == QuestState.ACCEPTED
        } ?: false

    private fun finished(miniBoss: CrimsonMiniBoss) {
        DailyQuestHelper.finishMiniBoss(miniBoss)
        miniBoss.doneToday = true
        CrimsonIsleReputationHelper.update()
    }

    fun MutableList<Renderable>.addDailyMiniBoss() {
        val done = miniBosses.count { it.doneToday }

        addString("")
        addString("§7Daily Bosses (§e$done§8/§e5 killed§7)")

        if (done != 5) {
            for (miniBoss in miniBosses) {
                if (config.hideComplete.get() && miniBoss.doneToday) continue
                val result = if (miniBoss.doneToday) "§aDone" else "§bTodo"
                val displayName = miniBoss.displayName
                val displayItem = miniBoss.displayItem

                addLine {
                    addString(" ")
                    addItemStack(displayItem.getItemStack())
                    addString("§5$displayName§7: $result")
                }
            }
        }
    }

    fun reset() {
        for (miniBoss in miniBosses) {
            miniBoss.doneToday = false
        }
    }

    fun saveConfig(storage: ProfileSpecificStorage.CrimsonIsleStorage) {
        storage.miniBossesDoneToday.clear()

        miniBosses.filter { it.doneToday }
            .forEach { storage.miniBossesDoneToday.add(it.displayName) }
    }

    fun onRepoReload(data: Map<String, ReputationQuest>) {
        miniBosses.clear()
        for ((displayName, quest) in data) {
            val displayItem = quest.item
            val pattern = "§f *§r§6§l${displayName.uppercase()} DOWN!".toPattern()
            val location = CrimsonIsleReputationHelper.readLocationData(quest.location)
            miniBosses.add(CrimsonMiniBoss(displayName, displayItem, location, pattern))
        }
    }

    fun loadData(storage: ProfileSpecificStorage.CrimsonIsleStorage) {
        if (miniBosses.isEmpty()) return
        for (name in storage.miniBossesDoneToday) {
            getByDisplayName(name)!!.doneToday = true
        }
    }

    private fun getByDisplayName(name: String) = miniBosses.firstOrNull { it.displayName == name }
    private fun isEnabled() = IslandType.CRIMSON_ISLE.isInIsland() && config.enabled.get()
}
