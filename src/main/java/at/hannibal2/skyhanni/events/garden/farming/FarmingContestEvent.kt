package at.hannibal2.skyhanni.events.garden.farming

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.garden.contest.FarmingContestPhase

class FarmingContestEvent(val crop: CropType, val phase: FarmingContestPhase) : SkyHanniEvent()
