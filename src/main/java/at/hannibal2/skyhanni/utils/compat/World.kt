package at.hannibal2.skyhanni.utils.compat

import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.potion.Potion

fun WorldClient.getLoadedPlayers(): List<EntityPlayer> =
//#if MC < 1.14
    this.playerEntities
//#else
//$$ this.players()
//#endif

fun Entity.getNameAsString(): String =
    this.name
//#if MC >= 1.14
//$$ .string
//#endif

fun EntityArmorStand.getArmorOrFullInventory() =
//#if MC < 1.12
    this.inventory
//#else
//$$ this.armorInventoryList
//#endif

fun Minecraft.isOnMainThread() =
//#if MC < 1.14
    this.isCallingFromMinecraftThread
//#else
//$$ this.isSameThread
//#endif

object Effects {
    val invisibility =
        //#if MC <1.12
        Potion.invisibility
    //#else
    //$$    net.minecraft.init.PotionTypes.INVISIBILITY
    //#endif
}
