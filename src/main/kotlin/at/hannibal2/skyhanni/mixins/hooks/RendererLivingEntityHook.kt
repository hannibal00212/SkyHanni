package at.hannibal2.skyhanni.mixins.hooks

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.features.misc.ContributorManager
import at.hannibal2.skyhanni.utils.EntityOutlineRenderer
import at.hannibal2.skyhanni.utils.LorenzUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer

object RendererLivingEntityHook {
    private val config get() = SkyHanniMod.feature.dev

    @JvmStatic
    fun setOutlineColor(red: Float, green: Float, blue: Float, alpha: Float, entity: EntityLivingBase) {
        val color = EntityOutlineRenderer.getCustomOutlineColor(entity)

        if (color != null) {
            val colorRed = (color shr 16 and 255).toFloat() / 255.0f
            val colorGreen = (color shr 8 and 255).toFloat() / 255.0f
            val colorBlue = (color and 255).toFloat() / 255.0f
            GlStateManager.color(colorRed, colorGreen, colorBlue, alpha)
        } else {
            GlStateManager.color(red, green, blue, alpha)
        }
    }

    /**
     * Check if the player is on the cool person list and if they should be flipped.
     */
    @JvmStatic
    fun shouldBeUpsideDown(userName: String?): Boolean {
        if (!LorenzUtils.inSkyBlock) return false
        if (!config.flipContributors && !LorenzUtils.isAprilFoolsDay) return false
        val name = userName ?: return false
        return ContributorManager.shouldBeUpsideDown(name)
    }

    /**
     * Check if the player should spin and rotate them if the option is on.
     */
    @JvmStatic
    fun rotatePlayer(player: EntityPlayer) {
        if (!LorenzUtils.inSkyBlock) return
        if (!config.rotateContributors && !LorenzUtils.isAprilFoolsDay) return
        val name = player.name ?: return
        if (!ContributorManager.shouldSpin(name)) return
        val rotation = ((player.ticksExisted % 90) * 4).toFloat()
        GlStateManager.rotate(rotation, 0f, 1f, 0f)
    }
}
