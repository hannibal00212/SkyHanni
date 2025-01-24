package at.hannibal2.skyhanni.mixins.hooks

import at.hannibal2.skyhanni.utils.EntityOutlineRenderer
import at.hannibal2.skyhanni.utils.RenderUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.culling.ICamera
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

class RenderGlobalHook {

    fun renderEntitiesOutlines(camera: ICamera?, partialTicks: Float): Boolean {
        val vec = Minecraft.getMinecraft().renderViewEntity?.let { RenderUtils.exactLocation(it, partialTicks) } ?: return false
        return EntityOutlineRenderer.renderEntityOutlines(camera!!, partialTicks, vec)
    }

    fun shouldRenderEntityOutlines(cir: CallbackInfoReturnable<Boolean?>) {
        if (EntityOutlineRenderer.shouldRenderEntityOutlines()) {
            cir.returnValue = true
        }
    }
}
