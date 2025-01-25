package at.hannibal2.skyhanni.features.garden.pests

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.model.TabWidget
import at.hannibal2.skyhanni.events.TabListUpdateEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.garden.pests.PestTrapDataUpdatedEvent
import at.hannibal2.skyhanni.features.garden.GardenApi
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.RegexUtils.groupOrNull
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.TimeLimitedCache
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import com.google.common.cache.RemovalCause.EXPIRED
import com.google.gson.annotations.Expose
import java.util.regex.Matcher
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object PestTrapAPI {

    data class PestTrapData(
        @Expose var number: Int,
        @Expose var name: String? = null,
        @Expose var plot: String? = null,
        @Expose var type: PestTrapType? = PestTrapType.PEST_TRAP,
        @Expose var count: Int = 0,
        @Expose var baitCount: Int = 0,
        @Expose var baitType: SprayType? = null,
    ) {
        val isFull = count >= MAX_PEST_COUNT_PER_TRAP
        val noBait = baitCount == 0
    }

    enum class PestTrapType(val displayName: String) {
        PEST_TRAP("§2Pest Trap"),
        MOUSE_TRAP("§9Mouse Trap"),
        ;

        override fun toString() = displayName
    }

    private val patternGroup = RepoPattern.group("garden.pests.trap")
    private val storage get() = GardenApi.storage

    // Todo: Use this in the future to tell the user to enable the widget if it's disabled
    private val widgetEnabledAndVisible: TimeLimitedCache<TabWidget, Boolean> = baseWidgetStatus()

    var MAX_PEST_COUNT_PER_TRAP = 3
    private var lastTabHash: Int = 0
    private var lastTitleHash: Int = 0
    private var lastFullHash: Int = 0
    private var lastNoBaitHash: Int = 0
    private var lastTotalHash: Int = lastTitleHash + lastFullHash + lastNoBaitHash


    // <editor-fold desc="Patterns">
    /**
     * REGEX-TEST: §2§lGOTCHA! §7Your traps caught a §2Pest §7in §aPlot §r§bM4§r§r§7!
     * REGEX-TEST: §2§lGOTCHA! §7Your traps caught a §2Pest §7in §aPlot §r§bFR1§r§7!
     */
    private val caughtChatPattern by patternGroup.pattern(
        "chat.caught",
        "(?:§.)+GOTCHA! §7Your traps caught a §.Pest §7in §.Plot (?:§.)+.*(?:§.)+!"
    )

    private val tabListPestTrapsPattern = TabWidget.PEST_TRAPS.pattern
    private val tabListFullTrapsPattern = TabWidget.FULL_TRAPS.pattern
    private val tabListNoBaitPattern = TabWidget.NO_BAIT.pattern
    // </editor-fold>

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onChat(event: SkyHanniChatEvent) {
        caughtChatPattern.matchMatcher(event.message) {
            val storage = storage ?: return
            val plot = groupOrNull("plot") ?: return
            storage.pestTrapStatus.filter {
                it.plot == plot && it.count < MAX_PEST_COUNT_PER_TRAP
            }.forEach {
                it.count++
            }
        }
    }

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onTabListUpdate(event: TabListUpdateEvent) {
        val storage = storage ?: return
        lastTabHash = event.tabList.sumOf { it.hashCode() }.takeIf { it != lastTabHash } ?: return

        for (line in event.tabList) {
            line.updateDataFromTitle(storage)
            line.updateDataFromFull(storage)
            line.updateDataFromNoBait(storage)
        }

        lastTotalHash = (lastTitleHash + lastFullHash + lastNoBaitHash).takeIf { it != lastTotalHash } ?: return

        PestTrapDataUpdatedEvent(storage.pestTrapStatus).post()
    }

    private fun String.updateDataFromTitle(
        storage: ProfileSpecificStorage.GardenStorage
    ) = tabListPestTrapsPattern.matchMatcher(this@updateDataFromTitle) {
        widgetEnabledAndVisible[TabWidget.PEST_TRAPS] = true
        val thisHash = this@updateDataFromTitle.hashCode().takeIf { it != lastTitleHash } ?: return@matchMatcher
        lastTitleHash = thisHash

        val count = group("count").toIntOrNull() ?: return@matchMatcher
        val max = group("max").toIntOrNull() ?: return@matchMatcher

        MAX_PEST_COUNT_PER_TRAP = max(max, MAX_PEST_COUNT_PER_TRAP)
        val numberToTrack = min(count, MAX_PEST_COUNT_PER_TRAP)

        storage.pestTrapStatus = storage.pestTrapStatus.take(numberToTrack).toMutableList()
        ChatUtils.chat("Updated pest trap status to ${storage.pestTrapStatus.joinToString("\n")}")
    }

    private fun Matcher.extractTrapList() = listOf(
        groupOrNull("one"),
        groupOrNull("two"),
        groupOrNull("three"),
    )

    private fun String.updateDataFromFull(
        storage: ProfileSpecificStorage.GardenStorage
    ) = tabListFullTrapsPattern.matchMatcher(this@updateDataFromFull) {
        widgetEnabledAndVisible[TabWidget.FULL_TRAPS] = true
        lastFullHash = this@updateDataFromFull.hashCode().takeIf { it != lastFullHash } ?: return@matchMatcher

        val fullTraps = extractTrapList()
        storage.pestTrapStatus.filter {
            fullTraps[it.number] != null
        }.forEach {
            it.count = MAX_PEST_COUNT_PER_TRAP
        }
        ChatUtils.chat("Updated pest trap status to ${storage.pestTrapStatus.joinToString("\n")}")
    }

    private fun String.updateDataFromNoBait(
        storage: ProfileSpecificStorage.GardenStorage
    ) = tabListNoBaitPattern.matchMatcher(this@updateDataFromNoBait) {
        widgetEnabledAndVisible[TabWidget.NO_BAIT] = true
        lastNoBaitHash = this@updateDataFromNoBait.hashCode().takeIf { it != lastNoBaitHash } ?: return@matchMatcher

        val noBaitTraps = extractTrapList()
        storage.pestTrapStatus.filter {
            noBaitTraps[it.number] != null
        }.forEach {
            it.baitType = null
            it.baitCount = 0
        }
        ChatUtils.chat("Updated pest trap status to ${storage.pestTrapStatus.joinToString("\n")}")
    }

    @Suppress("UnstableApiUsage")
    private fun baseWidgetStatus() = TimeLimitedCache<TabWidget, Boolean>(
        expireAfterWrite = 60.seconds,
        removalListener = { key, _, removalCause ->
            if (key != null && GardenApi.inGarden() && removalCause == EXPIRED) {
                ChatUtils.userError(
                    "Could not read ${key.name.lowercase().replace("_", " ")} data from the tab list!",
                    replaceSameMessage = true
                )
            }
        }
    )
}
