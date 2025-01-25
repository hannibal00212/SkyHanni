package at.hannibal2.skyhanni.events.slayer

import at.hannibal2.skyhanni.api.event.SkyHanniEvent

class SlayerProgressChangeEvent(val oldProgress: String, val newProgress: String) : SkyHanniEvent()
