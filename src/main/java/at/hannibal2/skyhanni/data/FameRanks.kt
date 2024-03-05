package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.data.jsonobjects.repo.FameRankJson
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object FameRanks {
    var fameRanks = emptyList<FameRank>()
        private set

    fun getFameRankByNameOrNull(name: String) = fameRanks.firstOrNull { it.name == name }

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val ranks = event.getConstant<FameRankJson>("FameRank")
        fameRanks = ranks.fame_rank.map { FameRank(it.value.name, it.value.fame_required, it.value.bits_multiplier, it.value.votes) }
    }
}

data class FameRank(
    val name: String,
    val fameRequired: Int,
    val bitsMultiplier: Double,
    val electionVotes: Int
)
