package at.hannibal2.skyhanni.events.entity

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.utils.NeuInternalName

class ItemAddInInventoryEvent(val internalName: NeuInternalName, val amount: Int) : SkyHanniEvent()
