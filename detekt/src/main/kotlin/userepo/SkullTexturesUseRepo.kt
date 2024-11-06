package at.hannibal2.skyhanni.detektrules.userepo

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtStringTemplateExpression

/**
 * This rule reports all instances of hard-coded skull textures in the codebase.
 */
class SkullTexturesUseRepo(config: Config) : Rule(config) {
    override val issue = Issue(
        "SkullTexturesUseRepo",
        Severity.Style,
        "Avoid hard-coding skull textures in strings. Use the SkullTextureHolder instead, and add the texture to Skulls.json in the repository.",
        Debt.FIVE_MINS,
    )

    private val scannedTextureStarts = listOf(
        "ewogICJ0aW1l",
        "eyJ0ZXh0dXJl"
    )

    override fun visitStringTemplateExpression(expression: KtStringTemplateExpression) {
        val text =
            expression.text // Be aware .getText() returns the entire span of this template, including variable names contained within. This should be rare enough of a problem for us to not care about it.

        for (textureStarters in scannedTextureStarts) {
            if (text.startsWith(textureStarters)) {
                report(
                    CodeSmell(
                        issue,
                        Entity.from(expression),
                        "Avoid hard-coding skull texture text in strings.",
                    ),
                )
            }
        }
        super.visitStringTemplateExpression(expression)
    }
}

