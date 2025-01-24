package at.hannibal2.skyhanni.data.jsonobjects.repo.neu

import com.google.gson.annotations.Expose

data class NeuHoppityJson(
    @Expose val hoppity: HoppityInfo,
)

data class HoppityInfo(
    @Expose val rarities: Map<String, HoppityRarityInfo>,
    @Expose val special: Map<String, MythicRabbitInfo>,
)

data class HoppityRarityInfo(
    @Expose val rabbits: List<String>,
    @Expose val chocolate: Int,
    @Expose val multiplier: Double,
)

data class MythicRabbitInfo(
    @Expose val chocolate: Int,
    @Expose val multiplier: Double,
)
