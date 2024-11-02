package at.hannibal2.skyhanni.features.fishing.tracker

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.ItemAddManager
import at.hannibal2.skyhanni.data.jsonobjects.repo.FishingProfitItemsJson
import at.hannibal2.skyhanni.events.FishingBobberCastEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.ItemAddEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.features.fishing.FishingAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.addSearchString
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.asInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.RenderableUtils.addButton
import at.hannibal2.skyhanni.utils.renderables.Searchable
import at.hannibal2.skyhanni.utils.renderables.toSearchable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import at.hannibal2.skyhanni.utils.tracker.ItemTrackerData
import at.hannibal2.skyhanni.utils.tracker.SkyHanniItemTracker
import at.hannibal2.skyhanni.utils.tracker.SkyHanniTracker
import com.google.gson.annotations.Expose
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

typealias CategoryName = String

@SkyHanniModule
object FishingProfitTracker {

    val config get() = SkyHanniMod.feature.fishing.fishingProfitTracker

    private val coinsChatPattern by RepoPattern.pattern(
        "fishing.tracker.chat.coins",
        ".* CATCH! §r§bYou found §r§6(?<coins>.*) Coins§r§b\\.",
    )

    private var lastCatchTime = SimpleTimeMark.farPast()
    private val tracker = SkyHanniItemTracker(
        "Fishing Profit Tracker",
        { Data() },
        { it.fishing.fishingProfitTracker },
    ) { drawDisplay(it) }

    class Data : ItemTrackerData() {

        override fun resetItems() {
            totalCatchAmount = 0
        }

        override fun getDescription(timesCaught: Long): List<String> {
            val percentage = timesCaught.toDouble() / totalCatchAmount
            val catchRate = LorenzUtils.formatPercentage(percentage.coerceAtMost(1.0))

            return listOf(
                "§7Caught §e${timesCaught.addSeparators()} §7times.",
                "§7Your catch rate: §c$catchRate",
            )
        }

        override fun getCoinName(item: TrackedItem) = "§6Fished Coins"

        override fun getCoinDescription(item: TrackedItem): List<String> {
            val mobKillCoinsFormat = item.totalAmount.shortFormat()
            return listOf(
                "§7You fished up §6$mobKillCoinsFormat coins §7already.",
            )
        }

        override fun getCustomPricePer(internalName: NEUInternalName): Double {
            // TODO find better way to tell if the item is a trophy
            val neuInternalNames = itemCategories["Trophy Fish"].orEmpty()

            return if (internalName in neuInternalNames) {
                SkyHanniTracker.getPricePer(MAGMA_FISH) * FishingAPI.getFilletPerTrophy(internalName)
            } else super.getCustomPricePer(internalName)
        }

        @Expose
        var totalCatchAmount = 0L
    }

    private val ItemTrackerData.TrackedItem.timesCaught get() = timesGained

    private val MAGMA_FISH by lazy { "MAGMA_FISH".asInternalName() }

    private const val NAME_ALL: CategoryName = "All"
    private var currentCategory: CategoryName = NAME_ALL

    private var itemCategories = mapOf<String, List<NEUInternalName>>()

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        itemCategories = event.getConstant<FishingProfitItemsJson>("FishingProfitItems").categories
    }

    private fun getCurrentCategories(data: Data): Map<CategoryName, Int> {
        val map = mutableMapOf<CategoryName, Int>()
        map[NAME_ALL] = data.items.size
        for ((name, items) in itemCategories) {
            val amount = items.count { it in data.items }
            if (amount > 0) {
                map[name] = amount
            }
        }

        return map
    }

    private fun drawDisplay(data: Data): List<Searchable> = buildList {
        addSearchString("§e§lFishing Profit Tracker")
        val filter: (NEUInternalName) -> Boolean = addCategories(data)

        val profit = tracker.drawItems(data, filter, this)

        val fishedCount = data.totalCatchAmount
        add(
            Renderable.hoverTips(
                "§7Times fished: §e${fishedCount.addSeparators()}",
                listOf("§7You've reeled in §e${fishedCount.addSeparators()} §7catches."),
            ).toSearchable(),
        )

        add(tracker.addTotalProfit(profit, data.totalCatchAmount, "catch"))

        tracker.addPriceFromButton(this)
    }

    private fun MutableList<Searchable>.addCategories(data: Data): (NEUInternalName) -> Boolean {
        val amounts = getCurrentCategories(data)
        checkMissingItems(data)
        val list = amounts.keys.toList()
        if (currentCategory !in list) {
            currentCategory = NAME_ALL
        }

        if (tracker.isInventoryOpen()) {
            addButton(
                prefix = "§7Category: ",
                getName = currentCategory + " §7(" + amounts[currentCategory] + ")",
                onChange = {
                    val id = list.indexOf(currentCategory)
                    currentCategory = list[(id + 1) % list.size]
                    tracker.update()
                },
            )
        }

        val filter: (NEUInternalName) -> Boolean = if (currentCategory == NAME_ALL) {
            { true }
        } else {
            { it in (itemCategories[currentCategory].orEmpty()) }
        }
        return filter
    }

    private fun checkMissingItems(data: Data) {
        val missingItems = mutableListOf<NEUInternalName>()
        for (internalName in data.items.keys) {
            if (itemCategories.none { internalName in it.value }) {
                missingItems.add(internalName)
            }
        }
        if (missingItems.isNotEmpty()) {
            val label = StringUtils.pluralize(missingItems.size, "item", withNumber = true)
            ErrorManager.logErrorStateWithData(
                "Loaded $label not in a fishing category",
                "Found items missing in itemCategories",
                "missingItems" to missingItems,
                noStackTrace = true,
            )
        }
    }

    @SubscribeEvent
    fun onItemAdd(event: ItemAddEvent) {
        if (!isEnabled()) return

        if (event.source == ItemAddManager.Source.COMMAND) {
            tryAddItem(event.internalName, event.amount, command = true)
            return
        }

        DelayedRun.runDelayed(500.milliseconds) {
            tryAddItem(event.internalName, event.amount, command = false)
        }
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        coinsChatPattern.matchMatcher(event.message) {
            tryAddItem(NEUInternalName.SKYBLOCK_COIN, group("coins").formatInt(), command = false)
            addCatch()
        }
    }

    private fun addCatch() {
        tracker.modify {
            it.totalCatchAmount++
        }
        lastCatchTime = SimpleTimeMark.now()
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent) {
        if (!isEnabled()) return

        val recentPickup = config.showWhenPickup && lastCatchTime.passedSince() < 3.seconds
        if (recentPickup || FishingAPI.isFishing(checkRodInHand = false)) {
            tracker.renderDisplay(config.position)
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        lastCatchTime = SimpleTimeMark.farPast()
    }

    private fun tryAddItem(internalName: NEUInternalName, amount: Int, command: Boolean) {
        if (!FishingAPI.isFishing(checkRodInHand = false)) return
        if (!isAllowedItem(internalName)) {
            ChatUtils.debug("Ignored non-fishing item pickup: $internalName'")
            return
        }

        tracker.addItem(internalName, amount, command)
        addCatch()
    }

    private fun isAllowedItem(internalName: NEUInternalName) = itemCategories.any { internalName in it.value }

    @SubscribeEvent
    fun onBobberThrow(event: FishingBobberCastEvent) {
        tracker.firstUpdate()
    }

    fun resetCommand() {
        tracker.resetCommand()
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled && !LorenzUtils.inKuudraFight
}
