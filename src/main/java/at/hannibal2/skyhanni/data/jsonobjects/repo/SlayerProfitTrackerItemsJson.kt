package at.hannibal2.skyhanni.data.jsonobjects.repo

import at.hannibal2.skyhanni.utils.NeuInternalName
import com.google.gson.annotations.Expose

data class SlayerProfitTrackerItemsJson(
    @Expose val slayers: Map<String, List<NeuInternalName>>,
)
