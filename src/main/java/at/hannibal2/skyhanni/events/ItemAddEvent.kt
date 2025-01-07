package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.data.ItemAddManager
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.PrimitiveItemStack

class ItemAddEvent(val internalName: NEUInternalName, val amount: Int, val source: ItemAddManager.Source) : SkyHanniEvent() {
    val pStack: PrimitiveItemStack = PrimitiveItemStack(internalName, amount)
}
