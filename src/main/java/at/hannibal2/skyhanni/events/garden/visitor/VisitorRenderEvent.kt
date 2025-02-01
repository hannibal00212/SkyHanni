package at.hannibal2.skyhanni.events.garden.visitor

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.features.garden.visitor.VisitorApi
import at.hannibal2.skyhanni.utils.SkyHanniVec3d

class VisitorRenderEvent(
    val visitor: VisitorApi.Visitor,
    val location: SkyHanniVec3d,
    val parent: SkyHanniRenderWorldEvent,
) : SkyHanniEvent()
