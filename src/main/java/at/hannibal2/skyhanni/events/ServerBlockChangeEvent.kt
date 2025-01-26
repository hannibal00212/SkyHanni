package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.utils.BlockUtils.getBlockStateAt
import at.hannibal2.skyhanni.utils.RegexUtils.matchGroup
import at.hannibal2.skyhanni.utils.toLorenzVec
import net.minecraft.block.state.IBlockState
import net.minecraft.util.BlockPos

class ServerBlockChangeEvent(blockPos: BlockPos, blockState: IBlockState) : SkyHanniEvent() {

    val location = blockPos.toLorenzVec()
    val old by lazy { oldState.block.toString().getName() }
    val oldState by lazy { location.getBlockStateAt() }
    val new by lazy { blockState.block.toString().getName() }
    val newState = blockState

    companion object {

        private val pattern = "Block\\{minecraft:(?<name>.*)}".toPattern()

        private fun String.getName() = pattern.matchGroup(this, "name") ?: this
    }
}


