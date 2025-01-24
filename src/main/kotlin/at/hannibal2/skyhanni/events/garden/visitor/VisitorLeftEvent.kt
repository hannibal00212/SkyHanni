package at.hannibal2.skyhanni.events.garden.visitor

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.features.garden.visitor.VisitorAPI

class VisitorLeftEvent(val visitor: VisitorAPI.Visitor) : SkyHanniEvent()
