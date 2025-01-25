package at.hannibal2.skyhanni.events.garden.visitor

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.events.minecraft.RenderWorldEvent
import at.hannibal2.skyhanni.features.garden.visitor.VisitorApi
import at.hannibal2.skyhanni.utils.LorenzVec

class VisitorRenderEvent(val visitor: VisitorApi.Visitor, val location: LorenzVec, val parent: RenderWorldEvent) : SkyHanniEvent()
