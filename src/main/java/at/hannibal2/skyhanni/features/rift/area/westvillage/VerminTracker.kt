package at.hannibal2.skyhanni.features.rift.area.westvillage

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.features.rift.RiftApi
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.CollectionUtils.addSearchString
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.renderables.Searchable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import at.hannibal2.skyhanni.utils.tracker.SkyHanniTracker
import at.hannibal2.skyhanni.utils.tracker.TrackerData
import com.google.gson.annotations.Expose
import java.util.regex.Pattern

@SkyHanniModule
object VerminTracker {

    private val patternGroup = RepoPattern.group("rift.area.westvillage.vermintracker")

    /**
     * REGEX-TEST: §eYou vacuumed a §r§aSilverfish§r§e!
     */
    private val silverfishPattern by patternGroup.pattern(
        "silverfish",
        ".*§eYou vacuumed a §.*Silverfish.*",
    )

    /**
     * REGEX-TEST: §eYou vacuumed a §r§aSpider§r§e!
     */
    private val spiderPattern by patternGroup.pattern(
        "spider",
        ".*§eYou vacuumed a §.*Spider.*",
    )

    /**
     * REGEX-TEST: §eYou vacuumed a §r§aFly§r§e!
     */
    private val flyPattern by patternGroup.pattern(
        "fly",
        ".*§eYou vacuumed a §.*Fly.*",
    )

    /**
     * REGEX-TEST: §fVermin Bin: §a27 Silverfishes
     * REGEX-TEST: §fVermin Bin: §a19 Flies
     */
    private val verminBinPattern by patternGroup.pattern(
        "binline",
        "§fVermin Bin: §\\w(?<count>\\d+) (?<vermin>\\w+)",
    )

    /**
     * REGEX-TEST: §fVacuum Bag: §72 Silverfishes
     * REGEX-TEST: §fVacuum Bag: §70 Spiders
     */
    private val verminBagPattern by patternGroup.pattern(
        "bagline",
        "§fVacuum Bag: §\\w(?<count>\\d+) (?<vermin>\\w+)",
    )

    private var hasVacuum = false
    private val TURBOMAX_VACUUM = "TURBOMAX_VACUUM".toInternalName()

    private val config get() = RiftApi.config.area.westVillage.verminTracker

    private val tracker = SkyHanniTracker("Vermin Tracker", { Data() }, { it.rift.verminTracker }) {
        drawDisplay(it)
    }

    class Data : TrackerData() {

        override fun reset() {
            count.clear()
        }

        @Expose
        var count: MutableMap<VerminType, Int> = mutableMapOf()
    }

    enum class VerminType(val order: Int, val vermin: String, val pattern: Pattern) {
        FLY(1, "§aFlies", flyPattern),
        SPIDER(2, "§aSpiders", spiderPattern),
        SILVERFISH(3, "§aSilverfish", silverfishPattern),
    }

    @HandleEvent(onlyOnIsland = IslandType.THE_RIFT)
    fun onSecondPassed(event: SecondPassedEvent) {
        checkVacuum()
    }

    private fun checkVacuum() {
        hasVacuum = InventoryUtils.getItemsInOwnInventory()
            .any { it.getInternalName() == TURBOMAX_VACUUM }
    }

    @HandleEvent(onlyOnIsland = IslandType.THE_RIFT)
    fun onChat(event: SkyHanniChatEvent) {
        for (verminType in VerminType.entries) {
            if (verminType.pattern.matches(event.message)) {
                tracker.modify { it.count.addOrPut(verminType, 1) }

                if (config.hideChat) {
                    event.blockedReason = "vermin_vacuumed"
                }
            }
        }
    }

    @HandleEvent(onlyOnIsland = IslandType.THE_RIFT)
    fun onInventoryFullyOpened(event: InventoryFullyOpenedEvent) {
        if (event.inventoryName != "Vermin Bin") return

        val bin = event.inventoryItems[13]?.getLore() ?: return
        val bag = InventoryUtils.getItemsInOwnInventory()
            .firstOrNull { it.getInternalName() == TURBOMAX_VACUUM }
            ?.getLore().orEmpty()

        val binCounts = countVermin(bin, verminBinPattern)
        VerminType.entries.forEach { setVermin(it, binCounts[it] ?: 0) }

        if (bag.isEmpty()) return

        val bagCounts = countVermin(bag, verminBagPattern)
        VerminType.entries.forEach { addVermin(it, bagCounts[it] ?: 0) }
    }

    private fun countVermin(lore: List<String>, pattern: Pattern): Map<VerminType, Int> {
        val verminCounts = mutableMapOf(
            VerminType.SILVERFISH to 0,
            VerminType.SPIDER to 0,
            VerminType.FLY to 0,
        )
        for (line in lore) {
            pattern.matchMatcher(line) {
                val vermin = group("vermin")?.lowercase() ?: continue
                val verminCount = group("count")?.toInt() ?: continue
                val verminType = getVerminType(vermin)
                verminCounts[verminType] = verminCount
            }
        }
        return verminCounts
    }

    private fun getVerminType(vermin: String): VerminType {
        return when (vermin) {
            "silverfish", "silverfishes" -> VerminType.SILVERFISH
            "spider", "spiders" -> VerminType.SPIDER
            "fly", "flies" -> VerminType.FLY
            else -> VerminType.SILVERFISH
        }
    }

    private fun addVermin(vermin: VerminType, count: Int = 1) {
        tracker.modify(SkyHanniTracker.DisplayMode.TOTAL) { it.count.addOrPut(vermin, count) }
    }

    private fun setVermin(vermin: VerminType, count: Int) {
        tracker.modify(SkyHanniTracker.DisplayMode.TOTAL) { it.count[vermin] = count }
    }

    private fun drawDisplay(data: Data): List<Searchable> = buildList {
        addSearchString("§7Vermin Tracker:")
        for ((vermin, amount) in data.count.entries.sortedBy { it.key.order }) {
            val verminName = vermin.vermin
            addSearchString(" §7- §e${amount.addSeparators()} $verminName", verminName)
        }
    }

    init {
        tracker.initRenderer({ config.position }) { shouldShowDisplay() }
    }

    private fun shouldShowDisplay(): Boolean {
        if (!isEnabled()) return false
        if (!config.showOutsideWestVillage && !RiftApi.inWestVillage()) return false
        if (!config.showWithoutVacuum && !hasVacuum) return false

        return true
    }

    @HandleEvent
    fun onIslandChange(event: IslandChangeEvent) {
        if (event.newIsland == IslandType.THE_RIFT) {
            tracker.firstUpdate()
        }
    }

    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.register("shresetvermintracker") {
            description = "Resets the Vermin Tracker"
            category = CommandCategory.USERS_RESET
            callback { tracker.resetCommand() }
        }
    }

    private fun isEnabled() = RiftApi.inRift() && config.enabled
}
