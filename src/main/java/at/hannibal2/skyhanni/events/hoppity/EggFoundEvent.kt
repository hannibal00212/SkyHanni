package at.hannibal2.skyhanni.events.hoppity

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType

class EggFoundEvent(
    val type: HoppityEggType,
    val slotIndex: Int? = null,
    val note: String? = null,
    val chatEvent: LorenzChatEvent? = null
) : SkyHanniEvent()
