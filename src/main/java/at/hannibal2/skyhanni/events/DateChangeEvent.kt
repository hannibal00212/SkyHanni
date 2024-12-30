package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import java.time.LocalDate

class DateChangeEvent(val oldDate: LocalDate, val newDate: LocalDate) : SkyHanniEvent()
