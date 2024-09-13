package at.hannibal2.skyhanni.mixins.hooks

import at.hannibal2.skyhanni.features.mining.MiningCommissionsBlocksColor
import at.hannibal2.skyhanni.features.mining.MiningCommissionsBlocksColor.CommissionBlock.Companion.onColor
import at.hannibal2.skyhanni.features.mining.MiningCommissionsBlocksColor.replaceBlocksMapCache
import at.hannibal2.skyhanni.features.mining.OreType.Companion.isOreType
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.LorenzUtils
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.resources.model.IBakedModel
import net.minecraft.util.BlockPos
import net.minecraft.world.IBlockAccess
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

// Taken and modified from Skytils
fun modifyGetModelFromBlockState(
    blockRendererDispatcher: BlockRendererDispatcher,
    state: IBlockState?,
    worldIn: IBlockAccess,
    pos: BlockPos?,
    cir: CallbackInfoReturnable<IBakedModel>,
) {
    if (state == null || pos == null) return
    var returnState: IBlockState = state

    if (!LorenzUtils.inSkyBlock) return

    try {
        if (MiningCommissionsBlocksColor.enabled && MiningCommissionsBlocksColor.active) {
            returnState = replaceBlocksMapCache.getOrPut(state) {
                MiningCommissionsBlocksColor.CommissionBlock.entries.firstOrNull {
                    state.isOreType(it.oreType)
                }?.onColor(state) ?: state
            }
        }
    } catch (e: Exception) {
        ErrorManager.logErrorWithData(e, "Error in MiningCommissionsBlocksColor")
    }

    if (returnState !== state) {
        cir.returnValue = blockRendererDispatcher.blockModelShapes.getModelForState(returnState)
    }
}
