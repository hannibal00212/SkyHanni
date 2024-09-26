package at.hannibal2.skyhanni.detektrules.grammar

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtStringTemplateExpression

/**
 * This rule reports all usages of the word "colour" in the codebase,
 * preferring the 'American' spelling "color" - this will ignore any
 * type annotations, i.e., `@ConfigEditorColour` will not be reported.
 */
class AvoidColour(config: Config) : Rule(config) {
    override val issue = Issue(
        "AvoidColour",
        Severity.Style,
        "Avoid using the word 'colour' in code, prefer 'color' instead.",
        Debt.FIVE_MINS
    )

    override fun visitStringTemplateExpression(expression: KtStringTemplateExpression) {
        val text = expression.text // Be aware .getText() returns the entire span of this template, including variable names contained within. This should be rare enough of a problem for us to not care about it.
        if (text.contains("colour", ignoreCase = true)) {
            report(
                CodeSmell(
                    issue,
                    Entity.from(expression),
                    "Avoid using the word 'colour' in code, prefer 'color' instead."
                )
            )
        }
        super.visitStringTemplateExpression(expression)
    }
}
