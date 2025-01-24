package at.hannibal2.skyhanni.events.fishing

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.features.fishing.SeaCreature

class SeaCreatureFishEvent(
    val seaCreature: SeaCreature,
    val chatEvent: SkyHanniChatEvent,
    val doubleHook: Boolean,
) : SkyHanniEvent()
