package at.hannibal2.skyhanni.events.mining

import at.hannibal2.skyhanni.api.HotmApi
import at.hannibal2.skyhanni.api.event.SkyHanniEvent

class PowderGainEvent(val powder: HotmApi.PowderType, val amount: Long) : SkyHanniEvent()
