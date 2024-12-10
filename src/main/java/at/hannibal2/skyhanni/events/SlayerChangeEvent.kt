package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent

class SlayerChangeEvent(val oldSlayer: String, val newSlayer: String) : SkyHanniEvent()
