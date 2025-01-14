package at.hannibal2.skyhanni.utils.tracker

import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.tracker.ItemTrackerData.TrackedItem
import com.google.gson.annotations.Expose
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl

abstract class BucketedItemTrackerData<E : Enum<E>> : TrackerData() {

    abstract fun resetItems()

    abstract fun getDescription(timesGained: Long): List<String>

    abstract fun getCoinName(bucket: E?, item: TrackedItem): String

    abstract fun getCoinDescription(bucket: E?, item: TrackedItem): List<String>

    open fun getCustomPricePer(internalName: NEUInternalName) = SkyHanniTracker.getPricePer(internalName)

    abstract fun E.isBucketSelectable(): Boolean

    override fun reset() {
        bucketedItems.clear()
        selectedBucket = null
        resetItems()
    }

    fun addItem(bucket: E, internalName: NEUInternalName, stackSize: Int) {
        val bucketMap = bucketedItems.getOrPut(bucket) { HashMap() }
        val item = bucketMap.getOrPut(internalName) { TrackedItem() }

        item.timesGained++
        item.totalAmount += stackSize
        item.lastTimeUpdated = SimpleTimeMark.now()
    }

    fun removeItem(bucket: E?, internalName: NEUInternalName) {
        bucket?.let {
            bucketedItems[bucket]?.remove(internalName)
        } ?: bucketedItems.forEach {
            it.value.remove(internalName)
        }
    }

    fun toggleItemHide(bucket: E?, internalName: NEUInternalName) {
        bucket?.let {
            bucketedItems[bucket]?.get(internalName)?.let { it.hidden = !it.hidden }
        } ?: bucketedItems.forEach {
            it.value[internalName]?.hidden = !it.value[internalName]?.hidden!!
        }
    }

    private val buckets: Array<E> by lazy {
        @Suppress("UNCHECKED_CAST")
        selectedBucket?.javaClass?.enumConstants
            ?: (this.javaClass.genericSuperclass as? ParameterizedTypeImpl)?.actualTypeArguments?.firstOrNull()?.let { type ->
                (type as? Class<E>)?.enumConstants
            } ?: ErrorManager.skyHanniError(
            "Unable to retrieve enum constants for E in BucketedItemTrackerData",
            "selectedBucket" to selectedBucket,
            "dataClass" to this.javaClass.superclass.name,
        )
    }

    @Expose
    var selectedBucket: E? = null
    @Expose
    private val bucketedItems: MutableMap<E, MutableMap<NEUInternalName, TrackedItem>> = HashMap()

    fun selectNextSequentialBucket(): E? {
        selectedBucket = if (selectedBucket == null) buckets.first { it.isBucketSelectable() }
        else selectedBucket?.let { sb ->
            buckets.filter { it.ordinal > sb.ordinal && it.isBucketSelectable() }.minByOrNull { it.ordinal }
        }
        return selectedBucket
    }

    private fun getBucketItems(bucket: E) = bucketedItems[bucket]?.toMutableMap() ?: HashMap()
    fun getSelectedBucketItems() = selectedBucket?.let { getBucketItems(it) } ?: flattenBucketsItems()
    private fun flattenBucketsItems(): MutableMap<NEUInternalName, TrackedItem> {
        val flatMap: MutableMap<NEUInternalName, TrackedItem> = HashMap()
        buckets.distinct().forEach { bucket ->
            getBucketItems(bucket).filter { !it.value.hidden }.entries.distinctBy { it.key }.forEach { (key, value) ->
                flatMap.merge(key, value) { existing, new ->
                    existing.copy(
                        hidden = false,
                        totalAmount = existing.totalAmount + new.totalAmount,
                        timesGained = existing.timesGained + new.timesGained,
                        lastTimeUpdated = maxOf(existing.lastTimeUpdated, new.lastTimeUpdated),
                    )
                }
            }
        }
        return flatMap.toMutableMap()
    }
}
