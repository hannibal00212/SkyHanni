package at.hannibal2.skyhanni.detektrules.repo

import com.google.auto.service.AutoService
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

@AutoService(RuleSetProvider::class)
class RepoRuleSetProvider : RuleSetProvider {
    override val ruleSetId: String = "RepoRules"

    override fun instance(config: Config): RuleSet {
        return RuleSet(
            ruleSetId,
            listOf(
                SkullTexturesUseRepo(config),
                RepoPatternRegexTest(config),
            ),
        )
    }
}
