package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.jsonobjects.repo.FameRankJson
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule

@SkyHanniModule
object FameRanks {
    var fameRanks = emptyMap<String, FameRank>()
        private set

    fun getFameRankByNameOrNull(name: String) = fameRanks[name]

    @HandleEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val ranks = event.getConstant<FameRankJson>("FameRank")
        fameRanks = ranks.fameRank.values.map { FameRank(it.name, it.fameRequired, it.bitsMultiplier, it.votes) }
            .associateBy { it.name }
    }
}

data class FameRank(
    val name: String,
    val fameRequired: Int,
    val bitsMultiplier: Double,
    val electionVotes: Int
)
