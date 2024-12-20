package at.hannibal2.skyhanni.data.jsonobjects.repo.neu

import at.hannibal2.skyhanni.utils.NEUInternalName
import com.google.gson.annotations.Expose

data class NeuRNGScore(
    @Expose val catacombs: Map<String, Map<NEUInternalName, Long>>,
    @Expose val slayer: Map<String, Map<NEUInternalName, Long>>,
)
