package at.hannibal2.skyhanni.events.garden.pests

import at.hannibal2.skyhanni.events.LorenzEvent
import at.hannibal2.skyhanni.features.garden.pests.PestType
import at.hannibal2.skyhanni.utils.NEUInternalName

class PestKillEvent(val pestType: PestType, val item: NEUInternalName, val amount: Int) : LorenzEvent() {
    var blockedReason: String? = null
}
