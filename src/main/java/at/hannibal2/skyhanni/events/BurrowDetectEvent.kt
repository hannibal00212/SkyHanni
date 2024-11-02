package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.features.event.diana.BurrowType
import at.hannibal2.skyhanni.utils.LorenzVec

class BurrowDetectEvent(val burrowLocation: LorenzVec, val type: BurrowType) : LorenzEvent()
