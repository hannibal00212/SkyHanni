package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.utils.NEUInternalName

class ItemInHandChangeEvent(val newItem: NEUInternalName, val oldItem: NEUInternalName) : SkyHanniEvent()
