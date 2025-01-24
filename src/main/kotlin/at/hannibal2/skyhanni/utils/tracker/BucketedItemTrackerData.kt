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
    private var selectedBucket: E? = null
    @Expose
    private val bucketedItems: MutableMap<E, MutableMap<NEUInternalName, TrackedItem>> = HashMap()

    private fun getBucket(bucket: E): MutableMap<NEUInternalName, TrackedItem> = bucketedItems[bucket]?.toMutableMap() ?: HashMap()
    private fun getPoppedBuckets(): MutableList<E> = bucketedItems.toMutableMap().filter {
        it.value.isNotEmpty()
    }.keys.toMutableList()
    fun getItemsProp(): MutableMap<NEUInternalName, TrackedItem> = getSelectedBucket()?.let {
        getBucket(it)
    } ?: flattenBuckets()
    fun getSelectedBucket() = selectedBucket
    fun selectNextSequentialBucket() {
        // Move to the next ordinal, or wrap to null if at the last value
        val nextOrdinal = selectedBucket?.let { it.ordinal + 1 } // Only calculate if selectedBucket is non-null
        selectedBucket = when {
            selectedBucket == null -> buckets.first() // If selectedBucket is null, start with the first enum
            nextOrdinal != null && nextOrdinal >= buckets.size -> null // Wrap to null if we've reached the end
            nextOrdinal != null -> buckets[nextOrdinal] // Move to the next enum value
            else -> selectedBucket // Fallback, shouldn't happen
        }
    }

    private fun flattenBuckets(): MutableMap<NEUInternalName, TrackedItem> {
        val flatMap: MutableMap<NEUInternalName, TrackedItem> = HashMap()
        getPoppedBuckets().distinct().forEach { bucket ->
            getBucket(bucket).filter { !it.value.hidden }.entries.distinctBy { it.key }.forEach { (key, value) ->
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
