package at.hannibal2.skyhanni.utils

import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.resources.DefaultPlayerSkin
import net.minecraft.entity.player.EnumPlayerModelParts
import net.minecraft.scoreboard.ScorePlayerTeam

class FakePlayer : EntityOtherPlayerMP(Minecraft.getMinecraft().theWorld, Minecraft.getMinecraft().thePlayer.gameProfile) {

    override fun getLocationSkin() =
        Minecraft.getMinecraft().thePlayer.locationSkin ?: DefaultPlayerSkin.getDefaultSkin(Minecraft.getMinecraft().thePlayer.uniqueID)

    override fun getTeam() = object : ScorePlayerTeam(null, null) {
        override fun getNameTagVisibility() = EnumVisible.NEVER
    }

    override fun isWearing(part: EnumPlayerModelParts): Boolean =
        Minecraft.getMinecraft().thePlayer.isWearing(part) && part != EnumPlayerModelParts.CAPE
}
