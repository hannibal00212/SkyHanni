package at.hannibal2.skyhanni.utils

import com.mojang.authlib.GameProfile
import com.mojang.authlib.minecraft.MinecraftProfileTexture
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.resources.DefaultPlayerSkin
import net.minecraft.entity.player.EnumPlayerModelParts
import net.minecraft.scoreboard.ScorePlayerTeam
import net.minecraft.util.ResourceLocation

/**
 * Utility class for creating fake players with either the player's skin or a custom skin.
 *
 * @param gameProfile The game profile of the fake player. If null, the player's skin will be used.
 * @return A fake player entity.
 */

class FakePlayer(private val gameProfile: GameProfile? = null) :
    EntityOtherPlayerMP(Minecraft.getMinecraft().theWorld, Minecraft.getMinecraft().thePlayer.gameProfile) {

    override fun getLocationSkin(): ResourceLocation {
        val player = Minecraft.getMinecraft().thePlayer

        return gameProfile?.let {
            Minecraft.getMinecraft().skinManager.run {
                loadSkinFromCache(gameProfile)?.get(MinecraftProfileTexture.Type.SKIN)?.let {
                    loadSkin(it, MinecraftProfileTexture.Type.SKIN)
                }
            }
        } ?: player.locationSkin ?: DefaultPlayerSkin.getDefaultSkin(player.uniqueID)
    }

    override fun getTeam() = object : ScorePlayerTeam(null, null) {
        override fun getNameTagVisibility() = EnumVisible.NEVER
    }

    override fun isWearing(part: EnumPlayerModelParts): Boolean =
        Minecraft.getMinecraft().thePlayer.isWearing(part) && part != EnumPlayerModelParts.CAPE
}
