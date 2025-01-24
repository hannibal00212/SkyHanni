package at.hannibal2.skyhanni.utils.repopatterns

import java.util.regex.Pattern

/**
 * Internal class implementing [RepoPattern]. Obtain via [RepoPattern.pattern].
 */
class RepoPatternImpl(
    override val defaultPattern: String,
    override val key: String,
    override val parent: RepoPatternKeyOwner? = null,
) : RepoPattern() {

    override var wasOverridden = false
    override var value: Pattern = Pattern.compile(defaultPattern)
    override var isLoadedRemotely: Boolean = false
    override val shares = true
    override fun dump(): Map<String, String> {
        return mapOf(key to defaultPattern)
    }
}
