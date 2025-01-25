package at.hannibal2.skyhanni.events.garden.pests

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.features.garden.pests.PestTrapAPI

class PestTrapDataUpdatedEvent(
    val data: MutableList<PestTrapAPI.PestTrapData>
) : SkyHanniEvent()
