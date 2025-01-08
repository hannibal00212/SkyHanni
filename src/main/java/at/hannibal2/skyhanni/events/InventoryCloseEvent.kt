package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent

class InventoryCloseEvent(val inventoryTitle: String, val reopenSameName: Boolean) : SkyHanniEvent()
