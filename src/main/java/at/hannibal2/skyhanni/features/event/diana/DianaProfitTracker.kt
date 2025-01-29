package at.hannibal2.skyhanni.features.event.diana

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.data.ElectionApi.getElectionYear
import at.hannibal2.skyhanni.data.ItemAddManager
import at.hannibal2.skyhanni.data.jsonobjects.repo.DianaDropsJson
import at.hannibal2.skyhanni.events.ItemAddEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.features.event.diana.DianaApi.isDianaSpade
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.addSearchString
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderDisplayHelper
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SkyBlockTime
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.Searchable
import at.hannibal2.skyhanni.utils.renderables.toSearchable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import at.hannibal2.skyhanni.utils.tracker.ItemTrackerData
import at.hannibal2.skyhanni.utils.tracker.SkyHanniItemTracker
import at.hannibal2.skyhanni.utils.tracker.SkyHanniTracker
import com.google.gson.annotations.Expose

@SkyHanniModule
object DianaProfitTracker {

    private val config get() = SkyHanniMod.feature.event.diana.dianaProfitTracker
    private var allowedDrops = listOf<NeuInternalName>()

    private val patternGroup = RepoPattern.group("diana.chat")
    private val chatDugOutPattern by patternGroup.pattern(
        "burrow.dug",
        "(?:§eYou dug out a Griffin Burrow!|§eYou finished the Griffin burrow chain!) .*",
    )
    private val chatDugOutCoinsPattern by patternGroup.pattern(
        "coins",
        "§6§lWow! §r§eYou dug out §r§6(?<coins>.*) coins§r§e!",
    )

    private val tracker = SkyHanniItemTracker(
        "Diana Profit Tracker",
        { Data() },
        { it.diana.profitTracker },
        SkyHanniTracker.DisplayMode.MAYOR to {
            it.diana.profitTrackerPerElection.getOrPut(
                SkyBlockTime.now().getElectionYear(), ::Data,
            )
        },
    ) { drawDisplay(it) }

    class Data : ItemTrackerData() {

        override fun resetItems() {
            burrowsDug = 0
        }

        @Expose
        var burrowsDug: Long = 0

        override fun getDescription(timesGained: Long): List<String> {
            val percentage = timesGained.toDouble() / burrowsDug
            val perBurrow = LorenzUtils.formatPercentage(percentage.coerceAtMost(1.0))

            return listOf(
                "§7Dropped §e${timesGained.addSeparators()} §7times.",
                "§7Your drop chance per burrow: §c$perBurrow",
            )
        }

        override fun getCoinName(item: TrackedItem) = "§6Dug Out Coins"

        override fun getCoinDescription(item: TrackedItem): List<String> {
            val burrowDugCoinsFormat = item.totalAmount.shortFormat()
            return listOf(
                "§7Digging treasures gave you",
                "§6$burrowDugCoinsFormat coins §7in total.",
            )
        }
    }

    private fun drawDisplay(data: Data): List<Searchable> = buildList {
        addSearchString("§e§lDiana Profit Tracker")

        val profit = tracker.drawItems(data, { true }, this)

        val treasureCoins = data.burrowsDug
        add(
            Renderable.hoverTips(
                "§7Burrows dug: §e${treasureCoins.addSeparators()}",
                listOf("§7You dug out griffin burrows §e${treasureCoins.addSeparators()} §7times."),
            ).toSearchable(),
        )

        add(tracker.addTotalProfit(profit, data.burrowsDug, "burrow"))

        tracker.addPriceFromButton(this)
    }

    @HandleEvent
    fun onItemAdd(event: ItemAddEvent) {
        if (!(DianaApi.isDoingDiana() && config.enabled)) return

        tryAddItem(event.internalName, event.amount, event.source == ItemAddManager.Source.COMMAND)
    }

    private fun tryAddItem(internalName: NeuInternalName, amount: Int, command: Boolean) {
        if (!isAllowedItem(internalName) && internalName != NeuInternalName.SKYBLOCK_COIN) {
            ChatUtils.debug("Ignored non-diana item pickup: '$internalName'")
            return
        }

        tracker.addItem(internalName, amount, command)
    }

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        val message = event.message
        if (chatDugOutPattern.matches(message)) {
            BurrowApi.lastBurrowRelatedChatMessage = SimpleTimeMark.now()
            tracker.modify {
                it.burrowsDug++
            }
            tryHide(event)
        }
        chatDugOutCoinsPattern.matchMatcher(message) {
            BurrowApi.lastBurrowRelatedChatMessage = SimpleTimeMark.now()
            tryAddItem(NeuInternalName.SKYBLOCK_COIN, group("coins").formatInt(), command = false)
            tryHide(event)
        }

        if (message == "§6§lRARE DROP! §r§eYou dug out a §r§9Griffin Feather§r§e!" ||
            message == "§eFollow the arrows to find the §r§6treasure§r§e!"
        ) {
            BurrowApi.lastBurrowRelatedChatMessage = SimpleTimeMark.now()
            tryHide(event)
        }
    }

    private fun tryHide(event: SkyHanniChatEvent) {
        if (SkyHanniMod.feature.chat.filterType.diana) {
            event.blockedReason = "diana_chain_or_drops"
        }
    }

    init {
        RenderDisplayHelper(
            outsideInventory = true,
            inOwnInventory = true,
            condition = { config.enabled },
            onRender = {
                val spadeInHand = InventoryUtils.getItemInHand()?.isDianaSpade ?: false
                if (!DianaApi.isDoingDiana() && !spadeInHand) return@RenderDisplayHelper
                if (spadeInHand) {
                    tracker.firstUpdate()
                }

                tracker.renderDisplay(config.position)
            },
        )
    }

    private fun isAllowedItem(internalName: NeuInternalName): Boolean = internalName in allowedDrops

    @HandleEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        allowedDrops = event.getConstant<DianaDropsJson>("DianaDrops").dianaDrops
    }

    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.register("shresetdianaprofittracker") {
            description = "Resets the Diana Profit Tracker"
            category = CommandCategory.USERS_RESET
            callback { tracker.resetCommand() }
        }
    }

    private val migrationMapping by lazy {
        mapOf(
            "dianaProfitTracker" to "profitTracker",
            "dianaProfitTrackerPerElectionSeason" to "profitTrackerPerElection",
            "mythologicalMobTrackerPerElectionSeason" to "mythologicalMobTrackerPerElection",
        )
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        migrationMapping.forEach { (old, new) ->
            event.move(70, "#profile.diana.$old", "#profile.diana.$new")
        }
    }
}
