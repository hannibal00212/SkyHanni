package at.hannibal2.skyhanni.utils.repopatterns

import java.util.regex.Pattern

/**
 * A list of [RepoPattern]s. It can be used almost identically, but will instead provide a [List] of [Pattern]s
 */
sealed class RepoPatternList : CommonPatternInfo<List<String>, List<Pattern>>() {
    /**
     * Check whether [value] has been loaded remotely or from the fallback value at [defaultPattern]. In case this is
     * accessed off-thread there are no guarantees for the correctness of this value in relation to any specific call
     * to [value].
     */
    abstract override val isLoadedRemotely: Boolean

    /**
     * Check whether [value] was compiled from a value other than the [defaultPattern]. This is `false` even when
     * loading remotely if the remote pattern matches the local one.
     */
    abstract override val wasOverridden: Boolean

    /**
     * The default patterns that is specified at compile time. This local patterns will be a fallback in case there are
     * no remote patterns available or the remote patterns do not compile.
     */
    abstract override val defaultPattern: List<String>

    /**
     * Key for this pattern list. When loading identifiers from the repo, this will pull all identifiers
     * that start with the key, followed by `.{number}`. Should be consistent across versions.
     */
    abstract override val key: String

    /**
     * Should not be accessed directly. Instead, use delegation at one code location and share the regexes from there.
     * ```kt
     * val actualValue: List<Pattern> by pattern
     * ```
     */
    abstract override val value: List<Pattern>
}
