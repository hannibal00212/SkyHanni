package at.hannibal2.skyhanni.utils

import net.minecraft.block.Block
import net.minecraft.block.properties.PropertyInteger
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.tileentity.TileEntitySkull
import net.minecraftforge.common.util.Constants

object BlockUtils {

    private val world get() = Minecraft.getMinecraft().theWorld

    fun LorenzVec.getBlockAt(): Block = getBlockStateAt().block

    fun LorenzVec.getBlockStateAt(): IBlockState = world.getBlockState(toBlockPos())

    fun LorenzVec.isInLoadedChunk(): Boolean = world.chunkProvider.provideChunk(toBlockPos()).isLoaded

    fun getTextureFromSkull(position: LorenzVec?): String? {
        val entity = world.getTileEntity(position?.toBlockPos()) as? TileEntitySkull ?: return null
        val serializeNBT = entity.serializeNBT()
        return serializeNBT.getCompoundTag("Owner").getCompoundTag("Properties")
            .getTagList("textures", Constants.NBT.TAG_COMPOUND).getCompoundTagAt(0).getString("Value")
    }

    fun IBlockState.isBabyCrop(): Boolean {
        val property = (block.blockState.properties.find { it.name == "age" } as? PropertyInteger) ?: return false
        return getValue(property) == 0
    }

    fun rayTrace(start: LorenzVec, direction: LorenzVec, distance: Double = 50.0): LorenzVec? {
        val target = start + direction.normalize() * distance
        val result = world.rayTraceBlocks(start.toVec3(), target.toVec3())

        return result?.blockPos?.toLorenzVec()
    }

    fun getBlockLookingAt(distance: Double = 10.0) = rayTrace(
        LocationUtils.playerEyeLocation(),
        Minecraft.getMinecraft().thePlayer.lookVec.toLorenzVec(),
        distance
    )
}
