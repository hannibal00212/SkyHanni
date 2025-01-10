package at.hannibal2.skyhanni.events.garden.farming

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.data.ClickType
import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.utils.LorenzVec
import net.minecraft.block.state.IBlockState
import net.minecraft.item.ItemStack

class CropClickEvent(
    val position: LorenzVec,
    val crop: CropType,
    val blockState: IBlockState,
    val clickType: ClickType,
    val itemInHand: ItemStack?,
) : SkyHanniEvent()
