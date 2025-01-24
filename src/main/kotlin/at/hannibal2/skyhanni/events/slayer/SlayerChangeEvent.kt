package at.hannibal2.skyhanni.events.slayer

import at.hannibal2.skyhanni.api.event.SkyHanniEvent

class SlayerChangeEvent(val oldSlayer: String, val newSlayer: String) : SkyHanniEvent()
