package at.hannibal2.skyhanni.utils

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.time.Duration

class RecalculatingValue<T>(private val expireTime: Duration, private val calculation: () -> T) : ReadOnlyProperty<Any?, T> {

    private var currentValue: Any? = UNINITIALIZED_VALUE
    private var lastAccessTime = SimpleTimeMark.farPast()

    @Deprecated("use \"by RecalculatingValue\" instead")
    fun getValue(): T {
        if (lastAccessTime.passedSince() > expireTime) {
            currentValue = calculation()
            lastAccessTime = SimpleTimeMark.now()
        }
        @Suppress("UNCHECKED_CAST")
        return currentValue as T
    }

    @Suppress("DEPRECATION")
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = getValue()

    companion object {
        private val UNINITIALIZED_VALUE = Any()
    }
}
