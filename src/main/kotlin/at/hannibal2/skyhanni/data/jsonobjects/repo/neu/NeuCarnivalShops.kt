package at.hannibal2.skyhanni.data.jsonobjects.repo.neu

import com.google.gson.annotations.Expose

data class NeuMiscJson(
    @Expose val carnivalTokenShops: Map<String, Map<String, NeuCarnivalTokenCostJson>>
)

data class NeuCarnivalTokenCostJson(
    @Expose val name: String,
    @Expose val costs: List<Int>
)
