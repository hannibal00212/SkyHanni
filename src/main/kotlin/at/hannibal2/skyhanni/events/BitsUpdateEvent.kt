package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent

open class BitsUpdateEvent(val bits: Int, val bitsAvailable: Int, val difference: Int = 0) : SkyHanniEvent() {
    class BitsGain(bits: Int, bitsAvailable: Int, difference: Int) : BitsUpdateEvent(bits, bitsAvailable, difference)
    class BitsSpent(bits: Int, bitsAvailable: Int) : BitsUpdateEvent(bits, bitsAvailable)
    class BitsAvailableGained(bits: Int, bitsAvailable: Int) : BitsUpdateEvent(bits, bitsAvailable)
}
