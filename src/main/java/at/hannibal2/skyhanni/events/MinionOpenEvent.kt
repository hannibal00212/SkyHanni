package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.utils.SkyHanniVec3d
import net.minecraft.item.ItemStack

class MinionOpenEvent(val inventoryName: String, val inventoryItems: Map<Int, ItemStack>) : SkyHanniEvent()
class MinionCloseEvent : SkyHanniEvent()
class MinionStorageOpenEvent(val position: SkyHanniVec3d?, val inventoryItems: Map<Int, ItemStack>) : SkyHanniEvent()
