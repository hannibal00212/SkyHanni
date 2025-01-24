package at.hannibal2.skyhanni.utils.compat

import at.hannibal2.skyhanni.utils.LorenzVec
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.EnumFacing
//#if MC > 1.12
//$$ import net.minecraft.client.Minecraft
//#endif

fun C08PacketPlayerBlockPlacement.getFacing(): EnumFacing =
    //#if MC < 1.12
    EnumFacing.getFront(placedBlockDirection)
//#else
//$$ direction
//#endif

fun C08PacketPlayerBlockPlacement.getUsedItem(): ItemStack? =
    //#if MC < 1.12
    stack
//#else
//$$ Minecraft.getMinecraft().player?.getHeldItem(hand)
//#endif

fun C03PacketPlayer.getLocation(): LorenzVec =
    //#if MC < 1.12
    LorenzVec(positionX, positionY, positionZ)
//#else
//$$ LorenzVec(getX(0.0), getY(0.0), getZ(0.0))
//#endif
