package at.hannibal2.skyhanni.features.mining.crystalhollows

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.event.HandleEvent.Companion.HIGH
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.ItemAddManager
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.ItemAddEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.mining.CrystalNucleusLootEvent
import at.hannibal2.skyhanni.features.mining.crystalhollows.CrystalNucleusApi.EPIC_BAL_ITEM
import at.hannibal2.skyhanni.features.mining.crystalhollows.CrystalNucleusApi.JUNGLE_KEY_ITEM
import at.hannibal2.skyhanni.features.mining.crystalhollows.CrystalNucleusApi.LEGENDARY_BAL_ITEM
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.addSearchString
import at.hannibal2.skyhanni.utils.ConditionalUtils.onToggle
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getPrice
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderDisplayHelper
import at.hannibal2.skyhanni.utils.StringUtils
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.Searchable
import at.hannibal2.skyhanni.utils.renderables.toSearchable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import at.hannibal2.skyhanni.utils.tracker.ItemTrackerData
import at.hannibal2.skyhanni.utils.tracker.SkyHanniItemTracker
import com.google.gson.annotations.Expose

@SkyHanniModule
object CrystalNucleusTracker {
    private val config get() = SkyHanniMod.feature.mining.crystalNucleusTracker
    private val patternGroup = RepoPattern.group("mining.crystalnucleus.tracker")

    /**
     * REGEX-TEST: §b[MVP§r§2+§r§b] oBlazin§r§f §r§ehas obtained §r§a§r§7[Lvl 1] §r§6Bal§r§e!
     * REGEX-TEST: §6[MVP§r§2++§r§b] oBlazin§r§f §r§ehas obtained §r§a§r§7[Lvl 1] §r§6Bal§r§e!
     * REGEX-TEST: oBlazin§r§f §r§ehas obtained §r§a§r§7[Lvl 1] §r§6Bal§r§e!
     * REGEX-TEST: §c[§fYOUTUBE§c] oBlazin§r§f §r§ehas obtained §r§a§r§7[Lvl 1] §r§6Bal§r§e!
     */
    @Suppress("MaxLineLength")
    private val balObtainedPattern by patternGroup.pattern(
        "bal.obtained",
        "(?:(?:§.)*\\[.*(?:§.)*\\+*(?:§.)*\\] )?(?<player>.*)§r§f §r§ehas obtained §r§a§r§7\\[Lvl 1\\] §r§(?<raritycolor>[65])Bal§r§e!",
    )

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

    @HandleEvent(onlyOnSkyblock = true)
    fun onChat(event: SkyHanniChatEvent) {
        balObtainedPattern.matchMatcher(event.message) {
            if (!group("player").equals(LorenzUtils.getPlayerName(), ignoreCase = true)) return@matchMatcher

            val item = when (group("raritycolor")) {
                "6" -> LEGENDARY_BAL_ITEM
                "5" -> EPIC_BAL_ITEM
                else -> return@matchMatcher
            }
            tracker.modify {
                it.addItem(item, amount = 1, false)
            }
        }
    }

    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.register("shresetcrystalnucleustracker") {
            description = "Resets the Crystal Nucleus Tracker."
            category = CommandCategory.USERS_RESET
            callback { tracker.resetCommand() }
        }
    }

    @HandleEvent(priority = HIGH)
    fun onCrystalNucleusLoot(event: CrystalNucleusLootEvent) {
        tracker.modify {
            it.runsCompleted++
        }
        for ((internalName, amount) in event.loot) {
            tracker.addItem(internalName, amount, false)
        }
    }

    @HandleEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        config.professorUsage.onToggle(tracker::update)
    }

    private fun drawDisplay(data: Data): List<Searchable> = buildList {
        addSearchString("§e§lCrystal Nucleus Profit Tracker")

        val runsCompleted = data.runsCompleted
        if (runsCompleted > 0) {
            var profit = tracker.drawItems(data, { true }, this)
            val jungleKeyCost: Double = JUNGLE_KEY_ITEM.getPrice() * runsCompleted
            profit -= jungleKeyCost
            val jungleKeyCostFormat = jungleKeyCost.shortFormat()
            add(
                Renderable.hoverTips(
                    " §7${runsCompleted}x §5Jungle Key§7: §c-$jungleKeyCostFormat",
                    tips = listOf(
                        "§7You lost §c$jungleKeyCostFormat §7of total profit",
                        "§7due to §5Jungle Keys§7.",
                    ),
                ).toSearchable("Jungle Key"),
            )

            val usesApparatus = CrystalNucleusApi.usesApparatus()
            val partsCost = CrystalNucleusApi.getPrecursorRunPrice()
            val totalSapphireCost: Double = partsCost * runsCompleted
            val rawConfigString = config.professorUsage.get().toString()
            val usageString = if (usesApparatus) StringUtils.pluralize(
                runsCompleted.toInt(),
                rawConfigString,
                "§5Precursor Apparatuses",
            )
            else rawConfigString
            val usageTotal = if (usesApparatus) runsCompleted else runsCompleted * 6

            profit -= totalSapphireCost
            val totalSapphireCostFormat = totalSapphireCost.shortFormat()
            add(
                Renderable.hoverTips(
                    " §7${usageTotal}x $usageString§7: §c-$totalSapphireCostFormat",
                    tips = listOf(
                        "§7You lost §c$totalSapphireCostFormat §7of total profit",
                        "§7due to $usageString§7.",
                    ),
                ).toSearchable(usageString.removeColor()),
            )

            add(
                Renderable.hoverTips(
                    "§7Runs completed: §e${runsCompleted.addSeparators()}",
                    tips = listOf("§7You completed §e${runsCompleted.addSeparators()} §7Crystal Nucleus Runs."),
                ).toSearchable(),
            )

            add(tracker.addTotalProfit(profit, data.runsCompleted, "run"))
        } else {
            addSearchString("§7Do a run to start tracking!")
        }

        tracker.addPriceFromButton(this)
    }

    @HandleEvent
    fun onItemAdd(event: ItemAddEvent) {
        if (!isEnabled()) return

        if (event.source == ItemAddManager.Source.COMMAND) {
            tracker.addItem(event.internalName, event.amount, command = true)
        }
    }

    init {
        RenderDisplayHelper(
            outsideInventory = true,
            inOwnInventory = true,
            condition = { isEnabled() },
            onRender = {
                tracker.renderDisplay(config.position)
            },
        )
    }

    @HandleEvent
    fun onIslandChange(event: IslandChangeEvent) {
        if (event.newIsland == IslandType.CRYSTAL_HOLLOWS) {
            tracker.firstUpdate()
        }
    }

    private fun isAreaEnabled() = config.showOutsideNucleus || LorenzUtils.skyBlockArea == "Crystal Nucleus"
    private fun isEnabled() = config.enabled && IslandType.CRYSTAL_HOLLOWS.isInIsland() && isAreaEnabled()
}
