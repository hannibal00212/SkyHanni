package at.hannibal2.skyhanni.events.diana

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.features.event.diana.BurrowType
import at.hannibal2.skyhanni.utils.SkyHanniVec3d

class BurrowDetectEvent(val burrowLocation: SkyHanniVec3d, val type: BurrowType) : SkyHanniEvent()
