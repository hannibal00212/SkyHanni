package at.hannibal2.skyhanni.events.diana

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.features.event.diana.BurrowType
import at.hannibal2.skyhanni.utils.LorenzVec

class BurrowDetectEvent(val burrowLocation: LorenzVec, val type: BurrowType) : SkyHanniEvent()
