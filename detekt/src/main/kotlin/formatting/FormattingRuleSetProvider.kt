package at.hannibal2.skyhanni.detektrules.formatting

import com.google.auto.service.AutoService
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

@AutoService(RuleSetProvider::class)
class FormattingRuleSetProvider : RuleSetProvider {
    override val ruleSetId: String = "FormattingRules"

    override fun instance(config: Config): RuleSet {
        return RuleSet(ruleSetId, listOf(
            CustomCommentSpacing(config)
        ))
    }
}
