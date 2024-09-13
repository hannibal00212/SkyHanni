package at.hannibal2.skyhanni.data.jsonobjects.repo

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.utils.LorenzVec
import com.google.gson.annotations.Expose
import java.util.TreeSet

data class HoppityEggLocationsJson(
    @Expose val eggLocations: Map<IslandType, List<LorenzVec>>,
    @Expose val rabbitSlots: Map<Int, Int>,
    @Expose val otherUpgradeSlots: Set<Int>,
    @Expose val noPickblockSlots: Set<Int>,
    @Expose val barnIndex: Int,
    @Expose val infoIndex: Int,
    @Expose val productionInfoIndex: Int,
    @Expose val prestigeIndex: Int,
    @Expose val milestoneIndex: Int,
    @Expose val leaderboardIndex: Int,
    @Expose val handCookieIndex: Int,
    @Expose val timeTowerIndex: Int,
    @Expose val shrineIndex: Int,
    @Expose val coachRabbitIndex: Int,
    @Expose val maxRabbits: Int,
    @Expose val maxPrestige: Int,
    @Expose val chocolateMilestones: TreeSet<Long>,
    @Expose val apiEggLocations: Map<IslandType, Map<String, LorenzVec>>,
    @Expose val specialRabbits: List<String>,
)
