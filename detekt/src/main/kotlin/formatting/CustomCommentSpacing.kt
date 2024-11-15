package at.hannibal2.skyhanni.detektrules.formatting

import at.hannibal2.skyhanni.detektrules.PreprocessingPattern.Companion.containsPreprocessingPattern
import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.com.intellij.psi.PsiComment

/**
 * This rule enforces the default spacing rules for comments but ignores preprocessed comments.
 */
class CustomCommentSpacing(config: Config) : Rule(config) {
    override val issue = Issue(
        "CustomCommentSpacing",
        Severity.Style,
        "Enforces custom spacing rules for comments.",
        Debt.FIVE_MINS
    )

    override fun visitComment(comment: PsiComment) {
        if (comment.text.containsPreprocessingPattern()) return
        if (!commentRegex.matches(comment.text)) {
            report(
                CodeSmell(
                    issue,
                    Entity.from(comment),
                    "Expected space after opening comment."
                )
            )
        }

        // Fallback to super (ostensibly a no-check)
        super.visitComment(comment)
    }

    companion object {
        /**
         * REGEX-TEST: // Test comment
         * REGEX-TEST: /* Test comment */
         */
        val commentRegex = Regex("""^(?:\/{2}|\/\*)(?:\s.*|$)""", RegexOption.DOT_MATCHES_ALL)
    }
}
