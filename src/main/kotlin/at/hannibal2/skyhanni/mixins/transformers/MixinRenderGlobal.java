package at.hannibal2.skyhanni.mixins.transformers;

import at.hannibal2.skyhanni.mixins.hooks.RenderGlobalHook;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderGlobal.class)
public abstract class MixinRenderGlobal {

    @Shadow
    abstract boolean isRenderEntityOutlines();

    @Unique
    private final RenderGlobalHook skyHanni$hook = new RenderGlobalHook();

    @Redirect(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderGlobal;isRenderEntityOutlines()Z"))
    public boolean renderEntitiesOutlines(RenderGlobal self, Entity renderViewEntity, ICamera camera, float partialTicks) {
        return skyHanni$hook.renderEntitiesOutlines(camera, partialTicks) && this.isRenderEntityOutlines();
    }

    @Inject(method = "isRenderEntityOutlines", at = @At(value = "HEAD"), cancellable = true)
    public void isRenderEntityOutlinesWrapper(CallbackInfoReturnable<Boolean> cir) {
        skyHanni$hook.shouldRenderEntityOutlines(cir);
    }

    @Inject(method = "renderEntityOutlineFramebuffer", at = @At(value = "RETURN"))
    public void afterFramebufferDraw(CallbackInfo callbackInfo) {
        GlStateManager.enableDepth();
    }
}
