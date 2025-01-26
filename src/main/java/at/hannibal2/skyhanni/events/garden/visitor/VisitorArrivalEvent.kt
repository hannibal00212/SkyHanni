package at.hannibal2.skyhanni.events.garden.visitor

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.features.garden.visitor.VisitorApi.Visitor

class VisitorArrivalEvent(val visitor: Visitor) : SkyHanniEvent()
