package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.utils.LorenzVec
import net.minecraft.item.ItemStack

class MinionOpenEvent(val inventoryName: String, val inventoryItems: Map<Int, ItemStack>) : SkyHanniEvent()
class MinionCloseEvent : SkyHanniEvent()
class MinionStorageOpenEvent(val position: LorenzVec?, val inventoryItems: Map<Int, ItemStack>) : SkyHanniEvent()
