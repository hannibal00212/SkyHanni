package at.hannibal2.skyhanni.utils.tracker

import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage
import at.hannibal2.skyhanni.data.ItemAddManager
import at.hannibal2.skyhanni.data.TrackerManager
import at.hannibal2.skyhanni.events.ItemAddEvent
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.SKYBLOCK_COIN
import at.hannibal2.skyhanni.utils.renderables.RenderableUtils.addButton
import at.hannibal2.skyhanni.utils.renderables.Searchable

@Suppress("SpreadOperator")
class SkyHanniBucketedItemTracker<E : Enum<E>, BucketedData : BucketedItemTrackerData<E>>(
    name: String,
    createNewSession: () -> BucketedData,
    getStorage: (ProfileSpecificStorage) -> BucketedData,
    drawDisplay: (BucketedData) -> List<Searchable>,
    vararg extraStorage: Pair<DisplayMode, (ProfileSpecificStorage) -> BucketedData>,
) : SkyHanniItemTracker<BucketedData>(name, createNewSession, getStorage, *extraStorage, drawDisplay = drawDisplay) {

    @Deprecated("Use addCoins(bucket, coins) instead", ReplaceWith("addCoins(bucket, coins)"))
    override fun addCoins(amount: Int, command: Boolean) =
        throw UnsupportedOperationException("Use addCoins(bucket, coins) instead")

    fun addCoins(bucket: E, coins: Int) {
        addItem(bucket, SKYBLOCK_COIN, coins)
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

    @Deprecated(
        "Use addItem(bucket, internalName, amount) instead",
        ReplaceWith("addItem(bucket, internalName, amount)"),
    )
    override fun addItem(internalName: NeuInternalName, amount: Int, command: Boolean) =
        throw UnsupportedOperationException("Use addItem(bucket, internalName, amount) instead")

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

    override fun drawItems(
        data: BucketedData,
        filter: (NeuInternalName) -> Boolean,
        lists: MutableList<Searchable>,
        itemsAccessor: () -> Map<NeuInternalName, ItemTrackerData.TrackedItem>,
        getCoinName: (ItemTrackerData.TrackedItem) -> String,
        itemRemover: (NeuInternalName, String) -> Unit,
        itemHider: (NeuInternalName, Boolean) -> Unit,
        getLoreList: (NeuInternalName, ItemTrackerData.TrackedItem) -> List<String>,
    ): Double = super.drawItems(
        data = data,
        filter = filter,
        lists = lists,
        itemsAccessor = { data.getSelectedBucketItems() },
        getCoinName = { item ->
            data.getCoinName(data.selectedBucket, item)
        },
        itemRemover = { internalName, cleanName ->
            modify {
                it.removeItem(data.selectedBucket, internalName)
            }
            ChatUtils.chat("Removed $cleanName §efrom $name.")
        },
        itemHider = { internalName, _ ->
            modify {
                it.toggleItemHide(data.selectedBucket, internalName)
            }
        },
        getLoreList = { internalName, item ->
            val selectedBucket = data.selectedBucket
            if (internalName == SKYBLOCK_COIN) data.getCoinDescription(selectedBucket, item)
            else data.getDescription(selectedBucket, item.timesGained)
        },
    )
}
