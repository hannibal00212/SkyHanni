package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent

class SecondPassedEvent(private val totalSeconds: Int) : SkyHanniEvent() {
    fun repeatSeconds(i: Int) = totalSeconds % i == 0
}
