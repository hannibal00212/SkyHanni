package at.hannibal2.skyhanni.features.garden.contest

import at.hannibal2.skyhanni.features.garden.CropType

data class FarmingContest(val time: Long, val crop: CropType, val brackets: Map<ContestBracket, Int>)
