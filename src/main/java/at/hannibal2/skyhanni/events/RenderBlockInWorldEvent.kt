package at.hannibal2.skyhanni.events

import net.minecraft.block.state.IBlockState
import net.minecraft.util.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraftforge.fml.common.eventhandler.Cancelable

@Cancelable
data class RenderBlockInWorldEvent(@JvmField var state: IBlockState?, var world: IBlockAccess, var pos: BlockPos?) : LorenzEvent()
