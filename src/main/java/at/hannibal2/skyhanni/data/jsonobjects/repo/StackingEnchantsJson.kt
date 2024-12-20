package at.hannibal2.skyhanni.data.jsonobjects.repo

import com.google.gson.annotations.Expose

data class StackingEnchantsJson(
    @Expose val enchants: Map<String, StackingEnchantData>,
)

data class StackingEnchantData(
    @Expose val levels: List<Int>,
    @Expose val statName: String,
)
