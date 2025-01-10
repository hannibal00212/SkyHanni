package at.hannibal2.skyhanni.events.bazaar

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.utils.NEUInternalName

class BazaarOpenedProductEvent(val openedProduct: NEUInternalName?, val inventoryOpenEvent: InventoryFullyOpenedEvent) : SkyHanniEvent()
