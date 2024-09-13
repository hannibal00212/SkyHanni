package at.hannibal2.skyhanni.utils

import com.google.common.cache.CacheBuilder
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.time.Duration

class TimeLimitedCache<K : Any, V : Any>(
    expireAfterWrite: Duration,
    private val removalListener: (K?, V?) -> Unit = { _, _ -> },
) : Iterable<Map.Entry<K, V>> {

    private val cacheLock = ReentrantReadWriteLock()

    private val cache = CacheBuilder.newBuilder()
        .expireAfterWrite(expireAfterWrite.inWholeMilliseconds, TimeUnit.MILLISECONDS)
        .removalListener {
            cacheLock.writeLock().lock()
            try {
                removalListener(it.key, it.value)
            } finally {
                cacheLock.writeLock().unlock()
            }
        }
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
     * @return A read-only view of the cache's underlying map.
     */
    private fun getMap(): ConcurrentMap<K, V> {
        val asMap: ConcurrentMap<K, V>

        cacheLock.readLock().lock()
        try {
            asMap = cache.asMap()
        } finally {
            cacheLock.readLock().unlock()
        }

        return asMap
    }

    fun containsKey(key: K): Boolean = cache.getIfPresent(key) != null

    override fun iterator(): Iterator<Map.Entry<K, V>> = entries().iterator()

    operator fun set(key: K, value: V) {
        cache.put(key, value)
    }
}
