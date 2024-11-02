package at.hannibal2.skyhanni.mixins.hooks

import at.hannibal2.skyhanni.features.mining.MiningCommissionsBlocksColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import net.minecraft.block.state.IBlockState

fun modifyConnectedTexturesBlockState(state: IBlockState?): IBlockState? {
    if (!LorenzUtils.inSkyBlock) return state
    return MiningCommissionsBlocksColor.processState(state)
}

