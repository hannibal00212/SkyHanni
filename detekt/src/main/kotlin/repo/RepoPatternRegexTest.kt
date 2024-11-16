package at.hannibal2.skyhanni.detektrules.repo

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtEscapeStringTemplateEntry
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPropertyDelegate
import org.jetbrains.kotlin.psi.KtStringTemplateEntryWithExpression
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import java.util.regex.PatternSyntaxException

class RepoPatternRegexTest(config: Config) : Rule(config) {
    override val issue = Issue(
        "RepoPatternRegexTest",
        Severity.Style,
        "All repo patterns must be accompanied by a passing regex test.",
        Debt.FIVE_MINS,
    )

    override fun visitPropertyDelegate(delegate: KtPropertyDelegate) {
        super.visitPropertyDelegate(delegate)

        val expression = delegate.expression as? KtDotQualifiedExpression ?: return
        if (!expression.text.contains(".pattern(")) return
        val callExpression = expression.selectorExpression as? KtCallExpression ?: return
        if (callExpression.valueArguments.size != 2) return

        val patternArg = callExpression.valueArguments[1].getArgumentExpression() ?: return

        // We only want to match on plain strings, not string templates
        if (patternArg !is KtStringTemplateExpression) return
        if (patternArg.entries.any { it is KtStringTemplateEntryWithExpression }) return

        val rawPattern = patternArg.entries.joinToString("") { entry ->
            when (entry) {
                is KtLiteralStringTemplateEntry -> entry.text
                is KtEscapeStringTemplateEntry -> entry.unescapedValue
                else -> "" // Skip any other types of entries
            }
        }.removeSurrounding("\"").replace("\n", "")

        if (!rawPattern.needsRegexTest()) return

        val parent = delegate.parent as? KtProperty ?: return
        val variableName = parent.name ?: "unknownPattern"

        val (regexTests, failingRegexTests) = findRegexTestInKDoc(parent)

        if (regexTests.isEmpty()) {
            report(
                CodeSmell(
                    issue,
                    Entity.from(delegate),
                    "Repo pattern `$variableName` must have a regex test.",
                ),
            )
            return
        }

        val pattern = try {
            rawPattern.toPattern()
        } catch (e: PatternSyntaxException) {
            report(
                CodeSmell(
                    issue,
                    Entity.from(delegate),
                    "Repo pattern `$variableName` has an invalid regex: `$rawPattern`.",
                ),
            )
            return
        }

        regexTests.forEach { test ->
            if (!pattern.matcher(test).find()) {
                report(
                    CodeSmell(
                        issue,
                        Entity.from(delegate),
                        "Repo pattern `$variableName` failed regex test: `$test` pattern: `$rawPattern`.",
                    ),
                )
            }
        }

        failingRegexTests.forEach { test ->
            if (pattern.matcher(test).find()) {
                report(
                    CodeSmell(
                        issue,
                        Entity.from(delegate),
                        "Repo pattern `$variableName` passed regex test: `$test` pattern: `$rawPattern` even though it was set to fail.",
                    ),
                )
            }
        }
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

    private fun String.needsRegexTest(): Boolean {
        return regexConstructs.containsMatchIn(this)
    }

    companion object {
        val regexConstructs = Regex("""(?<!\\)[.*+(){}\[|?]""")
    }
}
