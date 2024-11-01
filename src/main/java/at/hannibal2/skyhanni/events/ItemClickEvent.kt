package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.data.ClickType
import net.minecraft.item.ItemStack

class ItemClickEvent(itemInHand: ItemStack?, clickType: ClickType) : WorldClickEvent(itemInHand, clickType)
