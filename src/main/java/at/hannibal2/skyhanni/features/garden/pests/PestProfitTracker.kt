package at.hannibal2.skyhanni.features.garden.pests

import at.hannibal2.skyhanni.SkyHanniMod
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
import at.hannibal2.skyhanni.utils.CollectionUtils.addSearchString
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.Searchable
import at.hannibal2.skyhanni.utils.renderables.toSearchable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import at.hannibal2.skyhanni.utils.tracker.ItemTrackerData
import at.hannibal2.skyhanni.utils.tracker.SkyHanniItemTracker
import com.google.gson.annotations.Expose
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
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

    private var lastPestKillTime = SimpleTimeMark.farPast()
    private val tracker = SkyHanniItemTracker(
        "Pest Profit Tracker",
        { Data() },
        { it.garden.pestProfitTracker },
    ) { drawDisplay(it) }

    class Data : ItemTrackerData() {
        override fun resetItems() {
            totalPestsKills = 0L
        }

        override fun getDescription(timesGained: Long): List<String> {
            val percentage = timesGained.toDouble() / totalPestsKills
            val dropRate = LorenzUtils.formatPercentage(percentage.coerceAtMost(1.0))
            return listOf(
                "§7Dropped §e${timesGained.addSeparators()} §7times.",
                "§7Your drop rate: §c$dropRate.",
            )
        }

        override fun getCoinName(item: TrackedItem) = "§6Pest Kill Coins"

        override fun getCoinDescription(item: TrackedItem): List<String> {
            val pestsCoinsFormat = item.totalAmount.shortFormat()
            return listOf(
                "§7Killing pests gives you coins.",
                "§7You got §6$pestsCoinsFormat coins §7that way.",
            )
        }

        @Expose
        var totalPestsKills = 0L
    }

    @SubscribeEvent
    fun onItemAdd(event: ItemAddEvent) {
        if (!isEnabled()) return

        val internalName = event.internalName
        if (event.source == ItemAddManager.Source.COMMAND) {
            tryAddItem(internalName, event.amount, command = true)
        }
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!isEnabled()) return
        PestAPI.pestDeathChatPattern.matchMatcher(event.message) {
            val amount = group("amount").toInt()
            val internalName = NEUInternalName.fromItemNameOrNull(group("item")) ?: return

            tryAddItem(internalName, amount, command = false)
            addKill()
            if (config.hideChat) event.blockedReason = "pest_drop"
        }
        pestRareDropPattern.matchMatcher(event.message) {
            val internalName = NEUInternalName.fromItemNameOrNull(group("item")) ?: return

            tryAddItem(internalName, 1, command = false)
            // pests always have guaranteed loot, therefore there's no need to add kill here
        }
    }

    private fun tryAddItem(internalName: NEUInternalName, amount: Int, command: Boolean) {
        tracker.addItem(internalName, amount, command)
    }

    private fun addKill() {
        tracker.modify {
            it.totalPestsKills++
        }
        lastPestKillTime = SimpleTimeMark.now()
    }

    private fun drawDisplay(data: Data): List<Searchable> = buildList {
        addSearchString("§e§lPest Profit Tracker")
        val profit = tracker.drawItems(data, { true }, this)

        val pestsKilled = data.totalPestsKills
        add(
            Renderable.hoverTips(
                "§7Pests killed: §e${pestsKilled.addSeparators()}",
                listOf("§7You killed pests §e${pestsKilled.addSeparators()} §7times."),
            ).toSearchable(),
        )
        add(tracker.addTotalProfit(profit, data.totalPestsKills, "kill"))

        tracker.addPriceFromButton(this)
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent) {
        if (!isEnabled()) return
        if (GardenAPI.isCurrentlyFarming()) return
        if (lastPestKillTime.passedSince() > config.timeDisplayed.seconds && !PestAPI.hasVacuumInHand()) return

        tracker.renderDisplay(config.position)
    }

    @SubscribeEvent
    fun onPurseChange(event: PurseChangeEvent) {
        if (!isEnabled()) return
        val coins = event.oldCoins
        if (coins > 1000) return
        if (event.reason == PurseChangeCause.GAIN_MOB_KILL && lastPestKillTime.passedSince() < 2.seconds) {
            tryAddItem(NEUInternalName.SKYBLOCK_COIN, coins.toInt(), command = false)
        }
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
}
