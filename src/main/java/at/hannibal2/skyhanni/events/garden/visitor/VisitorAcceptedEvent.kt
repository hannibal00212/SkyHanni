package at.hannibal2.skyhanni.events.garden.visitor

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.features.garden.visitor.VisitorApi

class VisitorAcceptedEvent(val visitor: VisitorApi.Visitor) : SkyHanniEvent()
