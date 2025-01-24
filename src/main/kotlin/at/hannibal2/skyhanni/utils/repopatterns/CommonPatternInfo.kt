package at.hannibal2.skyhanni.utils.repopatterns

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

sealed class CommonPatternInfo<R, C> : ReadOnlyProperty<Any?, C> {
    abstract val isLoadedRemotely: Boolean

    abstract val wasOverridden: Boolean

    abstract val defaultPattern: R

    abstract val key: String

    abstract val parent: RepoPatternKeyOwner?

    abstract val shares: Boolean

    abstract val value: C

    /**
     * Whether the pattern has obtained a lock on a code location and a key.
     * Once set, no other code locations can access this repo pattern (and therefore the key).
     * @see RepoPatternManager.checkExclusivity
     */
    internal var hasObtainedLock = false

    override fun getValue(thisRef: Any?, property: KProperty<*>): C {
        verifyLock(thisRef, property)
        return value
    }

    /**
     * Try to lock the [key] to this key location.
     * @see RepoPatternManager.checkExclusivity
     */
    private fun verifyLock(thisRef: Any?, property: KProperty<*>) {
        if (hasObtainedLock) return
        hasObtainedLock = true
        val owner = RepoPatternKeyOwner(thisRef?.javaClass, property, shares, parent)
        if (shares) {
            RepoPatternManager.checkExclusivity(owner, key)
        } else {
            RepoPatternManager.checkNameSpaceExclusivity(owner, key)
        }
    }

    abstract fun dump(): Map<String, String>
}
