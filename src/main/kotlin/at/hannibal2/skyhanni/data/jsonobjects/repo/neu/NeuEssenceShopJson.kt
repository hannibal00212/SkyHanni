package at.hannibal2.skyhanni.data.jsonobjects.repo.neu

import com.google.gson.annotations.Expose

data class NeuEssenceShopJson(
    @Expose val name: String,
    @Expose val costs: List<Int>
)
