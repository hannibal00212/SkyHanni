package at.hannibal2.skyhanni.data.jsonobjects.repo

import at.hannibal2.skyhanni.utils.NeuInternalName
import com.google.gson.annotations.Expose

data class CrimsonIsleReputationJson(
    @Expose val FISHING: Map<String, ReputationQuest>,
    @Expose val RESCUE: Map<String, ReputationQuest>,
    @Expose val FETCH: Map<String, ReputationQuest>,
    @Expose val DOJO: Map<String, ReputationQuest>,
    @Expose val MINIBOSS: Map<String, ReputationQuest>,
    @Expose val KUUDRA: Map<String, ReputationQuest>,
)

data class ReputationQuest(
    @Expose val item: NeuInternalName,
    @Expose val location: List<Double>,
)
