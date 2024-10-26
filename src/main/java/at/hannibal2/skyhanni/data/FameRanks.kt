package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.data.jsonobjects.repo.FameRankJson
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object FameRanks {
    var fameRanks = listOf<FameRank>()
        private set

    fun getByName(name: String) = fameRanks.find { it.name.equals(name, true) }

    fun getByInternalName(internalName: String) = fameRanks.find { it.internalName == internalName }

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val ranks = event.getConstant<FameRankJson>("FameRank")
        fameRanks = ranks.fameRank.map { (internalName, rank) ->
            FameRank(rank.name, rank.fameRequired, rank.bitsMultiplier, rank.votes, internalName)
        }
    }
}

data class FameRank(
    val name: String,
    val fameRequired: Int,
    val bitsMultiplier: Double,
    val electionVotes: Int,
    val internalName: String,
)
