package at.hannibal2.skyhanni.events.garden.visitor

import at.hannibal2.skyhanni.events.LorenzEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.features.garden.visitor.VisitorAPI
import at.hannibal2.skyhanni.utils.LorenzVec

class VisitorRenderEvent(val visitor: VisitorAPI.Visitor, val location: LorenzVec, val parent: LorenzRenderWorldEvent) : LorenzEvent()
