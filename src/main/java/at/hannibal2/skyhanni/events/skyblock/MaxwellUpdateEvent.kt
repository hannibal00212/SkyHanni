package at.hannibal2.skyhanni.events.skyblock

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.data.maxwell.MaxwellPower
import at.hannibal2.skyhanni.data.maxwell.MaxwellTunings

open class MaxwellUpdateEvent : SkyHanniEvent() {
    class Power(val power: MaxwellPower) : MaxwellUpdateEvent() {
        val name: String get() = power.name
    }
    class Tuning(val tunings: List<MaxwellTunings>) : MaxwellUpdateEvent()
    class MagicalPower(val mp: Int) : MaxwellUpdateEvent()
}
