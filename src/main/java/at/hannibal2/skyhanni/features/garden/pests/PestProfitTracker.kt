package at.hannibal2.skyhanni.features.garden.pests

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigManager
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.ItemAddManager
import at.hannibal2.skyhanni.data.jsonobjects.repo.GardenJson
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.ItemAddEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.PurseChangeCause
import at.hannibal2.skyhanni.events.PurseChangeEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
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
import at.hannibal2.skyhanni.utils.RegexUtils.matchGroup
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
import net.minecraft.util.ChatComponentText
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
     * REGEX-TEST: §6§lRARE DROP! §9Squeaky Toy §6(§6+1,549☘)
     * REGEX-TEST: §6§lRARE DROP! §6Squeaky Mousemat §6(§6+1,549☘)
     */
    private val pestRareDropPattern by patternGroup.pattern(
        "raredrop",
        "§6§l(?:RARE|PET) DROP! (?:§r)?(?<item>.+) §6\\(§6\\+.*☘\\)",
    )

    /**
     * REGEX-TEST: §a§lSPRAYONATOR! §r§7You sprayed §r§aPlot §r§7- §r§bFR1 §r§7with §r§aPlant Matter§r§7!
     * REGEX-TEST: §a§lSPRAYONATOR! §r§7You sprayed §r§aPlot §r§7- §r§bFR1 §r§7with §r§aDung§r§7!
     * REGEX-TEST: §a§lSPRAYONATOR! §r§7You sprayed §r§aPlot §r§7- §r§bFR1 §r§7with §r§aHoney Jar§r§7!
     * REGEX-TEST: §a§lSPRAYONATOR! §r§7You sprayed §r§aPlot §r§7- §r§bFR1 §r§7with §r§aTasty Cheese§r§7!
     * REGEX-TEST: §a§lSPRAYONATOR! §r§7You sprayed §r§aPlot §r§7- §r§bFR1 §r§7with §r§aCompost§r§7!
     */
    private val sprayonatorUsedPattern by patternGroup.pattern(
        "sprayonator",
        "§a§lSPRAYONATOR! §r§7You sprayed §r§aPlot §r§7- .* §r§7with §r§.(?<spray>.*)§r§7!",
    )

    val DUNG_ITEM = "DUNG".toInternalName()
    private val lastPestKillTimes: TimeLimitedCache<PestType, SimpleTimeMark> = TimeLimitedCache(15.seconds)
    private val tracker = SkyHanniBucketedItemTracker<PestType, BucketData>(
        "Pest Profit Tracker",
        { BucketData() },
        { it.garden.pestProfitTracker },
        { drawDisplay(it) },
    )
    private var adjustmentMap: Map<PestType, Map<NEUInternalName, Int>> = mapOf()

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

        override fun PestType.isBucketSelectable() = PestType.filterableEntries.contains(this)

        fun getTotalPestCount(): Long =
            if (selectedBucket != null) pestKills[selectedBucket] ?: 0L
            else (pestKills.entries.filter { it.key != PestType.UNKNOWN }.sumOf { it.value } + totalPestsKills)

        @Expose
        @Deprecated("Use pestKills instead")
        var totalPestsKills = 0L

        @Expose
        var pestKills: MutableMap<PestType, Long> = EnumMap(PestType::class.java)

        @Expose
        var spraysUsed: MutableMap<SprayType, Long> = EnumMap(SprayType::class.java)
    }

    private fun SprayType.addSprayUsed() = tracker.modify { it.spraysUsed.addOrPut(this, 1) }

    @HandleEvent
    fun onItemAdd(event: ItemAddEvent) {
        if (!isEnabled() || event.source != ItemAddManager.Source.COMMAND) return
        tracker.addItem(event)
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!isEnabled()) return
        event.checkPestChats()
        event.checkSprayChats()
    }

    private fun LorenzChatEvent.checkPestChats() {
        PestAPI.pestDeathChatPattern.matchMatcher(message) {
            val pest = PestType.getByNameOrNull(group("pest")) ?: ErrorManager.skyHanniError(
                "Could not find PestType for killed pest, please report this in the Discord.",
                "pest_name" to group("pest"),
                "full_message" to message,
            )
            val internalName = NEUInternalName.fromItemNameOrNull(group("item")) ?: return
            val amount = group("amount").toInt().fixAmount(internalName, pest)

            tryAddItem(pest, internalName, amount)

            // Field Mice drop 6 separate items, but we only want to count the kill once
            if (pest == PestType.FIELD_MOUSE && internalName == DUNG_ITEM) addKill(pest)
            else if (pest != PestType.FIELD_MOUSE) addKill(pest)

            if (config.hideChat) blockedReason = "pest_drop"
        }
        pestRareDropPattern.matchMatcher(message) {
            val itemGroup = group("item")
            val internalName = NEUInternalName.fromItemNameOrNull(itemGroup) ?: return
            val pest = PestType.getByInternalNameItemOrNull(internalName) ?: return@matchMatcher
            val amount = 1.fixAmount(internalName, pest).also {
                if (it == 1) return@also
                // If the amount was fixed, edit the chat message to reflect the change
                val fixedString = message.replace(itemGroup, "§a${it}x $itemGroup")
                chatComponent = ChatComponentText(fixedString)
            }

            tryAddItem(pest, internalName, amount)
            // pests always have guaranteed loot, therefore there's no need to add kill here
        }
    }

    private fun LorenzChatEvent.checkSprayChats() {
        sprayonatorUsedPattern.matchGroup(message, "spray")?.let {
            SprayType.getByNameOrNull(it)?.addSprayUsed()
        }
    }

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        adjustmentMap = event.getConstant<GardenJson>("Garden").pestRareDrops
    }

    private fun Int.fixAmount(internalName: NEUInternalName, pestType: PestType): Int {
        return adjustmentMap[pestType]?.get(internalName) ?: this
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

        val selectedBucket = bucketData.selectedBucket
        val pestCount = selectedBucket?.let { bucketData.pestKills[it] } ?: bucketData.getTotalPestCount()
        val pestCountFormat = "§7${selectedBucket?.pluralName ?: "Pests"} killed: §e${pestCount.addSeparators()}"

        add(
            when {
                selectedBucket != null -> Renderable.string(pestCountFormat).toSearchable()
                else -> Renderable.hoverTips(
                    pestCountFormat,
                    buildList {
                        // Sort by A-Z in displaying real types
                        bucketData.pestKills.toList().sortedBy {
                            it.first.displayName
                        }.forEach { (type, count) ->
                            add("§7${type.pluralName}: §e${count.addSeparators()}")
                        }
                    }
                ).toSearchable()
            }
        )

        if (selectedBucket == null || selectedBucket.spray != null) {
            val applicableSprays = SprayType.getByPestTypeOrAll(selectedBucket)
            val applicableSpraysUsed = bucketData.spraysUsed.filterKeys { it in applicableSprays }
            val spraysUsedFormat = "§7Sprays used: §a${applicableSpraysUsed.values.sum().addSeparators()}"

            add(
                Renderable.hoverTips(
                    spraysUsedFormat,
                    buildList {
                        applicableSpraysUsed.forEach { (spray, count) ->
                            add("§7${spray.displayName}: §a${count.addSeparators()}")
                        }
                    }
                ).toSearchable()
            )
        }

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
        val notHoldingTool = !PestAPI.hasVacuumInHand() && !PestAPI.hasSprayonatorInHand()
        if (allInactive && notHoldingTool) return

        tracker.renderDisplay(config.position)
    }

    @HandleEvent
    fun onPurseChange(event: PurseChangeEvent) {
        if (!isEnabled() || event.reason != PurseChangeCause.GAIN_MOB_KILL || lastPestKillTimes.isEmpty()) return
        val coins = event.coins.takeIf { it in 1000.0..10000.0 } ?: return

        // Get a list of all that have been killed in the last 2 seconds, it will
        // want to be the most recent one that was killed.
        val pest = lastPestKillTimes.minByOrNull { it.value }?.key ?: return
        tracker.addCoins(pest, coins.roundToInt())
    }

    @HandleEvent
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
                    PestType.getByInternalNameItemOrNull(item) ?: PestType.UNKNOWN
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
