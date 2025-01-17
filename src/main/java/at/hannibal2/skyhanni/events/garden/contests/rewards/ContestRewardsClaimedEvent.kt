package at.hannibal2.skyhanni.events.garden.contests.rewards

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.config.storage.ResettableStorageSet
import at.hannibal2.skyhanni.features.garden.AnitaMedalProfit
import at.hannibal2.skyhanni.features.garden.CropType

class ContestRewardsClaimedEvent(val rewards: ContestRewardSet, val messages: List<String>) : SkyHanniEvent()

data class ContestRewardSet(
    var jacobTickets: Int = 0,
    var carnivalTickets: Int = 0,
    var books: Map<CropType, Int> = emptyMap(),
    var medals: Map<AnitaMedalProfit.MedalType, Int> = emptyMap(),
    var bits: Int = 0
) : ResettableStorageSet()
