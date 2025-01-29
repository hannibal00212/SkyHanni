package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent

open class BitsUpdateEvent(val bits: Int, val bitsAvailable: Int, val difference: Int) : SkyHanniEvent() {
    class BitsGain(bits: Int, bitsAvailable: Int, difference: Int) : BitsUpdateEvent(bits, bitsAvailable, difference)
    class BitsSpent(bits: Int, bitsAvailable: Int, difference: Int) : BitsUpdateEvent(bits, bitsAvailable, difference)
}

class BitsAvailableUpdateEvent(val bitsAvailable: Int) : SkyHanniEvent()
