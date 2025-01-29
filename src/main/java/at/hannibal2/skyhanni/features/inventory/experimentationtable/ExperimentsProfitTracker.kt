package at.hannibal2.skyhanni.features.inventory.experimentationtable

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.data.ClickType
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.ItemAddManager
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryUpdatedEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.ItemAddEvent
import at.hannibal2.skyhanni.events.ItemClickEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.features.inventory.experimentationtable.ExperimentationTableApi.claimMessagePattern
import at.hannibal2.skyhanni.features.inventory.experimentationtable.ExperimentationTableApi.enchantingExpPattern
import at.hannibal2.skyhanni.features.inventory.experimentationtable.ExperimentationTableApi.experienceBottleChatPattern
import at.hannibal2.skyhanni.features.inventory.experimentationtable.ExperimentationTableApi.experienceBottlePattern
import at.hannibal2.skyhanni.features.inventory.experimentationtable.ExperimentationTableApi.experimentRenewPattern
import at.hannibal2.skyhanni.features.inventory.experimentationtable.ExperimentationTableApi.experimentsDropPattern
import at.hannibal2.skyhanni.features.inventory.experimentationtable.ExperimentationTableApi.inventoriesPattern
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.CollectionUtils.addSearchString
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getNpcPriceOrNull
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getPrice
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.Searchable
import at.hannibal2.skyhanni.utils.renderables.toSearchable
import at.hannibal2.skyhanni.utils.tracker.ItemTrackerData
import at.hannibal2.skyhanni.utils.tracker.SkyHanniItemTracker
import com.google.gson.annotations.Expose
import net.minecraft.item.ItemStack
import kotlin.math.absoluteValue
import kotlin.time.Duration.Companion.milliseconds

@SkyHanniModule
object ExperimentsProfitTracker {

    private val config get() = SkyHanniMod.feature.inventory.experimentationTable.experimentsProfitTracker

    private val tracker = SkyHanniItemTracker(
        "Experiments Profit Tracker",
        { Data() },
        { it.experimentation.experimentsProfitTracker },
    ) { drawDisplay(it) }

    private val lastSplashes = mutableListOf<ItemStack>()
    private var lastSplashTime = SimpleTimeMark.farPast()
    private val lastBottlesInInventory = mutableMapOf<NeuInternalName, Int>()
    private val currentBottlesInInventory = mutableMapOf<NeuInternalName, Int>()

    class Data : ItemTrackerData() {
        override fun resetItems() {
            experimentsDone = 0L
            xpGained = 0L
            bitCost = 0L
            startCost = 0L
        }

        override fun getDescription(timesGained: Long): List<String> {
            val percentage = timesGained.toDouble() / experimentsDone
            val dropRate = LorenzUtils.formatPercentage(percentage.coerceAtMost(1.0))
            return listOf(
                "§7Dropped §e${timesGained.addSeparators()} §7times.",
                "§7Your drop rate: §c$dropRate.",
            )
        }

        override fun getCoinName(item: TrackedItem) = ""

        override fun getCoinDescription(item: TrackedItem) = listOf<String>()

        @Expose
        var experimentsDone = 0L

        @Expose
        var xpGained = 0L

        @Expose
        var bitCost = 0L

        @Expose
        var startCost = 0L
    }

    @HandleEvent
    fun onItemAdd(event: ItemAddEvent) {
        if (!isEnabled() || event.source != ItemAddManager.Source.COMMAND) return

        tracker.addItem(event.internalName, event.amount, command = true)
    }

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        if (!isEnabled()) return

        val message = event.message.removeColor()
        if (claimMessagePattern.matches(message) && ExperimentMessages.DONE.isSelected()) {
            event.blockedReason = "CLAIM_MESSAGE"
        }

        experimentsDropPattern.matchMatcher(message) {
            event.handleDrop(group("reward"))
            return
        }

        experimentRenewPattern.matchMatcher(message) {
            val increments = mapOf(1 to 150, 2 to 300, 3 to 500)
            tracker.modify {
                it.bitCost += increments.getValue(group("current").toInt())
            }
        }
    }

    private fun SkyHanniChatEvent.handleDrop(reward: String) {
        blockedReason = when {
            enchantingExpPattern.matches(reward) && ExperimentMessages.EXPERIENCE.isSelected() -> "EXPERIENCE_DROP"
            experienceBottleChatPattern.matches(reward) && ExperimentMessages.BOTTLES.isSelected() -> "BOTTLE_DROP"
            listOf("Metaphysical Serum", "Experiment The Fish").contains(reward) && ExperimentMessages.MISC.isSelected() -> "MISC_DROP"
            ExperimentMessages.ENCHANTMENTS.isSelected() -> "ENCHANT_DROP"
            else -> ""
        }

        enchantingExpPattern.matchMatcher(reward) {
            tracker.modify {
                it.xpGained += group("amount").substringBefore(",").toInt() * 1000
            }
            return
        }

        val internalName = NeuInternalName.fromItemNameOrNull(reward) ?: return
        if (!experienceBottleChatPattern.matches(reward)) tracker.addItem(internalName, 1, false)
        else DelayedRun.runDelayed(100.milliseconds) { handleExpBottles(true) }
    }

    @HandleEvent
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (!isEnabled() ||
            InventoryUtils.openInventoryName() != "Bottles of Enchanting" ||
            !listOf(11, 12, 14, 15).contains(event.slotId)
        ) return
        val stack = event.slot?.stack ?: return

        val internalName = stack.getInternalName()
        if (internalName.isExpBottle()) {
            tracker.modify {
                it.startCost -= calculateBottlePrice(internalName)
            }
        }
    }

    @HandleEvent
    fun onItemClick(event: ItemClickEvent) {
        if (!isEnabled(checkDistanceToExperimentationTable = false)) return
        if (event.clickType != ClickType.RIGHT_CLICK) return
        val item = event.itemInHand ?: return
        val internalName = item.getInternalName()
        if (!internalName.isExpBottle()) return

        lastSplashTime = SimpleTimeMark.now()

        if (ExperimentationTableApi.inDistanceToTable(15.0)) {
            tracker.modify {
                it.startCost -= calculateBottlePrice(internalName)
            }
            DelayedRun.runDelayed(100.milliseconds) { handleExpBottles(false) }
        } else {
            lastSplashes.add(item)
        }
    }

    private fun NeuInternalName.isExpBottle() = experienceBottlePattern.matches(asString())

    @HandleEvent
    fun onInventoryUpdated(event: InventoryUpdatedEvent) {
        if (!isEnabled()) return

        if (inventoriesPattern.matches(event.inventoryName)) {
            var startCostTemp = 0
            for (item in lastSplashes) {
                startCostTemp += calculateBottlePrice(item.getInternalName())
            }
            lastSplashes.clear()
            tracker.modify {
                it.startCost -= startCostTemp
            }
            lastSplashTime = SimpleTimeMark.farPast()
        }

        handleExpBottles(false)
    }

    private fun calculateBottlePrice(internalName: NeuInternalName): Int {
        val price = internalName.getPrice()
        val npcPrice = internalName.getNpcPriceOrNull() ?: 0.0
        return npcPrice.coerceAtLeast(price).toInt()
    }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (!isEnabled()) return

        if (ExperimentationTableApi.getCurrentExperiment() != null) {
            tracker.modify {
                it.experimentsDone++
            }
        }
    }

    private fun drawDisplay(data: Data): List<Searchable> = buildList {
        addSearchString("§e§lExperiments Profit Tracker")
        val profit = tracker.drawItems(data, { true }, this) + data.startCost

        val experimentsDone = data.experimentsDone
        addSearchString("")
        addSearchString("§eExperiments Done: §a${experimentsDone.addSeparators()}")
        val startCostFormat = data.startCost.absoluteValue.shortFormat()
        val bitCostFormat = data.bitCost.shortFormat()
        add(
            Renderable.hoverTips(
                "§eTotal Cost: §c-$startCostFormat§e/§b-$bitCostFormat",
                listOf(
                    "§7You paid §c$startCostFormat §7coins and", "§b$bitCostFormat §7bits for starting",
                    "§7experiments.",
                ),
            ).toSearchable(),
        )
        add(tracker.addTotalProfit(profit, data.experimentsDone, "experiment"))
        addSearchString("§eTotal Enchanting Exp: §b${data.xpGained.shortFormat()}")

        tracker.addPriceFromButton(this)
    }

    init {
        tracker.initRenderer(
            { config.position },
            ExperimentationTableApi.superpairInventory,
        ) { isEnabled() }
    }

    @HandleEvent
    fun onIslandChange(event: IslandChangeEvent) {
        if (event.newIsland == IslandType.PRIVATE_ISLAND) {
            tracker.firstUpdate()
        }
    }

    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.register("shresetexperimentsprofittracker") {
            description = "Resets the Experiments Profit Tracker"
            category = CommandCategory.USERS_RESET
            callback { tracker.resetCommand() }
        }
    }

    private fun handleExpBottles(addToTracker: Boolean) {
        for (item in InventoryUtils.getItemsInOwnInventory()) {
            val internalName = item.getInternalNameOrNull() ?: continue
            if (internalName.asString() !in listOf("EXP_BOTTLE", "GRAND_EXP_BOTTLE", "TITANIC_EXP_BOTTLE")) continue
            currentBottlesInInventory.addOrPut(internalName, item.stackSize)
        }

        for ((internalName, amount) in currentBottlesInInventory) {
            val lastInInv = lastBottlesInInventory.getOrDefault(internalName, 0)
            if (lastInInv >= amount) {
                lastBottlesInInventory[internalName] = amount
                continue
            }

            if (lastInInv == 0) {
                lastBottlesInInventory[internalName] = amount
                if (addToTracker) tracker.addItem(internalName, amount, false)
                continue
            }

            lastBottlesInInventory[internalName] = amount
            if (addToTracker) tracker.addItem(internalName, amount - lastInInv, false)
        }
        currentBottlesInInventory.clear()
    }

    private fun ExperimentMessages.isSelected() = config.hideMessages.contains(this)

    private fun isEnabled(checkDistanceToExperimentationTable: Boolean = true) =
        IslandType.PRIVATE_ISLAND.isInIsland() && config.enabled &&
            (!checkDistanceToExperimentationTable || ExperimentationTableApi.inDistanceToTable(5.0))

}
