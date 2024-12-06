package at.hannibal2.skyhanni.events.mining

import at.hannibal2.skyhanni.api.event.SkyHanniEvent

class FossilExcavationEvent(val loot: List<Pair<String, Int>>) : SkyHanniEvent()
