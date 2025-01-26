package at.hannibal2.skyhanni.data.jsonobjects.repo.neu

import at.hannibal2.skyhanni.utils.NeuInternalName
import com.google.gson.annotations.Expose

data class NeuRNGScore(
    @Expose val catacombs: Map<String, Map<NeuInternalName, Long>>,
    @Expose val slayer: Map<String, Map<NeuInternalName, Long>>,
)
