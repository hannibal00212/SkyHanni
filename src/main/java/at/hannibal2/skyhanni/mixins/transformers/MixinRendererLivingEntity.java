package at.hannibal2.skyhanni.mixins.transformers;

import at.hannibal2.skyhanni.mixins.hooks.RendererLivingEntityHook;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RendererLivingEntity.class)
public class MixinRendererLivingEntity {

    @Redirect(method = "setScoreTeamColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFFF)V"))
    public void setOutlineColor(float colorRed, float colorGreen, float colorBlue, float colorAlpha, EntityLivingBase entity) {
        RendererLivingEntityHook.setOutlineColor(colorRed, colorGreen, colorBlue, colorAlpha, entity);
    }
}
