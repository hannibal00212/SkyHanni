package at.hannibal2.skyhanni.utils

import com.google.common.cache.CacheBuilder
import com.google.common.cache.RemovalCause
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

@Suppress("UnstableApiUsage", "IgnoredReturnValue")
class TimeLimitedCache<K : Any, V : Any>(
    expireAfterWrite: Duration,
    private val removalListener: ((K?, V?, RemovalCause) -> Unit)? = null,
) : MutableMap<K, V> {

    private val cache = CacheBuilder.newBuilder()
        .expireAfterWrite(expireAfterWrite.inWholeMilliseconds, TimeUnit.MILLISECONDS)
        .apply {
            removalListener?.let { listener ->
                removalListener<K?, V?> {
                    listener(it.key, it.value, it.cause)
                }
            }
        }
        .build<K, V>()

    override val size: Int get() = cache.size().toInt()

    override fun isEmpty(): Boolean = cache.size() == 0L

    override fun containsKey(key: K): Boolean = cache.getIfPresent(key) != null

    override fun containsValue(value: V): Boolean = value in values

    override fun get(key: K): V? = cache.getIfPresent(key)

    operator fun set(key: K, value: V) = cache.put(key, value)

    override fun put(key: K, value: V): V? {
        val previous = get(key)
        set(key, value)
        return previous
    }

    override fun remove(key: K): V? {
        val value = get(key) ?: return null
        cache.invalidate(key)
        return value
    }

    override fun putAll(from: Map<out K, V>) {
        from.forEach { (key, value) -> put(key, value) }
    }

    override fun clear() = cache.invalidateAll()

    override val keys: MutableSet<K> = getMap().keys

    override val values: MutableCollection<V> get() = getMap().values

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>> = getMap().entries

    @Deprecated("", ReplaceWith("get(key)"))
    fun getOrNull(key: K): V? = get(key)

    fun isEmpty(): Boolean = cache.size() == 0L

    fun isNotEmpty(): Boolean = !isEmpty()

    val size: Long
        get() = cache.size()

    /**
     * Modifications to the returned map are not supported and may lead to unexpected behavior.
     * This method is intended for read-only operations such as iteration or retrieval of values.
     *
     * This returning map and any view into that map via [Map.keys], [Map.values] or [Map.entries],
     * may return [Collection.size] values larger than the elements actually present during iteration.
     * This can lead to problems with kotlin's [Iterable.toSet], [Iterable.toList] (etc.) small collection
     * optimizations. Those methods (and similar ones) have optimizations for single element collections.
     * Since the [Collection.size] is checked first those methods will then not make any additional
     * checks when accessing the elements of the collection. This can lead to rare [NoSuchElementException].
     * Therefore, the direct constructors of [HashSet], [ArrayList] and similar are to be preferred,
     * since they make no such optimizations.
     *
     * @return A read-only view of the cache's underlying map.
     */
    private fun getMap(): ConcurrentMap<K, V> = cache.asMap()
}
