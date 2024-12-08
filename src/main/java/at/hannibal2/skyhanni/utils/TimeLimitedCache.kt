package at.hannibal2.skyhanni.utils

import com.google.common.cache.CacheBuilder
import com.google.common.cache.RemovalCause
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

@Suppress("UnstableApiUsage")
class TimeLimitedCache<K : Any, V : Any>(
    expireAfterWrite: Duration,
    private val removalListener: (K?, V?, RemovalCause) -> Unit = { _, _, _ -> },
) : Iterable<Map.Entry<K, V>> {

    private val cache = CacheBuilder.newBuilder()
        .expireAfterWrite(expireAfterWrite.inWholeMilliseconds, TimeUnit.MILLISECONDS)
        .removalListener { removalListener(it.key, it.value, it.cause) }
        .build<K, V>()

    // TODO IntelliJ cant replace this, find another way?
//     @Deprecated("outdated", ReplaceWith("[key] = value"))
    @Deprecated("outdated", ReplaceWith("set(key, value)"))
    fun put(key: K, value: V) = set(key, value)

    fun getOrNull(key: K): V? = cache.getIfPresent(key)

    fun getOrPut(key: K, defaultValue: () -> V) = getOrNull(key) ?: defaultValue().also { set(key, it) }

    fun clear() = cache.invalidateAll()

    fun remove(key: K) = cache.invalidate(key)

    fun entries(): Set<Map.Entry<K, V>> = getMap().entries

    fun values(): Collection<V> = getMap().values

    fun keys(): Set<K> = getMap().keys

    /**
     * Modifications to the returned map are not supported and may lead to unexpected behavior.
     * This method is intended for read-only operations such as iteration or retrieval of values.
     *
     * This returning map and any view into that map via [Map.keys], [Map.values] or [Map.entries],
     * may return [Collection.size] values larger than the elements actually present during iteration.
     * This can lead to problems with kotlins [Iterable.toSet], [Iterable.toList] (etc.) small collection
     * optimizations. Those methods (and similar ones) have optimizations for single element collections.
     * Since the [Collection.size] is checked first those methods will then not make any additional
     * checks when accessing the elements of the collection. This can lead to rare [NoSuchElementException].
     * Therefore, the direct constructors of [HashSet], [ArrayList] and similar are to be preferred,
     * since they make no such optimizations.
     *
     * @return A read-only view of the cache's underlying map.
     */
    private fun getMap(): ConcurrentMap<K, V> = cache.asMap()

    fun containsKey(key: K): Boolean = cache.getIfPresent(key) != null

    override fun iterator(): Iterator<Map.Entry<K, V>> = entries().iterator()

    operator fun set(key: K, value: V) {
        cache.put(key, value)
    }
}
