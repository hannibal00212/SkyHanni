package at.hannibal2.skyhanni.events.mining

import at.hannibal2.skyhanni.api.HotmAPI
import at.hannibal2.skyhanni.api.event.SkyHanniEvent

open class PowderEvent(val powder: HotmAPI.PowderType) : SkyHanniEvent() {
    class Gain(powder: HotmAPI.PowderType, val amount: Long) : PowderEvent(powder)
    class Spent(powder: HotmAPI.PowderType, val amount: Long) : PowderEvent(powder)
}
