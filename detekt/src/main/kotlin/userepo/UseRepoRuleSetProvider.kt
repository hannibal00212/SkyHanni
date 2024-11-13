package at.hannibal2.skyhanni.detektrules.userepo

import com.google.auto.service.AutoService
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

@AutoService(RuleSetProvider::class)
class UseRepoRuleSetProvider : RuleSetProvider {
    override val ruleSetId: String = "UseRepoRules"

    override fun instance(config: Config): RuleSet {
        return RuleSet(
            ruleSetId,
            listOf(
                SkullTexturesUseRepo(config),
            ),
        )
    }
}
