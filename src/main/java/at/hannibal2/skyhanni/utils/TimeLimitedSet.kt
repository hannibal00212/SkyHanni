package at.hannibal2.skyhanni.utils

import com.google.common.cache.RemovalCause
import kotlin.time.Duration

@Suppress("UnstableApiUsage")
class TimeLimitedSet<T : Any>(
    expireAfterWrite: Duration,
    private val removalListener: ((T?, RemovalCause) -> Unit)? = null,
) : MutableSet<T> {

    private val cache = TimeLimitedCache<T, Unit>(expireAfterWrite) { key, _, cause ->
        removalListener?.let {
            it(key, cause)
        }
    }

    override val size: Int get() = cache.size

    override fun isEmpty(): Boolean = cache.isEmpty()

    override operator fun contains(element: T): Boolean = cache.containsKey(element)

    override fun add(element: T): Boolean {
        return (element in cache).also { cache[element] = Unit }
    }

    override fun remove(element: T): Boolean {
        if (element !in cache) return false
        cache.remove(element)
        return true
    }

    fun addIfAbsent(element: T) {
        if (!contains(element)) add(element)
    }

    override fun addAll(elements: Collection<T>): Boolean {
        var value = false
        for (element in elements) {
            if (add(element)) value = true
        }
        return value
    }

    override fun clear() = cache.clear()

    override fun containsAll(elements: Collection<T>): Boolean {
        return elements.all { it in cache }
    }

    override fun iterator(): MutableIterator<T> = cache.keys.iterator()
    override fun retainAll(elements: Collection<T>): Boolean {
        var value = false
        for (key in cache.keys) {
            if (key !in elements) {
                remove(key)
                value = true
            }
        }
        return value
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        var value = false
        for (element in elements) {
            if (remove(element)) value = true
        }
        return value
    }
}
