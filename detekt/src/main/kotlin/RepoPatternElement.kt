package at.hannibal2.skyhanni.detektrules

import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtEscapeStringTemplateEntry
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPropertyDelegate
import org.jetbrains.kotlin.psi.KtStringTemplateEntryWithExpression
import org.jetbrains.kotlin.psi.KtStringTemplateExpression

class RepoPatternElement private constructor(
    val variableName: String,
    val rawPattern: String,
    val regexTests: List<String>,
    val failingRegexTests: List<String>,
) {

    val pattern by lazy { rawPattern.toPattern() }

    companion object {
        fun KtPropertyDelegate.asRepoPatternElement(): RepoPatternElement? {
            val expression = this.expression as? KtDotQualifiedExpression ?: return null
            val callExpression = expression.selectorExpression as? KtCallExpression ?: return null
            if (callExpression.valueArguments.size != 2) return null

            val patternArg = callExpression.valueArguments[1].getArgumentExpression() ?: return null

            // We only want to match on plain strings, not string templates
            if (patternArg !is KtStringTemplateExpression) return null
            if (patternArg.entries.any { it is KtStringTemplateEntryWithExpression }) return null

            val rawPattern = patternArg.entries.joinToString("") { entry ->
                when (entry) {
                    is KtLiteralStringTemplateEntry -> entry.text
                    is KtEscapeStringTemplateEntry -> entry.unescapedValue
                    else -> "" // Skip any other types of entries
                }
            }.removeSurrounding("\"").replace("\n", "")

            val parent = parent as? KtProperty ?: return null
            val variableName = parent.name ?: "unknownPattern"

            val (regexTests, failingRegexTests) = findRegexTestInKDoc(parent)
            return RepoPatternElement(variableName, rawPattern, regexTests, failingRegexTests)
        }

        private fun findRegexTestInKDoc(property: KtProperty): Pair<List<String>, List<String>> {
            val kDoc = property.docComment ?: return listOf<String>() to listOf()

            val regexTests = mutableListOf<String>()
            val failingRegexTests = mutableListOf<String>()

            kDoc.getDefaultSection().getContent().lines().forEach { line ->
                if (line.contains("REGEX-TEST: ")) {
                    regexTests.add(line.substringAfter("REGEX-TEST: "))
                }
                if (line.contains("REGEX-FAIL: ")) {
                    failingRegexTests.add(line.substringAfter("REGEX-FAIL: "))
                }
            }
            return regexTests to failingRegexTests
        }
    }
}
