package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.utils.ItemUtils.getSkullTexture
import net.minecraft.block.Block
import net.minecraft.block.properties.PropertyInteger
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.tileentity.TileEntitySkull

object BlockUtils {

    private val world get() = Minecraft.getMinecraft().theWorld

    fun SkyHanniVec3d.getBlockAt(): Block = getBlockStateAt().block

    fun SkyHanniVec3d.getBlockStateAt(): IBlockState = world.getBlockState(toBlockPos())

    fun SkyHanniVec3d.isInLoadedChunk(): Boolean = world.isBlockLoaded(toBlockPos(), false)

    fun getTextureFromSkull(position: SkyHanniVec3d?): String? {
        val entity = world.getTileEntity(position?.toBlockPos()) as? TileEntitySkull ?: return null
        return entity.serializeNBT().getCompoundTag("Owner").getSkullTexture()
    }

    fun IBlockState.isBabyCrop(): Boolean {
        val property = (block.blockState.properties.find { it.name == "age" } as? PropertyInteger) ?: return false
        return getValue(property) == 0
    }

    fun rayTrace(start: SkyHanniVec3d, direction: SkyHanniVec3d, distance: Double = 50.0): SkyHanniVec3d? {
        val target = start + direction.normalize() * distance
        val result = world.rayTraceBlocks(start.toVec3(), target.toVec3())

        return result?.blockPos?.toLorenzVec()
    }

    fun getBlockLookingAt(distance: Double = 10.0) = rayTrace(
        LocationUtils.playerEyeLocation(),
        Minecraft.getMinecraft().thePlayer.lookVec.toLorenzVec(),
        distance,
    )
}
