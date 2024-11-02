package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.garden.contest.FarmingContestPhase

class FarmingContestEvent(val crop: CropType, val phase: FarmingContestPhase) : LorenzEvent()
