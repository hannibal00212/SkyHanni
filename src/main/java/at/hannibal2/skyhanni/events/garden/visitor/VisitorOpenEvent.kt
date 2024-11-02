package at.hannibal2.skyhanni.events.garden.visitor

import at.hannibal2.skyhanni.events.LorenzEvent
import at.hannibal2.skyhanni.features.garden.visitor.VisitorAPI

class VisitorOpenEvent(val visitor: VisitorAPI.Visitor) : LorenzEvent()
