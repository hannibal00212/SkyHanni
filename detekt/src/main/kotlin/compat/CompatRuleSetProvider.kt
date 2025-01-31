package at.hannibal2.skyhanni.detektrules.compat

import com.google.auto.service.AutoService
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

@AutoService(RuleSetProvider::class)
class CompatRuleSetProvider : RuleSetProvider {
    override val ruleSetId: String = "CompatRules"

    override fun instance(config: Config): RuleSet {
        return RuleSet(
            ruleSetId,
            listOf(
                McPlayerRule(config),
                McWorldRule(config),
            ),
        )
    }
}

