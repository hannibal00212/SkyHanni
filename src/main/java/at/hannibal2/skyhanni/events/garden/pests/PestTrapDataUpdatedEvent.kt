package at.hannibal2.skyhanni.events.garden.pests

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.features.garden.pests.PestTrapApi

class PestTrapDataUpdatedEvent(
    val data: MutableList<PestTrapApi.PestTrapData>
) : SkyHanniEvent()
