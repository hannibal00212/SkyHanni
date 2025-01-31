package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.CancellableSkyHanniEvent
import at.hannibal2.skyhanni.data.ItemAddManager
import at.hannibal2.skyhanni.utils.NeuInternalName

class ItemAddEvent(val internalName: NeuInternalName, val amount: Int, val source: ItemAddManager.Source) : CancellableSkyHanniEvent()
