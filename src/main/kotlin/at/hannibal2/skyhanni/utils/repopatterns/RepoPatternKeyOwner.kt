package at.hannibal2.skyhanni.utils.repopatterns

import kotlin.reflect.KProperty

/** Declares which class/property owns a repo pattern key.
 * @param ownerClass the owning class
 * @param property the property how owns it in the [ownerClass]
 * @param shares declares if the sub key space is allowed to be used by other [RepoPatternKeyOwner]
 * @param parent the [RepoPatternKeyOwner] that gives the permission to use a sub key from it sub key space that is locked, as [shares] is false for that[RepoPatternKeyOwner]
 * @param transient declares if it is just a ghost how can be replaced at any time in the [RepoPatternManager.exclusivity]
 * */
data class RepoPatternKeyOwner(
    val ownerClass: Class<*>?,
    val property: KProperty<*>?,
    val shares: Boolean,
    val parent: RepoPatternKeyOwner?,
    val transient: Boolean = false,
)
