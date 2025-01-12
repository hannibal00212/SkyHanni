package at.hannibal2.skyhanni.events.fishing

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.features.fishing.SeaCreature

class SeaCreatureFishEvent(
    val seaCreature: SeaCreature,
    val chatEvent: LorenzChatEvent,
    val doubleHook: Boolean,
) : SkyHanniEvent()
