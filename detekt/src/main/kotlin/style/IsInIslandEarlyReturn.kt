package at.hannibal2.skyhanni.detektrules.style

import at.hannibal2.skyhanni.detektrules.SkyHanniRule
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.rules.hasAnnotation
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

class IsInIslandEarlyReturn(config: Config) : SkyHanniRule(config) {
    override val issue = Issue(
        "IsInIslandEarlyReturn",
        Severity.Style,
        "!isInIsland checks should be removed and replaced with onlyOnIsland = IslandType in @HandleEvent annotation",
        Debt.FIVE_MINS
    )

    private fun KtExpression.containsIsInIslandCheck(): Boolean = isInIslandRegex.matches(text)
    private fun KtExpression.isEarlyReturn(): Boolean = this is KtIfExpression && then is KtReturnExpression

    override fun visitNamedFunction(function: KtNamedFunction) {
        if (function.hasAnnotation("HandleEvent")) {
            val bodyExpressions = function.bodyExpression?.collectDescendantsOfType<KtIfExpression>() ?: return

            for (ifExpression in bodyExpressions) {
                if (ifExpression.containsIsInIslandCheck() && ifExpression.isEarlyReturn()) {
                    ifExpression.reportIssue("This early return should be replaced with onlyOnIsland = IslandType in @HandleEvent annotation")
                }
            }
        }

        super.visitNamedFunction(function)
    }

    companion object {
        private val isInIslandRegex = Regex(".*!IslandType\\..*\\.isInIsland\\(\\).*")
    }
}
