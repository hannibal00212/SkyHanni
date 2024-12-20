package at.hannibal2.skyhanni.data.jsonobjects.repo

import at.hannibal2.skyhanni.utils.NEUInternalName
import com.google.gson.annotations.Expose

data class SlayerProfitTrackerItemsJson(
    @Expose val slayers: Map<String, List<NEUInternalName>>,
)
