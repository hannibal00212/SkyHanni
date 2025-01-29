package at.hannibal2.skyhanni.detektrules.imports

import com.google.auto.service.AutoService
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

@AutoService(RuleSetProvider::class)
class ImportRuleSetProvider : RuleSetProvider {
    override val ruleSetId: String = "ImportRules"

    override fun instance(config: Config): RuleSet {
        return RuleSet(ruleSetId, listOf(
            CustomImportOrdering(config)
        ))
    }
}
