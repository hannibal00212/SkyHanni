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
        "!isInIsland checks should be removed and replaced with onlyOnIsland = IslandType in the @HandleEvent annotation",
        Debt.FIVE_MINS
    )

    private fun KtExpression.isEarlyReturn(): Boolean = this is KtIfExpression && then is KtReturnExpression

    override fun visitNamedFunction(function: KtNamedFunction) {
        if (function.hasAnnotation("HandleEvent")) {
            val bodyExpressions = function.bodyExpression?.collectDescendantsOfType<KtIfExpression>() ?: return

            for (ifExpression in bodyExpressions) {
                if (!ifExpression.isEarlyReturn()) continue
                val issueSpecific = regexMap.entries.firstOrNull { it.key.matches(ifExpression.text) }?.value ?: continue
                ifExpression.reportIssue(
                    "This early return should be replaced with onlyOnIsland = $issueSpecific in the @HandleEvent annotation"
                )
            }
        }

        super.visitNamedFunction(function)
    }

    companion object {
        /**
         * This is the regex I'm using to check these:
         *
         * @HandleEvent
         * (?<fun>.*)
         * .*if \(!RiftApi.inRift\(\)\) return
         */
        private val isInIslandRegex = Regex(".*!IslandType\\..*\\.isInIsland\\(\\).*")
        private val isInGardenRegex = Regex(".*!GardenApi.inGarden\\(\\).*")
        private val isInRiftRegex = Regex(".*!RiftApi.inRift\\(\\).*")
        private val isInDungeonRegex = Regex(".*!DungeonApi.inDungeon\\(\\).*")
        private val isInKuudraRegex = Regex(".*!KuudraApi.inKuudra\\(\\).*")

        private val regexMap = mapOf(
            isInIslandRegex to "IslandType",
            isInGardenRegex to "IslandType.GARDEN",
            isInRiftRegex to "IslandType.THE_RIFT",
            isInDungeonRegex to "IslandType.CATACOMBS",
            isInKuudraRegex to "IslandType.KUUDRA_ARENA"
        )
    }
}
