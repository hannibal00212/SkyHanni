package at.hannibal2.skyhanni.features.event.jerry.frozentreasure

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.features.event.winter.FrozenTreasureConfig.FrozenTreasureDisplayEntry
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.data.ScoreboardData
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.CollectionUtils.addSearchString
import at.hannibal2.skyhanni.utils.ConfigUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.renderables.Searchable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import at.hannibal2.skyhanni.utils.tracker.SkyHanniTracker
import at.hannibal2.skyhanni.utils.tracker.TrackerData
import com.google.gson.annotations.Expose
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object FrozenTreasureTracker {

    private val config get() = SkyHanniMod.feature.event.winter.frozenTreasureTracker

    private val compactPattern by RepoPattern.pattern(
        "event.jerry.frozentreasure.compact",
        "COMPACT! You found an Enchanted Ice!"
    )

    private var estimatedIce = 0L
    private var lastEstimatedIce = 0L
    private var icePerSecond = mutableListOf<Long>()
    private var icePerHour = 0
    private var stoppedChecks = 0
    private val tracker = SkyHanniTracker("Frozen Treasure Tracker", { Data() }, { it.frozenTreasureTracker }) {
        formatDisplay(drawDisplay(it))
    }

    init {
        FrozenTreasure.entries.forEach { it.chatPattern }
    }

    class Data : TrackerData() {

        override fun reset() {
            treasureCount.clear()
            treasuresMined = 0
            compactProcs = 0
        }

        @Expose
        var treasuresMined = 0

        @Expose
        var compactProcs = 0

        @Expose
        var treasureCount: MutableMap<FrozenTreasure, Int> = mutableMapOf()
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        icePerHour = 0
        stoppedChecks = 0
        icePerSecond = mutableListOf()
        tracker.update()
    }

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!onJerryWorkshop()) return

        val difference = estimatedIce - lastEstimatedIce
        lastEstimatedIce = estimatedIce

        if (difference == estimatedIce) return

        if (difference == 0L) {
            if (icePerSecond.isEmpty()) return
            stoppedChecks += 1
        } else {
            if (stoppedChecks > 60) {
                stoppedChecks = 0
                icePerSecond.clear()
                icePerHour = 0
            }
            while (stoppedChecks > 0) {
                stoppedChecks -= 1
                icePerSecond.add(0)
            }
            icePerSecond.add(difference)
            val listCopy = icePerSecond
            while (listCopy.size > 1200) listCopy.removeAt(0)
            icePerSecond = listCopy
        }
        icePerHour = (icePerSecond.average() * 3600).toInt()
    }

    private fun formatDisplay(map: List<Searchable>): List<Searchable> {
        val newList = mutableListOf<Searchable>()
        for (index in config.textFormat) {
            // TODO, change functionality to use enum rather than ordinals
            newList.add(map[index.ordinal])
        }
        return newList
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!ProfileStorageData.loaded) return
        if (!onJerryWorkshop()) return

        val message = event.message.removeColor().trim()

        compactPattern.matchMatcher(message) {
            tracker.modify {
                it.compactProcs += 1
            }
            if (config.hideMessages) event.blockedReason = "frozen treasure tracker"
        }

        for (treasure in FrozenTreasure.entries.filter { it.chatPattern.matches(message) }) {
            tracker.modify {
                it.treasuresMined += 1
                it.treasureCount.addOrPut(treasure, 1)
            }
            if (config.hideMessages) event.blockedReason = "frozen treasure tracker"
        }
    }

    private fun drawDisplay(data: Data) = buildList<Searchable> {
        calculateIce(data)
        addSearchString("§e§lFrozen Treasure Tracker")
        addSearchString("§6${formatNumber(data.treasuresMined)} Treasures Mined")
        addSearchString("§3${formatNumber(estimatedIce)} Total Ice")
        addSearchString("§3${formatNumber(icePerHour)} Ice/hr")
        addSearchString("§8${formatNumber(data.compactProcs)} Compact Procs")
        addSearchString("")

        for (treasure in FrozenTreasure.entries) {
            val count = (data.treasureCount[treasure] ?: 0) * if (config.showAsDrops) treasure.defaultAmount else 1
            addSearchString("§b${formatNumber(count)} ${treasure.displayName}", treasure.displayName)
        }
        addSearchString("")
    }

    fun formatNumber(amount: Number): String {
        if (amount is Int) return amount.addSeparators()
        if (amount is Long) return amount.shortFormat()
        return "$amount"
    }

    private fun calculateIce(data: Data) {
        estimatedIce = data.compactProcs * 160L
        for (treasure in FrozenTreasure.entries) {
            val amount = data.treasureCount[treasure] ?: 0
            estimatedIce += amount * treasure.defaultAmount * treasure.iceMultiplier
        }
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent) {
        if (!config.enabled) return
        if (!onJerryWorkshop()) return
        if (config.onlyInCave && !inGlacialCave()) return

        tracker.renderDisplay(config.position)
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(2, "misc.frozenTreasureTracker", "event.winter.frozenTreasureTracker")
        event.move(
            11,
            "event.winter.frozenTreasureTracker.textFormat",
            "event.winter.frozenTreasureTracker.textFormat"
        ) { element ->
            ConfigUtils.migrateIntArrayListToEnumArrayList(element, FrozenTreasureDisplayEntry::class.java)
        }
    }

    private fun onJerryWorkshop() = IslandType.WINTER.isInIsland()

    private fun inGlacialCave() =
        onJerryWorkshop() && ScoreboardData.sidebarLinesFormatted.contains(" §7⏣ §3Glacial Cave")

    fun resetCommand() {
        tracker.resetCommand()
    }
}
