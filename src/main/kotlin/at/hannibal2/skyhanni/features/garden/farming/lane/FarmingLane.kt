package at.hannibal2.skyhanni.features.garden.farming.lane

import com.google.gson.annotations.Expose

class FarmingLane(
    @Expose val direction: FarmingDirection,
    @Expose val min: Double,
    @Expose val max: Double,
)
