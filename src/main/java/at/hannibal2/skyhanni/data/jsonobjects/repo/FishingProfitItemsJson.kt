package at.hannibal2.skyhanni.data.jsonobjects.repo

import at.hannibal2.skyhanni.utils.NeuInternalName
import com.google.gson.annotations.Expose

data class FishingProfitItemsJson(
    @Expose val categories: Map<String, List<NeuInternalName>>,
)
