package at.hannibal2.skyhanni.data.jsonobjects.repo.neu

import at.hannibal2.skyhanni.utils.NEUInternalName
import com.google.gson.annotations.Expose

data class NeuSacksJson(
    @Expose val sacks: Map<String, SackInfo>,
)

data class SackInfo(
    @Expose val item: NEUInternalName,
    @Expose val contents: List<NEUInternalName>,
)
