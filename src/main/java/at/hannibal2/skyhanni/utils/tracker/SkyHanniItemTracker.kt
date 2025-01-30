package at.hannibal2.skyhanni.utils.tracker

import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage
import at.hannibal2.skyhanni.data.TrackerManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.sortedDesc
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.ItemUtils.readableInternalName
import at.hannibal2.skyhanni.utils.KeyboardManager
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.SKYBLOCK_COIN
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.Searchable
import at.hannibal2.skyhanni.utils.renderables.toSearchable
import kotlin.time.Duration.Companion.seconds

@Suppress("SpreadOperator")
open class SkyHanniItemTracker<Data : ItemTrackerData>(
    name: String,
    createNewSession: () -> Data,
    getStorage: (ProfileSpecificStorage) -> Data,
    vararg extraStorage: Pair<DisplayMode, (ProfileSpecificStorage) -> Data>,
    drawDisplay: (Data) -> List<Searchable>,
) : SkyHanniTracker<Data>(name, createNewSession, getStorage, *extraStorage, drawDisplay = drawDisplay) {

    open fun addCoins(amount: Int, command: Boolean) {
        addItem(SKYBLOCK_COIN, amount, command)
    }

    open fun addItem(internalName: NeuInternalName, amount: Int, command: Boolean) {
        modify {
            it.addItem(internalName, amount, command)
        }
        getSharedTracker()?.let { sharedData ->
            val isHidden = sharedData.get(DisplayMode.TOTAL).items[internalName]?.hidden
            if (isHidden != null) sharedData.modify { it.items[internalName]?.hidden = isHidden }
        }

        if (command) {
            TrackerManager.commandEditTrackerSuccess = true
            val displayName = internalName.itemName
            if (amount > 0) {
                ChatUtils.chat("Manually added to $name: §r$displayName §7(${amount}x§7)")
            } else {
                ChatUtils.chat("Manually removed from $name: §r$displayName §7(${-amount}x§7)")
            }
            return
        }
        handlePossibleRareDrop(internalName, amount)
    }

    private fun NeuInternalName.getCleanName(
        data: Data,
        getCoinName: (ItemTrackerData.TrackedItem) -> String,
    ): String {
        val item = data.items[this] ?: error("Item not found for $this")
        return if (this == SKYBLOCK_COIN) getCoinName.invoke(item) else this.itemName
    }

    open fun drawItems(
        data: Data,
        filter: (NeuInternalName) -> Boolean,
        lists: MutableList<Searchable>,
        itemsAccessor: () -> Map<NeuInternalName, ItemTrackerData.TrackedItem> = { data.items },
        getCoinName: (ItemTrackerData.TrackedItem) -> String = { item -> item.timesGained.toString() },
        itemRemover: (NeuInternalName) -> Unit = { item ->
            modify {
                it.items.remove(item)
            }
            ChatUtils.chat("Removed ${item.getCleanName(data, getCoinName)} §efrom $name.")
        },
        itemHider: (NeuInternalName, Boolean) -> Unit = { item, currentlyHidden ->
            modify {
                it.items[item]?.hidden = !currentlyHidden
            }
        },
    ): Double {
        var profit = 0.0
        val items = mutableMapOf<NeuInternalName, Long>()
        val dataItems = itemsAccessor.invoke()
        for ((internalName, itemProfit) in dataItems) {
            if (!filter(internalName)) continue

            val amount = itemProfit.totalAmount
            val pricePer = if (internalName == SKYBLOCK_COIN) 1.0 else data.getCustomPricePer(internalName)
            val price = (pricePer * amount).toLong()
            val hidden = itemProfit.hidden

            if (isInventoryOpen() || !hidden) {
                items[internalName] = price
            }
            if (!hidden || !config.excludeHiddenItemsInPrice) {
                profit += price
            }
        }

        val limitList = config.hideCheapItems
        var pos = 0
        val hiddenItemTexts = mutableListOf<String>()
        for ((internalName, price) in items.sortedDesc()) {
            val itemProfit = dataItems[internalName] ?: error("Item not found for $internalName")

            val amount = itemProfit.totalAmount
            val displayAmount = if (internalName == SKYBLOCK_COIN) itemProfit.timesGained else amount

            val cleanName = internalName.getCleanName(data, getCoinName)

            val priceFormat = price.shortFormat()
            val hidden = itemProfit.hidden
            val newDrop = itemProfit.lastTimeUpdated.passedSince() < 10.seconds && config.showRecentDrops
            val numberColor = if (newDrop) "§a§l" else "§7"

            val formattedName = cleanName.removeColor(keepFormatting = true).replace("§r", "")
            val displayName = if (hidden) "§8§m$formattedName" else cleanName
            val listFormat = " $numberColor${displayAmount.addSeparators()}x $displayName§7: §6$priceFormat"

            pos++
            if (limitList.enabled.get()) {
                if (pos > limitList.alwaysShowBest.get()) {
                    if (price < limitList.minPrice.get() * 1000) {
                        hiddenItemTexts += listFormat
                        continue
                    }
                }
            }

            val lore = buildLore(data, itemProfit, hidden, newDrop, internalName)
            val renderable = if (isInventoryOpen()) Renderable.clickAndHover(
                listFormat, lore,
                onClick = {
                    if (KeyboardManager.isModifierKeyDown()) itemRemover.invoke(internalName)
                    else itemHider.invoke(internalName, hidden)
                    update()
                },
            ) else Renderable.string(listFormat)

            lists.add(renderable.toSearchable(formattedName))
        }
        if (hiddenItemTexts.size > 0) {
            val text = Renderable.hoverTips(" §7${hiddenItemTexts.size} cheap items are hidden.", hiddenItemTexts).toSearchable()
            lists.add(text)
        }

        return profit
    }

    private fun buildLore(
        data: Data,
        item: ItemTrackerData.TrackedItem,
        hidden: Boolean,
        newDrop: Boolean,
        internalName: NeuInternalName,
    ) = buildList {
        if (internalName == SKYBLOCK_COIN) {
            addAll(data.getCoinDescription(item))
        } else {
            addAll(data.getDescription(item.timesGained))
        }
        add("")
        if (newDrop) {
            add("§aYou obtained this item recently.")
            add("")
        }
        add("§eClick to " + (if (hidden) "show" else "hide") + "!")
        add("§eControl + Click to remove this item!")

        add("")
        add("§7Use §e/shedittracker ${internalName.readableInternalName} <amount>")
        add("§7to edit the number.")
        add("§7Use negative numbers to remove items.")

        if (LorenzUtils.debug) {
            add("")
            add("§7$internalName")
        }
    }

    fun addTotalProfit(profit: Double, totalAmount: Long, action: String): Searchable {
        val profitFormat = profit.toLong().addSeparators()
        val profitPrefix = if (profit < 0) "§c" else "§6"

        val tips = if (totalAmount > 0) {
            val profitPerCatch = profit / totalAmount
            val profitPerCatchFormat = profitPerCatch.shortFormat()
            listOf("§7Profit per $action: $profitPrefix$profitPerCatchFormat")
        } else emptyList()

        val text = "§eTotal Profit: $profitPrefix$profitFormat coins"
        return Renderable.hoverTips(text, tips).toSearchable()
    }
}
