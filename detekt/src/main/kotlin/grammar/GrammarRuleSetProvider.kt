package at.hannibal2.skyhanni.detektrules.grammar

import com.google.auto.service.AutoService
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

@AutoService(RuleSetProvider::class)
class GrammarRuleSetProvider : RuleSetProvider {
    override val ruleSetId: String = "GrammarRules"

    override fun instance(config: Config): RuleSet {
        return RuleSet(ruleSetId, listOf(
            AvoidColour(config)
        ))
    }
}
