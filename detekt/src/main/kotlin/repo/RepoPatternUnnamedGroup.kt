package at.hannibal2.skyhanni.detektrules.repo

import at.hannibal2.skyhanni.detektrules.RepoPatternElement.Companion.asRepoPatternElement
import at.hannibal2.skyhanni.detektrules.SkyHanniRule
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtPropertyDelegate

class RepoPatternUnnamedGroup(config: Config) : SkyHanniRule(config) {
    override val issue = Issue(
        "RepoPatternUnnamedGroup",
        Severity.Style,
        "All repo patterns must not contain unnamed groups.",
        Debt.FIVE_MINS,
    )

    override fun visitPropertyDelegate(delegate: KtPropertyDelegate) {
        super.visitPropertyDelegate(delegate)

        val repoPatternElement = delegate.asRepoPatternElement() ?: return

        if (repoPatternElement.rawPattern.hasUnnamedGroup()) {
            delegate.reportIssue("Repo pattern `${repoPatternElement.variableName}` must not contain unnamed capture groups.")
        }
    }

    private fun String.hasUnnamedGroup(): Boolean {
        // Remove content inside square brackets
        val withoutSquareBrackets = squareBracketRegex.replace(this, "")
        // Check if simplified string contains unnamed groups
        return unnamedGroupRegex.containsMatchIn(withoutSquareBrackets)
    }

    companion object {
        // Regex to find content inside square brackets, including nested brackets
        private val squareBracketRegex = Regex("""(?<!\\)\[(?:\^])?(?:\\.|[^]])*]""")
        // Regex to find unescaped '(' not followed by '?'
        private val unnamedGroupRegex = Regex("""(?<!\\)\((?!\?)""")
    }
}
