package at.hannibal2.skyhanni.features.fame

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.features.dungeon.DungeonApi
import at.hannibal2.skyhanni.features.garden.contest.FarmingContestApi
import at.hannibal2.skyhanni.features.rift.RiftApi
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland

object ReminderUtils {

    /**
     * TODO:
     *  add arachne fight
     *  add slayer boss spawned
     *  add dragon fight
     *  add experimentation solver
     *  add matriach lair
     *  add server restart
     */
    fun isBusy(ignoreFarmingContest: Boolean = false): Boolean =
        (DungeonApi.inDungeon() && !DungeonApi.completed) ||
            LorenzUtils.inKuudraFight || (FarmingContestApi.inContest && !ignoreFarmingContest) ||
            RiftApi.inRift() || IslandType.DARK_AUCTION.isInIsland() || IslandType.MINESHAFT.isInIsland() ||
            IslandType.NONE.isInIsland() || IslandType.UNKNOWN.isInIsland()
}
