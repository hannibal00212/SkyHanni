package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.features.garden.CropType
import net.minecraft.item.ItemStack

class GardenToolChangeEvent(val crop: CropType?, val toolItem: ItemStack?) : LorenzEvent()
