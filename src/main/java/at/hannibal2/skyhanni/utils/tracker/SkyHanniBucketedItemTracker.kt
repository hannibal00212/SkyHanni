package at.hannibal2.skyhanni.utils.tracker

import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage
import at.hannibal2.skyhanni.data.ItemAddManager
import at.hannibal2.skyhanni.data.TrackerManager
import at.hannibal2.skyhanni.events.ItemAddEvent
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.sortedDesc
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.KeyboardManager
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.RenderableUtils.addButton
import at.hannibal2.skyhanni.utils.renderables.Searchable
import at.hannibal2.skyhanni.utils.renderables.toSearchable
import kotlin.time.Duration.Companion.seconds

@Suppress("SpreadOperator")
class SkyHanniBucketedItemTracker<E : Enum<E>, BucketedData : BucketedItemTrackerData<E>>(
    name: String,
    createNewSession: () -> BucketedData,
    getStorage: (ProfileSpecificStorage) -> BucketedData,
    drawDisplay: (BucketedData) -> List<Searchable>,
    vararg extraStorage: Pair<DisplayMode, (ProfileSpecificStorage) -> BucketedData>,
) : SkyHanniTracker<BucketedData>(name, createNewSession, getStorage, *extraStorage, drawDisplay = drawDisplay) {

    companion object {
        val SKYBLOCK_COIN = NeuInternalName.SKYBLOCK_COIN
    }

    fun addCoins(bucket: E, coins: Int) {
        addItem(bucket, SKYBLOCK_COIN, coins)
    }

    private fun getSelectedBucket(): E? {
        var selectedBucket: E? = null
        modify { data ->
            selectedBucket = data.selectedBucket
        }
        return selectedBucket
    }

    fun ItemAddEvent.addItemFromEvent() {
        var bucket: E? = null
        modify { data ->
            bucket = data.selectedBucket
        }
        val selectedBucket: E = bucket ?: run {
            ChatUtils.userError(
                "No bucket selected for §b$name§c.\nSelect one in the §b$name §cGUI, then try again.",
            )
            cancel()
            return
        }

        modify {
            it.addItem(selectedBucket, internalName, amount)
        }
        if (source == ItemAddManager.Source.COMMAND) {
            TrackerManager.commandEditTrackerSuccess = true
            ChatUtils.chat(
                "Added ${internalName.itemName} §e$amount§7x to ($selectedBucket§7)",
            )
        }
    }

    fun addItem(bucket: E, internalName: NeuInternalName, amount: Int) {
        modify {
            it.addItem(bucket, internalName, amount)
        }
        getSharedTracker()?.let {
            val totalProp = it.get(DisplayMode.TOTAL).getSelectedBucketItems().getOrPut(internalName) {
                ItemTrackerData.TrackedItem()
            }
            val sessionProp = it.get(DisplayMode.SESSION).getSelectedBucketItems().getOrPut(internalName) {
                ItemTrackerData.TrackedItem()
            }
            sessionProp.hidden = totalProp.hidden
        }
        handlePossibleRareDrop(internalName, amount)
    }

    fun addBucketSelector(
        lists: MutableList<Searchable>,
        data: BucketedData,
        sourceStringPrefix: String,
        nullBucketLabel: String = "All",
    ) {
        if (isInventoryOpen()) {
            lists.addButton(
                prefix = "§7$sourceStringPrefix: ",
                getName = data.selectedBucket?.toString() ?: nullBucketLabel,
                onChange = {
                    // We need to make sure the selected bucket syncs with the shared tracker
                    val newBucket: E? = data.selectNextSequentialBucket()
                    modifyEachMode { it.selectedBucket = newBucket }
                },
            )
        }
    }

    fun drawItems(
        data: BucketedData,
        filter: (NeuInternalName) -> Boolean,
        lists: MutableList<Searchable>,
    ): Double {
        var profit = 0.0
        val dataItems = data.getSelectedBucketItems()
        val items = mutableMapOf<NeuInternalName, Long>()
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
            val itemProfit = data.getSelectedBucketItems()[internalName] ?: error("Item not found for $internalName")

            val amount = itemProfit.totalAmount
            val displayAmount = if (internalName == SKYBLOCK_COIN) itemProfit.timesGained else amount

            val cleanName = if (internalName == SKYBLOCK_COIN) {
                data.getCoinName(data.selectedBucket, itemProfit)
            } else {
                internalName.itemName
            }

            val priceFormat = price.shortFormat()
            val hidden = itemProfit.hidden
            val newDrop = itemProfit.lastTimeUpdated.passedSince() < 10.seconds && config.showRecentDrops
            val numberColor = if (newDrop) "§a§l" else "§7"

            var displayName = if (hidden) {
                "§8§m" + cleanName.removeColor(keepFormatting = true).replace("§r", "")
            } else cleanName
            displayName = " $numberColor${displayAmount.addSeparators()}x $displayName§7: §6$priceFormat"

            pos++
            if (limitList.enabled.get()) {
                if (pos > limitList.alwaysShowBest.get()) {
                    if (price < limitList.minPrice.get() * 1000) {
                        hiddenItemTexts += displayName
                        continue
                    }
                }
            }

            val lore = buildLore(data, itemProfit, hidden, newDrop, internalName)
            val renderable = if (isInventoryOpen()) Renderable.clickAndHover(
                displayName, lore,
                onClick = {
                    if (KeyboardManager.isModifierKeyDown()) {
                        data.removeItem(data.selectedBucket, internalName)
                        ChatUtils.chat(
                            "Removed $cleanName §efrom $name" +
                                if (data.selectedBucket != null) " (${data.selectedBucket})"
                                else "",
                        )
                    } else {
                        modify {
                            it.toggleItemHide(data.selectedBucket, internalName)
                        }
                    }
                    update()

                },
            ) else Renderable.string(displayName)

            lists.add(renderable.toSearchable(name))
        }
        if (hiddenItemTexts.size > 0) {
            val text = Renderable.hoverTips(" §7${hiddenItemTexts.size} cheap items are hidden.", hiddenItemTexts).toSearchable()
            lists.add(text)
        }

        return profit
    }

    private fun buildLore(
        data: BucketedData,
        item: ItemTrackerData.TrackedItem,
        hidden: Boolean,
        newDrop: Boolean,
        internalName: NeuInternalName,
    ) = buildList {
        if (internalName == SKYBLOCK_COIN) {
            addAll(data.getCoinDescription(data.selectedBucket, item))
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
