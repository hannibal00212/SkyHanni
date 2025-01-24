package at.hannibal2.skyhanni.events.entity

import at.hannibal2.skyhanni.data.ClickType
import at.hannibal2.skyhanni.events.WorldClickEvent
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack

class EntityClickEvent(clickType: ClickType, val clickedEntity: Entity?, itemInHand: ItemStack?) : WorldClickEvent(itemInHand, clickType)
