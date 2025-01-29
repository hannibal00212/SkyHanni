package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.CancellableSkyHanniEvent
import at.hannibal2.skyhanni.data.ClickType
import net.minecraft.item.ItemStack

open class WorldClickEvent(val itemInHand: ItemStack?, val clickType: ClickType) : CancellableSkyHanniEvent()
