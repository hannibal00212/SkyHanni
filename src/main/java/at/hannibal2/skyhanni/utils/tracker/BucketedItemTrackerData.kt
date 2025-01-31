package at.hannibal2.skyhanni.utils.tracker

import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import com.google.gson.annotations.Expose
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl

abstract class BucketedItemTrackerData<E : Enum<E>> : ItemTrackerData() {

    @Deprecated("Use getBucketItems(bucket) instead", ReplaceWith("getBucketItems(bucket)"))
    override fun getDescription(timesGained: Long): List<String> =
        throw UnsupportedOperationException("Use getDescription(bucket, timesGained) instead")

    abstract fun getDescription(bucket: E?, timesGained: Long): List<String>

    @Deprecated("Use getBucketItems(bucket) instead", ReplaceWith("getBucketItems(bucket)"))
    override fun getCoinName(item: TrackedItem): String =
        throw UnsupportedOperationException("Use getCoinName(bucket, item) instead")

    abstract fun getCoinName(bucket: E?, item: TrackedItem): String

    @Deprecated("Use getBucketItems(bucket) instead", ReplaceWith("getBucketItems(bucket)"))
    override fun getCoinDescription(item: TrackedItem): List<String> =
        throw UnsupportedOperationException("Use getCoinDescription(bucket, item) instead")

    abstract fun getCoinDescription(bucket: E?, item: TrackedItem): List<String>

    abstract fun E.isBucketSelectable(): Boolean

    override fun reset() {
        bucketedItems.clear()
        selectedBucket = null
        resetItems()
    }

    fun addItem(bucket: E, internalName: NeuInternalName, stackSize: Int) {
        val bucketMap = bucketedItems.getOrPut(bucket) { HashMap() }
        val item = bucketMap.getOrPut(internalName) { TrackedItem() }

        item.timesGained++
        item.totalAmount += stackSize
        item.lastTimeUpdated = SimpleTimeMark.now()
    }

    fun removeItem(bucket: E?, internalName: NeuInternalName) {
        bucket?.let {
            bucketedItems[bucket]?.remove(internalName)
        } ?: bucketedItems.forEach {
            it.value.remove(internalName)
        }
    }

    fun toggleItemHide(bucket: E?, internalName: NeuInternalName) {
        bucket?.let {
            bucketedItems[bucket]?.get(internalName)?.let { it.hidden = !it.hidden }
        } ?: bucketedItems.forEach {
            it.value[internalName]?.hidden = !it.value[internalName]?.hidden!!
        }
    }

    val selectableBuckets get() = buckets.filter { it.isBucketSelectable() }

    private val buckets: Array<E> by lazy {
        @Suppress("UNCHECKED_CAST")
        selectedBucket?.javaClass?.enumConstants
            ?: (this.javaClass.genericSuperclass as? ParameterizedTypeImpl)?.actualTypeArguments?.firstOrNull()?.let { type ->
                (type as? Class<E>)?.enumConstants
            } ?: throwBucketInitError()
    }

    private fun throwBucketInitError(): Nothing = ErrorManager.skyHanniError(
        "Unable to retrieve enum constants for E in BucketedItemTrackerData",
        "selectedBucket" to selectedBucket,
        "dataClass" to this.javaClass.superclass.name,
    )

    @Expose
    var selectedBucket: E? = null

    @Expose
    private val bucketedItems: MutableMap<E, MutableMap<NeuInternalName, TrackedItem>> = HashMap()

    private fun getBucket(bucket: E): MutableMap<NeuInternalName, TrackedItem> = bucketedItems[bucket]?.toMutableMap() ?: HashMap()
    private fun getPoppedBuckets(): MutableList<E> = bucketedItems.toMutableMap().filter {
        it.value.isNotEmpty()
    }.keys.toMutableList()

    fun getItemsProp(): MutableMap<NeuInternalName, TrackedItem> = selectedBucket?.let {
        getBucket(it)
    } ?: flattenBucketsItems()

    private fun getBucketItems(bucket: E) = bucketedItems[bucket]?.toMutableMap() ?: HashMap()
    fun getSelectedBucketItems() = selectedBucket?.let { getBucketItems(it) } ?: flattenBucketsItems()
    private fun flattenBucketsItems(): MutableMap<NeuInternalName, TrackedItem> {
        val flatMap: MutableMap<NeuInternalName, TrackedItem> = HashMap()
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
