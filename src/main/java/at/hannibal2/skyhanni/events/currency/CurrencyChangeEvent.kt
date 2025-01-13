package at.hannibal2.skyhanni.events.currency

import at.hannibal2.skyhanni.api.event.SkyHanniEvent

open class CurrencyChangeEvent(val difference: Int, val total: Long) : SkyHanniEvent() {

    class Copper(difference: Int, total: Long) : CurrencyChangeEvent(difference, total)
    class Motes(difference: Int, total: Long) : CurrencyChangeEvent(difference, total)
    class NorthStars(difference: Int, total: Long) : CurrencyChangeEvent(difference, total)

}
