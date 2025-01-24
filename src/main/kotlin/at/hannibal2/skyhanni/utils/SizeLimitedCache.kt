package at.hannibal2.skyhanni.utils

import com.google.common.cache.CacheBuilder
import com.google.common.cache.RemovalCause

class SizeLimitedCache<K : Any, V : Any>(
    maxSize: Long,
    private val removalListener: (K?, V?, RemovalCause) -> Unit = { _, _, _ -> },
) : Iterable<Map.Entry<K, V>> {

    private val cache = CacheBuilder.newBuilder()
        .maximumSize(maxSize)
        .removalListener { removalListener(it.key, it.value, it.cause) }
        .build<K, V>()

    fun getOrNull(key: K): V? = cache.getIfPresent(key)

    fun getOrPut(key: K, defaultValue: () -> V) = getOrNull(key) ?: defaultValue().also { set(key, it) }

    fun clear() = cache.invalidateAll()

    fun remove(key: K) = cache.invalidate(key)

    operator fun minusAssign(element: K) = remove(element)

    fun entries(): Set<Map.Entry<K, V>> = cache.asMap().entries

    fun values(): Collection<V> = cache.asMap().values

    fun keys(): Set<K> = cache.asMap().keys

    fun containsKey(key: K): Boolean = cache.getIfPresent(key) != null

    override fun iterator(): Iterator<Map.Entry<K, V>> = entries().iterator()

    operator fun set(key: K, value: V) = cache.put(key, value)
}
