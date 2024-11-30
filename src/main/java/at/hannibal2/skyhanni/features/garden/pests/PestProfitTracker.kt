package at.hannibal2.skyhanni.features.garden.pests

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigManager
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.ItemAddManager
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.ItemAddEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.PurseChangeCause
import at.hannibal2.skyhanni.events.PurseChangeEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.CollectionUtils.addSearchString
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeLimitedCache
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.Searchable
import at.hannibal2.skyhanni.utils.renderables.toSearchable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import at.hannibal2.skyhanni.utils.tracker.BucketedItemTrackerData
import at.hannibal2.skyhanni.utils.tracker.ItemTrackerData.TrackedItem
import at.hannibal2.skyhanni.utils.tracker.SkyHanniBucketedItemTracker
import com.google.gson.JsonPrimitive
import com.google.gson.annotations.Expose
import com.google.gson.reflect.TypeToken
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.EnumMap
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object PestProfitTracker {
    val config get() = SkyHanniMod.feature.garden.pests.pestProfitTacker

    private val patternGroup = RepoPattern.group("garden.pests.tracker")

    /**
     * REGEX-TEST: §6§lRARE DROP! §9Mutant Nether Wart §6(§6+1,344☘)
     * REGEX-TEST: §6§lPET DROP! §r§5Slug §6(§6+1300☘)
     * REGEX-TEST: §6§lPET DROP! §r§6Slug §6(§6+1300☘)
     */
    private val pestRareDropPattern by patternGroup.pattern(
        "raredrop",
        "§6§l(?:RARE|PET) DROP! (?:§r)?(?<item>.+) §6\\(§6\\+.*☘\\)",
    )

    private val lastPestKillTimes: TimeLimitedCache<PestType, SimpleTimeMark> = TimeLimitedCache(15.seconds)
    private val tracker = SkyHanniBucketedItemTracker<PestType, BucketData>(
        "Pest Profit Tracker",
        { BucketData() },
        { it.garden.pestProfitTracker },
        { drawDisplay(it) },
    )

    class BucketData : BucketedItemTrackerData<PestType>() {
        override fun resetItems() {
            totalPestsKills = 0L
            pestKills.clear()
        }

        override fun getDescription(timesGained: Long): List<String> {
            val percentage = timesGained.toDouble() / getTotalPestCount()
            val dropRate = LorenzUtils.formatPercentage(percentage.coerceAtMost(1.0))
            return listOf(
                "§7Dropped §e${timesGained.addSeparators()} §7times.",
                "§7Your drop rate: §c$dropRate.",
            )
        }

        override fun getCoinName(bucket: PestType?, item: TrackedItem) = "§6Pest Kill Coins"

        override fun getCoinDescription(bucket: PestType?, item: TrackedItem): List<String> {
            val pestsCoinsFormat = item.totalAmount.shortFormat()
            return listOf(
                "§7Killing pests gives you coins.",
                "§7You got §6$pestsCoinsFormat coins §7that way.",
            )
        }

        override fun PestType.isBucketFilterable() = PestType.filterableEntries.contains(this)

        fun getTotalPestCount(): Long =
            if (getSelectedBucket() != null) pestKills[getSelectedBucket()] ?: 0L
            else (pestKills.entries.filter { it.key != PestType.UNKNOWN }.sumOf { it.value } + totalPestsKills)

        @Expose
        @Deprecated("Use pestKills instead")
        var totalPestsKills = 0L

        @Expose
        var pestKills: MutableMap<PestType, Long> = EnumMap(PestType::class.java)
    }

    @SubscribeEvent
    fun onItemAdd(event: ItemAddEvent) {
        if (!isEnabled() || event.source != ItemAddManager.Source.COMMAND) return
        tracker.addItem(event)
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!isEnabled()) return
        var pestThisRun = PestType.UNKNOWN
        PestAPI.pestDeathChatPattern.matchMatcher(event.message) {
            val amount = group("amount").toInt()
            val internalName = NEUInternalName.fromItemNameOrNull(group("item")) ?: return
            val pest = PestType.getByNameOrNull(group("pest")) ?: ErrorManager.skyHanniError(
                "Could not find PestType for killed pest, please report this in the Discord.",
                "pest_name" to group("pest"),
                "item_name" to group("item"),
                "amount" to amount,
                "full_message" to event.message,
            )
            pestThisRun = pest

            tryAddItem(pest, internalName, amount)
            addKill(pest)
            if (config.hideChat) event.blockedReason = "pest_drop"
        }
        pestRareDropPattern.matchMatcher(event.message) {
            val internalName = NEUInternalName.fromItemNameOrNull(group("item")) ?: return
            tryAddItem(pestThisRun, internalName, 1)
            // pests always have guaranteed loot, therefore there's no need to add kill here
        }
    }

    private fun tryAddItem(type: PestType, internalName: NEUInternalName, amount: Int) {
        tracker.addItem(type, internalName, amount)
    }

    private fun addKill(type: PestType) {
        tracker.modify {
            it.pestKills.addOrPut(type, 1)
        }
        lastPestKillTimes[type] = SimpleTimeMark.now()
    }

    private fun drawDisplay(bucketData: BucketData): List<Searchable> = buildList {
        addSearchString("§e§lPest Profit Tracker")
        tracker.addBucketSelector(this, bucketData, "Pest Type")

        val profit = tracker.drawItems(bucketData, { true }, this)

        val totalPestCount = bucketData.getTotalPestCount()
        add(
            Renderable.hoverTips(
                "§7Pests killed: §e${totalPestCount.addSeparators()}",
                buildList {
                    val data = bucketData.pestKills
                    data[PestType.UNKNOWN]?.let {
                        "§8Unknown: §e${it.addSeparators()}"
                    }
                    // Sort by A-Z in displaying real types
                    data.toList().sortedBy { it.first.displayName }.forEach { (type, count) ->
                        "§7${type.displayName}: §e${count.addSeparators()}"
                    }
                }
            ).toSearchable(),
        )
        add(tracker.addTotalProfit(profit, bucketData.getTotalPestCount(), "kill"))

        tracker.addPriceFromButton(this)
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent) {
        if (!isEnabled()) return
        if (GardenAPI.isCurrentlyFarming()) return
        val allInactive = lastPestKillTimes.all {
            it.value.passedSince() > config.timeDisplayed.seconds
        }
        if (allInactive && !PestAPI.hasVacuumInHand()) return

        tracker.renderDisplay(config.position)
    }

    @SubscribeEvent
    fun onPurseChange(event: PurseChangeEvent) {
        if (!isEnabled() || event.reason != PurseChangeCause.GAIN_MOB_KILL) return
        val coins = event.coins
        if (coins > 1000) return

        // Get a list of all that have been killed in the last 2 seconds, it will
        // want to be the most recent one that was killed.
        val lastPestKillType = lastPestKillTimes.entries().sortedBy { (_, time) ->
            time
        }.firstOrNull { (_, time) ->
            time.passedSince() < 2.seconds
        }?.key ?: return

        tracker.addCoins(lastPestKillType, event.coins.roundToInt())
    }

    @SubscribeEvent
    fun onIslandChange(event: IslandChangeEvent) {
        if (event.newIsland == IslandType.GARDEN) {
            tracker.firstUpdate()
        }
    }

    fun resetCommand() {
        tracker.resetCommand()
    }

    fun isEnabled() = GardenAPI.inGarden() && config.enabled

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        // Move any items that are in pestProfitTracker.items as the object as a map themselves,
        // migrate them to the new format of PestType -> Drop Count. All entries will be mapped to
        // respective PestType when possible, and the rest will be moved to UNKNOWN.
        val pestTypeMap: MutableMap<NEUInternalName, PestType> = mutableMapOf()
        val pestKillCountMap: MutableMap<PestType, Long> = mutableMapOf()
        event.move(
            69,
            "#profile.garden.pestProfitTracker.items",
            "#profile.garden.pestProfitTracker.bucketedItems",
        ) { items ->
            val newItems: MutableMap<PestType, MutableMap<String, TrackedItem>> = mutableMapOf()
            val type = object : TypeToken<MutableMap<String, TrackedItem>>() {}.type
            val oldItems: MutableMap<String, TrackedItem> = ConfigManager.gson.fromJson(items, type)

            oldItems.forEach { (neuInternalName, trackedItem) ->
                val item = neuInternalName.toInternalName()
                val pest = pestTypeMap.getOrPut(item) {
                    PestType.getByInternalNameItemOrNull(item)
                }

                // If the map for the pest already contains this item, combine the amounts
                val storage = newItems.getOrPut(pest) { mutableMapOf() }
                val newItem = storage[neuInternalName] ?: TrackedItem()
                newItem.totalAmount += trackedItem.totalAmount
                newItem.timesGained += trackedItem.timesGained
                storage[neuInternalName] = newItem
                // If the timesGained is higher than pestKillCountMap[pest], update it
                if (pest != PestType.UNKNOWN) { // Ignore UNKNOWN, as we don't want inflated kill counts
                    pestKillCountMap[pest] = pestKillCountMap.getOrDefault(pest, 0).coerceAtLeast(newItem.timesGained)
                }
            }

            ConfigManager.gson.toJsonTree(newItems)
        }

        event.add(69, "#profile.garden.pestProfitTracker.pestKills") {
            ConfigManager.gson.toJsonTree(pestKillCountMap)
        }

        event.transform(69, "#profile.garden.pestProfitTracker.totalPestsKills") { entry ->
            // Subtract all pestKillCountMap values from the totalPestsKills
            JsonPrimitive(
                entry.asLong - pestKillCountMap.entries.filter {
                    it.key != PestType.UNKNOWN
                }.sumOf { it.value }
            )
        }
    }
}
