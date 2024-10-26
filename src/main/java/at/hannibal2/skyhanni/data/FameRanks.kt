package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.data.jsonobjects.repo.FameRankJson
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object FameRanks {
    var fameRanksMap = mapOf<String, FameRank>()
        private set

    fun getByName(name: String) = fameRanksMap.values.find { it.name.equals(name, true) }

    fun getByInternalName(internalName: String) = fameRanksMap[internalName]

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val ranks = event.getConstant<FameRankJson>("FameRank")
        fameRanksMap = ranks.fameRank.map { (internalName, rank) ->
            FameRank(rank.name, rank.fameRequired, rank.bitsMultiplier, rank.votes, internalName)
        }.associateBy(FameRank::internalName)
    }
}

data class FameRank(
    val name: String,
    val fameRequired: Int,
    val bitsMultiplier: Double,
    val electionVotes: Int,
    val internalName: String,
)
