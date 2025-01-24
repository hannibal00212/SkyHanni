package at.hannibal2.skyhanni.utils.repopatterns

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * A utility class for allowing easier definitions of [RepoPattern]s with a common prefix.
 */
class RepoPatternExclusiveGroupInfo internal constructor(val prefix: String, val parent: RepoPatternKeyOwner?) :
    ReadOnlyProperty<Any?, RepoPatternExclusiveGroup> {

    internal var hasObtainedLock = false
    private lateinit var owner: RepoPatternKeyOwner

    init {
        RepoPatternManager.verifyKeyShape(prefix)
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): RepoPatternExclusiveGroup {
        verifyLock(thisRef, property)
        return RepoPatternExclusiveGroup(prefix, owner)
    }

    /**
     * Try to lock the [key] to this key location.
     * @see RepoPatternManager.checkExclusivity
     */
    private fun verifyLock(thisRef: Any?, property: KProperty<*>) {
        if (hasObtainedLock) return
        hasObtainedLock = true
        owner = RepoPatternKeyOwner(thisRef?.javaClass, property, false, parent)
        RepoPatternManager.checkNameSpaceExclusivity(owner, prefix)
    }
}
