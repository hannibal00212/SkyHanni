package at.hannibal2.skyhanni.detektrules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Rule
import org.jetbrains.kotlin.com.intellij.psi.PsiElement

abstract class SkyHanniRule(config: Config) : Rule(config) {

    protected fun PsiElement.reportIssue(message: String) {
        report(CodeSmell(issue, Entity.from(this), message))
    }
}
