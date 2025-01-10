package at.hannibal2.skyhanni.detektrules.storage

import com.google.auto.service.AutoService
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

@AutoService(RuleSetProvider::class)
class StorageRuleSetProvider : RuleSetProvider {
    override val ruleSetId: String = "StorageRules"

    override fun instance(config: Config): RuleSet {
        return RuleSet(ruleSetId, listOf(
            EnforceConfigMutableCollections(config)
        ))
    }
}
