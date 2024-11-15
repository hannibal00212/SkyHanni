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
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.psiUtil.nextLeaf
import org.jetbrains.kotlin.psi.psiUtil.siblings

/**
 * This rule enforces the default spacing rules for annotations but allows preprocessed comments to be between
 * an annotation and the annotated construct.
 */
class CustomAnnotationSpacing(config: Config) : Rule(config) {
    override val issue = Issue(
        "CustomAnnotationSpacing",
        Severity.Style,
        "Enforces custom spacing rules for annotations.",
        Debt.FIVE_MINS
    )

    override fun visitAnnotationEntry(annotationEntry: KtAnnotationEntry) {
        val nextNodes = annotationEntry.nextLeaf()?.siblings()?.takeWhile { it is PsiWhiteSpace || it is PsiComment } ?: sequenceOf()

        val hasInvalidSpacing = nextNodes.any { nextNode ->
            when (nextNode) {
                is PsiWhiteSpace -> nextNode.isInvalid()
                is PsiComment -> nextNode.isInvalid()
                else -> false
            }
        }

        if (hasInvalidSpacing) {
            report(
                CodeSmell(
                    issue,
                    Entity.from(annotationEntry),
                    "Annotations should occur immediately before the annotated construct"
                )
            )
        }
        super.visitAnnotationEntry(annotationEntry)
    }

    private fun PsiWhiteSpace.isInvalid(): Boolean {
        return text.indexOf('\n') != text.lastIndexOf('\n')
    }

    private fun PsiComment.isInvalid(): Boolean {
        return !text.containsPreprocessingPattern()
    }
}
