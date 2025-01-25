package at.hannibal2.skyhanni.features.nether.reputationhelper.kuudra

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.jsonobjects.repo.ReputationQuest
import at.hannibal2.skyhanni.events.kuudra.KuudraCompleteEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.features.nether.kuudra.KuudraTier
import at.hannibal2.skyhanni.features.nether.reputationhelper.CrimsonIsleReputationHelper
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.addItemStack
import at.hannibal2.skyhanni.utils.CollectionUtils.addString
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NeuItems.getItemStack
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.addLine

class DailyKuudraBossHelper(private val reputationHelper: CrimsonIsleReputationHelper) {

    val kuudraTiers = mutableListOf<KuudraTier>()

    private var kuudraLocation: LorenzVec? = null
    private var allKuudraDone = true

    private val config get() = SkyHanniMod.feature.crimsonIsle.reputationHelper

    @HandleEvent(onlyOnIsland = IslandType.CRIMSON_ISLE)
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!config.enabled.get()) return
        if (!reputationHelper.showLocations()) return
        if (allKuudraDone) return

        kuudraLocation?.let {
            event.drawWaypointFilled(it, LorenzColor.WHITE.toColor())
            event.drawDynamicText(it, "Kuudra", 1.5)
        }
    }

    @HandleEvent
    fun onKuudraDone(event: KuudraCompleteEvent) {
        val tier = event.kuudraTier
        val kuudraTier = getByTier(tier) ?: return
        ChatUtils.debug("Detected kuudra tier done: ${kuudraTier.getDisplayName()}")
        reputationHelper.questHelper.finishKuudra(kuudraTier)
        kuudraTier.doneToday = true
        updateAllKuudraDone()
        reputationHelper.update()
    }

    fun MutableList<Renderable>.addKuudraBoss() {
        val done = kuudraTiers.count { it.doneToday }
        addString("")
        addString("§7Daily Kuudra (§e$done§8/§e5 killed§7)")
        if (done < 5) {
            for (tier in kuudraTiers) {
                if (config.hideComplete.get() && tier.doneToday) continue
                val result = if (tier.doneToday) "§aDone" else "§bTodo"
                val displayName = tier.getDisplayName()
                val displayItem = tier.displayItem

                addLine {
                    addString(" ")
                    addItemStack(displayItem.getItemStack())
                    addString("$displayName: $result")
                }
            }
        }
    }

    fun reset() {
        for (miniBoss in kuudraTiers) {
            miniBoss.doneToday = false
        }
        updateAllKuudraDone()
    }

    fun saveConfig(storage: ProfileSpecificStorage.CrimsonIsleStorage) {
        storage.kuudraTiersDone.clear()

        kuudraTiers.filter { it.doneToday }
            .forEach { storage.kuudraTiersDone.add(it.name) }
    }

    fun onRepoReload(data: Map<String, ReputationQuest>) {
        kuudraTiers.clear()
        var tier = 1
        for ((displayName, kuudraTier) in data) {
            val displayItem = kuudraTier.item
            val location = reputationHelper.readLocationData(kuudraTier.location)
            if (location != null) {
                kuudraLocation = location
            }
            kuudraTiers.add(KuudraTier(displayName, displayItem, location, tier))

            tier++
        }
    }

    fun loadData(storage: ProfileSpecificStorage.CrimsonIsleStorage) {
        if (kuudraTiers.isEmpty()) return
        for (name in storage.kuudraTiersDone) {
            getByDisplayName(name)?.doneToday = true
        }
        updateAllKuudraDone()
    }

    private fun updateAllKuudraDone() {
        allKuudraDone = !kuudraTiers.any { !it.doneToday }
    }

    private fun getByDisplayName(name: String) = kuudraTiers.firstOrNull { it.name == name }

    private fun getByTier(number: Int) = kuudraTiers.firstOrNull { it.tierNumber == number }
}
