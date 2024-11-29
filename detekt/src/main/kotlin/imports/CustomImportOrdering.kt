package at.hannibal2.skyhanni.detektrules.imports

import at.hannibal2.skyhanni.detektrules.PreprocessingPattern
import at.hannibal2.skyhanni.detektrules.PreprocessingPattern.Companion.containsPreprocessingPattern
import at.hannibal2.skyhanni.detektrules.SkyHanniRule
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtImportList

/**
 * This rule enforces correct import ordering, while ignoring preprocessed comments and imports that are in a preprocessed block.
 */
class CustomImportOrdering(config: Config) : SkyHanniRule(config) {
    override val issue = Issue(
        "CustomImportOrdering",
        Severity.Style,
        "Enforces correct import ordering, taking into account preprocessed imports.",
        Debt.FIVE_MINS,
    )

    companion object {
        private val importOrder = ImportSorter()

        private val packageImportOrdering = listOf("java.", "javax.", "kotlin.")

        private class ImportSorter : Comparator<KtImportDirective> {
            override fun compare(
                import1: KtImportDirective,
                import2: KtImportDirective,
            ): Int {
                val importPath1 = import1.importPath!!.pathStr
                val importPath2 = import2.importPath!!.pathStr

                val isTypeAlias1 = import1.aliasName != null
                val isTypeAlias2 = import2.aliasName != null

                val index1 = packageImportOrdering.indexOfFirst { importPath1.startsWith(it) }
                val index2 = packageImportOrdering.indexOfFirst { importPath2.startsWith(it) }

                return when {
                    isTypeAlias1 && isTypeAlias2 -> importPath1.compareTo(importPath2)
                    isTypeAlias1 && !isTypeAlias2 -> 1
                    !isTypeAlias1 && isTypeAlias2 -> -1
                    index1 == -1 && index2 == -1 -> importPath1.compareTo(importPath2)
                    index1 == -1 -> -1
                    index2 == -1 -> 1
                    else -> index1.compareTo(index2)
                }
            }
        }
    }

    private fun isImportsCorrectlyOrdered(imports: List<KtImportDirective>, rawText: List<String>): Boolean {
        if (rawText.any { it.isBlank() }) {
            return false
        }

        var inPreprocess = false
        val linesToIgnore = mutableListOf<String>()

        for (line in rawText) {
            if (line.contains(PreprocessingPattern.IF.asComment)) {
                inPreprocess = true
                continue
            }
            if (line.contains(PreprocessingPattern.ENDIF.asComment)) {
                inPreprocess = false
                continue
            }
            if (line.contains(PreprocessingPattern.DOLLAR_DOLLAR.asComment)) {
                continue
            }
            if (inPreprocess) {
                linesToIgnore.add(line)
            }
        }

        val originalImports = rawText.filter { !it.containsPreprocessingPattern() && !linesToIgnore.contains(it) }
        val formattedOriginal = originalImports.joinToString("\n") { it }

        val expectedImports = imports.sortedWith(importOrder).map { "import ${it.importPath}" }
        val formattedExpected = expectedImports.filter { !linesToIgnore.contains(it) }.joinToString("\n")

        return formattedOriginal == formattedExpected
    }

    override fun visitImportList(importList: KtImportList) {
        val rawText = importList.text.trim()
        if (rawText.isBlank()) {
            return
        }

        val importsCorrect = isImportsCorrectlyOrdered(importList.imports, rawText.lines())

        if (!importsCorrect) {
            importList.reportIssue(
                "Imports must be ordered in lexicographic order without any empty lines in-between " +
                    "with \"java\", \"javax\", \"kotlin\" and aliases in the end. This should then be followed by " +
                    "pre-processed imports.",
            )
        }
        super.visitImportList(importList)
    }
}
