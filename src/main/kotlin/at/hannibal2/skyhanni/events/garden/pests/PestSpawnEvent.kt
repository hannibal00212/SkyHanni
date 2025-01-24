package at.hannibal2.skyhanni.events.garden.pests

import at.hannibal2.skyhanni.api.event.SkyHanniEvent

class PestSpawnEvent(val amountPests: Int, val plotNames: List<String>, val unknownAmount: Boolean) : SkyHanniEvent()
