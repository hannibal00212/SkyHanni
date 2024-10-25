package at.hannibal2.skyhanni.features.mining.crystalhollows

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.event.HandleEvent.Companion.HIGH
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.mining.CrystalNucleusLootEvent
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryAPI
import at.hannibal2.skyhanni.features.inventory.patternGroup
import at.hannibal2.skyhanni.features.mining.crystalhollows.CrystalNucleusProfitPer.jungleKeyItem
import at.hannibal2.skyhanni.features.mining.crystalhollows.CrystalNucleusProfitPer.robotPartItems
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.addSearchString
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getPrice
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.asInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.Searchable
import at.hannibal2.skyhanni.utils.renderables.toSearchable
import at.hannibal2.skyhanni.utils.tracker.ItemTrackerData
import at.hannibal2.skyhanni.utils.tracker.SkyHanniItemTracker
import com.google.gson.annotations.Expose
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object CrystalNucleusTracker {
    private val config get() = SkyHanniMod.feature.mining.crystalNucleusTracker

    /**
     * REGEX-TEST: §b[MVP§r§2+§r§b] oBlazin§r§f §r§ehas obtained §r§a§r§7[Lvl 1] §r§6Bal§r§e!
     * REGEX-TEST: §6[MVP§r§2++§r§b] oBlazin§r§f §r§ehas obtained §r§a§r§7[Lvl 1] §r§6Bal§r§e!
     * REGEX-TEST: oBlazin§r§f §r§ehas obtained §r§a§r§7[Lvl 1] §r§6Bal§r§e!
     * REGEX-TEST: §c[§fYOUTUBE§c] oBlazin§r§f §r§ehas obtained §r§a§r§7[Lvl 1] §r§6Bal§r§e!
     */
    @Suppress("MaxLineLength")
    private val balObtainedPattern by patternGroup.pattern(
        "bal.obtained",
        "(?:(?:§.)*\\[.*(?:§.)*\\+*(?:§.)*\\] )?(?<player>.*)§r§f §r§ehas obtained §r§a§r§7\\[Lvl 1\\] §r§(?<raritycolor>[65])Bal§r§e!"
    )

    private val EPIC_BAL_ITEM by lazy { "BAL;3".asInternalName() }
    private val LEGENDARY_BAL_ITEM by lazy { "BAL;4".asInternalName() }

    private val tracker = SkyHanniItemTracker(
        "Crystal Nucleus Tracker",
        { Data() },
        { it.mining.crystalNucleusTracker },
    ) { drawDisplay(it) }

    class Data : ItemTrackerData() {
        override fun resetItems() {
            runsCompleted = 0L
        }

        override fun getDescription(timesGained: Long): List<String> {
            val percentage = timesGained.toDouble() / runsCompleted
            val dropRate = LorenzUtils.formatPercentage(percentage.coerceAtMost(1.0))
            return listOf(
                "§7Dropped §e${timesGained.addSeparators()} §7times.",
                "§7Your drop rate: §c$dropRate.",
            )
        }

        // No direct coin drops from nuc runs
        override fun getCoinName(item: TrackedItem) = ""
        override fun getCoinDescription(item: TrackedItem) = mutableListOf<String>()

        @Expose
        var runsCompleted = 0L
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!LorenzUtils.inSkyBlock) return
        balObtainedPattern.matchMatcher(event.message) {
            if (!group("player").equals(LorenzUtils.getPlayerName(), ignoreCase = true)) return@matchMatcher

            val item = when (group("raritycolor")) {
                "6" -> LEGENDARY_BAL_ITEM
                "5" -> EPIC_BAL_ITEM
                else -> return@matchMatcher
            }
            tracker.modify {
                it.addItem(item, 1, false)
            }
        }
    }

    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.register("shresetcrystalnucleustracker") {
            description = "Resets the Crystal Nucleus Tracker"
            category = CommandCategory.USERS_RESET
            callback { tracker.resetCommand() }
        }
    }

    @HandleEvent(priority = HIGH)
    fun onCrystalNucleusLoot(event: CrystalNucleusLootEvent) {
        addCompletedRun()
        for ((internalName, amount) in event.loot) {
            tracker.addItem(internalName, amount, false)
        }
    }

    private fun addCompletedRun() {
        tracker.modify {
            it.runsCompleted++
        }
    }

    private fun drawDisplay(data: Data): List<Searchable> = buildList {
        addSearchString("§e§lCrystal Nucleus Profit Tracker")
        var profit = tracker.drawItems(data, { true }, this)

        val runsCompleted = data.runsCompleted

        if (runsCompleted > 0) {
            val jungleKeyCost = jungleKeyItem.getPrice() * runsCompleted
            profit -= jungleKeyCost
            val jungleKeyCostFormat = jungleKeyCost.shortFormat()
            add(
                Renderable.hoverTips(
                    " §7${runsCompleted}x §5Jungle Key§7: §c-$jungleKeyCostFormat",
                    listOf(
                        "§7You lost §c$jungleKeyCostFormat §7of total profit",
                        "§7due to §5Jungle Keys§7."
                    ),
                ).toSearchable(),
            )

            val robotPartsCost = robotPartItems.sumOf { it.getPrice() } * runsCompleted
            profit -= robotPartsCost
            val robotPartsCostFormat = robotPartsCost.shortFormat()
            add(
                Renderable.hoverTips(
                    " §7${runsCompleted * 6}x §9Robot Parts§7: §c-$robotPartsCostFormat",
                    listOf(
                        "§7You lost §c$robotPartsCostFormat §7of total profit",
                        "§7due to §9Robot Parts§7."
                    ),
                ).toSearchable(),
            )

            add(
                Renderable.hoverTips(
                    "§7Runs completed: §e${runsCompleted.addSeparators()}",
                    listOf("§7You completed §e${runsCompleted.addSeparators()} §7Crystal Nucleus Runs."),
                ).toSearchable(),
            )

            add(tracker.addTotalProfit(profit, data.runsCompleted, "run"))
        }

        tracker.addPriceFromButton(this)
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent) {
        if (!isEnabled()) return
        tracker.renderDisplay(config.position)
    }

    @SubscribeEvent
    fun onIslandChange(event: IslandChangeEvent) {
        if (event.newIsland == IslandType.CRYSTAL_HOLLOWS) {
            tracker.firstUpdate()
        }
    }

    private fun isCfEnabled() = !config.hideInCf || !ChocolateFactoryAPI.inChocolateFactory
    private fun isNucEnabled() = config.showOutsideNucleus || LorenzUtils.skyBlockArea == "Crystal Nucleus"
    private fun isEnabled() = config.enabled && IslandType.CRYSTAL_HOLLOWS.isInIsland() && isNucEnabled() && isCfEnabled()
}
